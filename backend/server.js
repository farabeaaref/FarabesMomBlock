const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');
const { execSync } = require('child_process');

const PORT = process.env.PORT || 8787;
const ROOT_DIR = __dirname;
const PROJECT_DIR = path.resolve(ROOT_DIR, '..');
const CONFIG_PATH = path.join(ROOT_DIR, 'config.json');
const UPLOADS_DIR = path.join(ROOT_DIR, 'uploads');
const PUBLIC_DIR = path.join(ROOT_DIR, 'public');
const CLOUD_SYNC_DIR = path.join(PROJECT_DIR, 'cloud-sync');

fs.mkdirSync(UPLOADS_DIR, { recursive: true });
fs.mkdirSync(PUBLIC_DIR, { recursive: true });

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.webp': 'image/webp',
  '.mp3': 'audio/mpeg',
  '.wav': 'audio/wav',
  '.ogg': 'audio/ogg',
  '.m4a': 'audio/mp4',
  '.apk': 'application/vnd.android.package-archive'
};

function readConfig() {
  return JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf8'));
}

function writeConfig(cfg) {
  fs.writeFileSync(CONFIG_PATH, JSON.stringify(cfg, null, 2), 'utf8');
}

function resolveUrl(val, baseUrl) {
  if (!val || typeof val !== 'string') return '';
  if (val.startsWith('http://') || val.startsWith('https://')) return val;
  if (val.startsWith('/')) return `${baseUrl}${val}`;
  return `${baseUrl}/${val}`;
}

function resolveConfigUrls(cfg, baseUrl) {
  const clone = JSON.parse(JSON.stringify(cfg));
  if (clone.background) {
    clone.background.homeBackgroundUrl = resolveUrl(clone.background.homeBackgroundUrl, baseUrl);
    clone.background.gameplayBackgroundUrl = resolveUrl(clone.background.gameplayBackgroundUrl, baseUrl);
  }
  if (clone.images) {
    clone.images.momPhotoUrl = resolveUrl(clone.images.momPhotoUrl, baseUrl);
    clone.images.helperAvatarUrl = resolveUrl(clone.images.helperAvatarUrl, baseUrl);
    clone.images.sketchbookImageUrl = resolveUrl(clone.images.sketchbookImageUrl, baseUrl);
  }
  if (clone.music) {
    clone.music.activeSongUrl = resolveUrl(clone.music.activeSongUrl, baseUrl);
    if (Array.isArray(clone.music.songs)) {
      clone.music.songs = clone.music.songs.map(s => ({
        ...s,
        url: resolveUrl(s.url, baseUrl)
      }));
    }
  }
  return clone;
}

function sendJson(res, status, data) {
  const body = JSON.stringify(data, null, 2);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, X-Filename'
  });
  res.end(body);
}

function readJsonBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    req.on('data', c => chunks.push(c));
    req.on('end', () => {
      try {
        const raw = Buffer.concat(chunks).toString('utf8');
        resolve(raw ? JSON.parse(raw) : {});
      } catch (e) {
        reject(e);
      }
    });
    req.on('error', reject);
  });
}

function getLanIps() {
  const nets = os.networkInterfaces();
  const ips = [];
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      if (net.family === 'IPv4' && !net.internal) {
        ips.push(net.address);
      }
    }
  }
  return ips;
}

function bumpVersion(cfg, notifyTitle, notifyBody) {
  cfg.configVersion = (Number(cfg.configVersion) || 1) + 1;
  cfg.notification = {
    notifyId: `notify_${cfg.configVersion}_${Date.now()}`,
    title: notifyTitle || cfg.notification?.title || 'আম্মু, নতুন সারপ্রাইজ এসেছে! ❤️',
    body: notifyBody || cfg.notification?.body || 'ফারাবী তোমার গেমে নতুন কিছু আপডেট করেছে!',
    timestamp: new Date().toISOString()
  };
}

function exportCloudSync(cfg, autoGitPush = false) {
  const cloudUploads = path.join(CLOUD_SYNC_DIR, 'uploads');
  fs.mkdirSync(cloudUploads, { recursive: true });

  // Copy all uploaded media to cloud-sync/uploads
  for (const file of fs.readdirSync(UPLOADS_DIR)) {
    fs.copyFileSync(path.join(UPLOADS_DIR, file), path.join(cloudUploads, file));
  }

  const base = (cfg.githubRawBaseUrl || '').replace(/\/+$/, '');
  const cloudCfg = resolveConfigUrls(cfg, base);
  fs.writeFileSync(path.join(CLOUD_SYNC_DIR, 'config.json'), JSON.stringify(cloudCfg, null, 2), 'utf8');

  let gitResult = 'Exported to cloud-sync/ folder. Ready to commit & push to GitHub.';
  if (autoGitPush) {
    try {
      execSync('git status', { cwd: PROJECT_DIR, stdio: 'ignore' });
      execSync('git add cloud-sync version.json', { cwd: PROJECT_DIR, stdio: 'pipe' });
      const pending = execSync('git status --porcelain cloud-sync version.json', { cwd: PROJECT_DIR }).toString().trim();
      if (pending) {
        execSync(`git commit -m "Mom update v${cfg.configVersion}"`, { cwd: PROJECT_DIR, stdio: 'pipe' });
      }
      execSync('git push -u origin HEAD', { cwd: PROJECT_DIR, stdio: 'pipe' });
      gitResult = 'Exported and pushed to GitHub!';
    } catch (err) {
      gitResult = `Exported to cloud-sync/ folder (git push skipped: ${err.message.split('\n')[0]})`;
    }
  }
  return { ok: true, cloudSyncDir: CLOUD_SYNC_DIR, message: gitResult };
}

const server = http.createServer(async (req, res) => {
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, X-Filename'
    });
    return res.end();
  }

  const urlObj = new URL(req.url, `http://${req.headers.host || 'localhost:' + PORT}`);
  const pathname = decodeURIComponent(urlObj.pathname);
  const baseUrl = `http://${req.headers.host || 'localhost:' + PORT}`;

  try {
    // 1. Admin Dashboard UI
    if (req.method === 'GET' && (pathname === '/' || pathname === '/index.html')) {
      const html = fs.readFileSync(path.join(PUBLIC_DIR, 'index.html'));
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      return res.end(html);
    }

    // 2. Get Live Game Config (used by Android app & Admin Studio)
    if (req.method === 'GET' && pathname === '/api/config') {
      const cfg = readConfig();
      const rawMode = urlObj.searchParams.get('raw') === '1';
      return sendJson(res, 200, rawMode ? cfg : resolveConfigUrls(cfg, baseUrl));
    }

    // 3. Server Info (LAN IP, paths)
    if (req.method === 'GET' && pathname === '/api/info') {
      return sendJson(res, 200, {
        port: PORT,
        emulatorUrl: `http://10.0.2.2:${PORT}/api/config`,
        lanUrls: getLanIps().map(ip => `http://${ip}:${PORT}/api/config`),
        uploads: fs.readdirSync(UPLOADS_DIR)
      });
    }

    // 4. Save Full Config & Trigger Notification
    if (req.method === 'POST' && pathname === '/api/config') {
      const body = await readJsonBody(req);
      const current = readConfig();
      const updated = {
        ...current,
        ...body,
        theme: { ...current.theme, ...(body.theme || {}) },
        background: { ...current.background, ...(body.background || {}) },
        images: { ...current.images, ...(body.images || {}) },
        music: { ...current.music, ...(body.music || {}) },
        gifts: { ...current.gifts, ...(body.gifts || {}) }
      };
      bumpVersion(updated, body.notification?.title, body.notification?.body);
      writeConfig(updated);
      exportCloudSync(updated, false);
      return sendJson(res, 200, { ok: true, config: resolveConfigUrls(updated, baseUrl) });
    }

    // 5. Send Instant Gift to Mom
    if (req.method === 'POST' && pathname === '/api/gift') {
      const body = await readJsonBody(req);
      const cfg = readConfig();
      cfg.gifts = cfg.gifts || {};
      cfg.gifts.activeGift = {
        giftId: `gift_${Date.now()}`,
        title: body.title || 'Surprise Gift from Farabe! 🎁',
        messageBengali: body.messageBengali || 'আম্মু, তোমার জন্য ভালোবাসার উপহার ❤️',
        coinsReward: Number(body.coinsReward) || 200,
        heartsReward: Number(body.heartsReward) || 2
      };
      bumpVersion(
        cfg,
        'আম্মু, ফারাবী তোমার জন্য উপহার পাঠিয়েছে! 🎁❤️',
        `${cfg.gifts.activeGift.title} (+${cfg.gifts.activeGift.coinsReward} coins)`
      );
      writeConfig(cfg);
      exportCloudSync(cfg, false);
      return sendJson(res, 200, { ok: true, config: resolveConfigUrls(cfg, baseUrl) });
    }

    // 6. Upload Media (background, mom photo, helper avatar, sketchbook, mp3 song)
    if (req.method === 'POST' && pathname === '/api/upload') {
      const targetType = urlObj.searchParams.get('type') || 'song';
      const origName = urlObj.searchParams.get('name') || req.headers['x-filename'] || 'upload.bin';
      const cleanName = `${Date.now()}_${path.basename(origName).replace(/[^a-zA-Z0-9._-]/g, '_')}`;
      const destPath = path.join(UPLOADS_DIR, cleanName);

      const chunks = [];
      for await (const chunk of req) chunks.push(chunk);
      fs.writeFileSync(destPath, Buffer.concat(chunks));

      const relUrl = `/uploads/${cleanName}`;
      const cfg = readConfig();

      if (targetType === 'home_bg') {
        cfg.background.homeBackgroundUrl = relUrl;
        bumpVersion(cfg, 'আম্মু, নতুন সুন্দর ব্যাকগ্রাউন্ড এসেছে! 🌸', 'ফারাবী গেমের হোম স্ক্রিন সাজিয়েছে, এখনই দেখো!');
      } else if (targetType === 'game_bg') {
        cfg.background.gameplayBackgroundUrl = relUrl;
        bumpVersion(cfg, 'আম্মু, বোর্ডের নতুন ডিজাইন এসেছে! ✨', 'খেলার বোর্ডের নতুন ব্যাকগ্রাউন্ড আপডেট করো!');
      } else if (targetType === 'mom_photo') {
        cfg.images.momPhotoUrl = relUrl;
        bumpVersion(cfg, 'আম্মু, নতুন স্মৃতির ছবি যোগ হয়েছে! 📸❤️', 'স্কেচবুকে তোমার জন্য নতুন ছবি আছে!');
      } else if (targetType === 'helper_avatar') {
        cfg.images.helperAvatarUrl = relUrl;
        bumpVersion(cfg, 'আম্মু, নতুন হেল্পার অবতার আপডেট হয়েছে! 😊', 'হেল্পার ট্যাবে নতুন ছবি দেখো!');
      } else if (targetType === 'sketchbook') {
        cfg.images.sketchbookImageUrl = relUrl;
        bumpVersion(cfg, 'আম্মু, স্কেচবুকে নতুন ছবি এসেছে! 🎨', 'নতুন অধ্যায়ের ছবি আনলক করতে ওপেন করো!');
      } else if (targetType === 'song') {
        const displayTitle = path.basename(origName, path.extname(origName));
        cfg.music.activeSongTitle = displayTitle;
        cfg.music.activeSongUrl = relUrl;
        cfg.music.songs = cfg.music.songs || [];
        cfg.music.songs.unshift({ title: displayTitle, url: relUrl, addedAt: new Date().toISOString() });
        bumpVersion(cfg, 'আম্মু, নতুন গান যোগ হয়েছে! 🎵❤️', `"${displayTitle}" শুনতে গেমটি আপডেট করো!`);
      }

      writeConfig(cfg);
      exportCloudSync(cfg, false);
      return sendJson(res, 200, { ok: true, fileUrl: resolveUrl(relUrl, baseUrl), config: resolveConfigUrls(cfg, baseUrl) });
    }

    // 7. Export & Optional Git Push for Mom's real phone over GitHub
    if (req.method === 'POST' && pathname === '/api/export-github') {
      const body = await readJsonBody(req);
      const cfg = readConfig();
      if (body.githubRawBaseUrl) {
        cfg.githubRawBaseUrl = body.githubRawBaseUrl;
        writeConfig(cfg);
      }
      const result = exportCloudSync(cfg, Boolean(body.autoPush));
      return sendJson(res, 200, result);
    }

    // 8. Serve Uploaded Media Files
    if (req.method === 'GET' && pathname.startsWith('/uploads/')) {
      const fileName = path.basename(pathname.slice('/uploads/'.length));
      const fullPath = path.join(UPLOADS_DIR, fileName);
      if (!fs.existsSync(fullPath)) {
        res.writeHead(404);
        return res.end('Not found');
      }
      const ext = path.extname(fullPath).toLowerCase();
      const stat = fs.statSync(fullPath);
      res.writeHead(200, {
        'Content-Type': MIME_TYPES[ext] || 'application/octet-stream',
        'Content-Length': stat.size,
        'Accept-Ranges': 'bytes',
        'Access-Control-Allow-Origin': '*'
      });
      return fs.createReadStream(fullPath).pipe(res);
    }

    res.writeHead(404);
    res.end('Not found');
  } catch (err) {
    sendJson(res, 500, { ok: false, error: err.message });
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log('====================================================');
  console.log("  FARABE'S MOM - BLOCK : LIVE GAME CONTROL STUDIO");
  console.log('====================================================');
  console.log(`  Admin Studio:  http://localhost:${PORT}`);
  console.log(`  Emulator API:  http://10.0.2.2:${PORT}/api/config`);
  for (const ip of getLanIps()) {
    console.log(`  Phone Wi-Fi:   http://${ip}:${PORT}/api/config`);
  }
  console.log('====================================================');
});
