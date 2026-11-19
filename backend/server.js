const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');
const { execSync } = require('child_process');

const PORT = process.env.PORT || 8787;
const ROOT_DIR = __dirname;
const PROJECT_DIR = path.resolve(ROOT_DIR, '..');
const DATA_DIR = process.env.DATA_DIR ? path.resolve(process.env.DATA_DIR) : ROOT_DIR;
const SEED_CONFIG_PATH = path.join(ROOT_DIR, 'config.json');
const CONFIG_PATH = path.join(DATA_DIR, 'config.json');
const PLAYERS_PATH = path.join(DATA_DIR, 'players.json');
const UPLOADS_DIR = path.join(DATA_DIR, 'uploads');
const PUBLIC_DIR = path.join(ROOT_DIR, 'public');
const CLOUD_SYNC_DIR = path.join(PROJECT_DIR, 'cloud-sync');

fs.mkdirSync(DATA_DIR, { recursive: true });
fs.mkdirSync(UPLOADS_DIR, { recursive: true });
fs.mkdirSync(PUBLIC_DIR, { recursive: true });

// Prune duplicate timestamped APK uploads on boot so persistent volume never fills up
try {
  for (const f of fs.readdirSync(UPLOADS_DIR)) {
    if (f.toLowerCase().endsWith('.apk') && f !== 'FarabesMomBlock-latest.apk') {
      fs.unlinkSync(path.join(UPLOADS_DIR, f));
    }
  }
} catch (_) {}

// Seed persistent volume with initial config on first Railway boot
if (!fs.existsSync(CONFIG_PATH) && fs.existsSync(SEED_CONFIG_PATH)) {
  fs.copyFileSync(SEED_CONFIG_PATH, CONFIG_PATH);
}
if (!fs.existsSync(CONFIG_PATH)) {
  fs.writeFileSync(CONFIG_PATH, JSON.stringify({ configVersion: 1, theme: {}, background: {}, images: {}, music: {}, voice: {}, gifts: {} }, null, 2), 'utf8');
}

function readPlayers() {
  try {
    if (fs.existsSync(PLAYERS_PATH)) {
      return JSON.parse(fs.readFileSync(PLAYERS_PATH, 'utf8'));
    }
  } catch (_) {}
  return {};
}

function writePlayers(playersObj) {
  try {
    fs.writeFileSync(PLAYERS_PATH, JSON.stringify(playersObj, null, 2), 'utf8');
  } catch (_) {}
}

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
  if (clone.appDownloadUrl) {
    clone.appDownloadUrl = resolveUrl(clone.appDownloadUrl, baseUrl);
  }
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
  if (clone.voice) {
    clone.voice.winVoiceUrl = resolveUrl(clone.voice.winVoiceUrl, baseUrl);
    clone.voice.loseVoiceUrl = resolveUrl(clone.voice.loseVoiceUrl, baseUrl);
    clone.voice.comboVoiceUrl = resolveUrl(clone.voice.comboVoiceUrl, baseUrl);
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
  const isCloudContainer = Boolean(process.env.RAILWAY_ENVIRONMENT || process.env.RAILWAY_SERVICE_ID || !fs.existsSync(CLOUD_SYNC_DIR));
  if (isCloudContainer && !autoGitPush) {
    return { ok: true, cloudSyncDir: DATA_DIR, message: 'Live on Railway Cloud — all family devices sync directly!' };
  }
  try {
    const cloudUploads = path.join(CLOUD_SYNC_DIR, 'uploads');
    fs.mkdirSync(cloudUploads, { recursive: true });

    // Copy all uploaded media to cloud-sync/uploads
    if (fs.existsSync(UPLOADS_DIR)) {
      for (const file of fs.readdirSync(UPLOADS_DIR)) {
        fs.copyFileSync(path.join(UPLOADS_DIR, file), path.join(cloudUploads, file));
      }
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
  } catch (err) {
    return { ok: true, cloudSyncDir: DATA_DIR, message: `Saved live on server (${err.message})` };
  }
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

  const rawProto = String(req.headers['x-forwarded-proto'] || 'http').split(',')[0].trim().toLowerCase();
  const proto = rawProto === 'https' ? 'https' : 'http';
  const host = req.headers['x-forwarded-host'] || req.headers.host || ('localhost:' + PORT);
  const baseUrl = `${proto}://${host}`;
  const urlObj = new URL(req.url, baseUrl);
  const pathname = decodeURIComponent(urlObj.pathname);

  try {
    // 0. Cloud Healthcheck
    if (req.method === 'GET' && pathname === '/health') {
      return sendJson(res, 200, {
        ok: true,
        status: 'healthy',
        uptimeSeconds: Math.floor(process.uptime()),
        dataDir: DATA_DIR
      });
    }

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

    // 3. Server Info (Public Cloud URL, LAN IP, paths)
    if (req.method === 'GET' && pathname === '/api/info') {
      const isCloud = Boolean(process.env.RAILWAY_ENVIRONMENT || process.env.RAILWAY_SERVICE_ID || proto === 'https');
      return sendJson(res, 200, {
        port: PORT,
        isCloud,
        publicBaseUrl: baseUrl,
        publicConfigUrl: `${baseUrl}/api/config`,
        emulatorUrl: `http://10.0.2.2:${PORT}/api/config`,
        lanUrls: getLanIps().map(ip => `http://${ip}:${PORT}/api/config`),
        uploads: fs.existsSync(UPLOADS_DIR) ? fs.readdirSync(UPLOADS_DIR) : []
      });
    }

    // 3B. Telemetry Heartbeat from Active Game Players (Mom's phone / Emulator)
    if (req.method === 'POST' && pathname === '/api/telemetry/heartbeat') {
      const body = await readJsonBody(req);
      const players = readPlayers();
      const playerId = String(body.playerId || 'mom_default_device').replace(/[^a-zA-Z0-9_-]/g, '_');
      const now = Date.now();
      const prev = players[playerId] || {};
      const clientIp = (req.headers['x-forwarded-for'] || req.socket.remoteAddress || '').replace('::ffff:', '');

      players[playerId] = {
        ...prev,
        playerId,
        playerName: body.playerName || prev.playerName || 'Mom ❤️',
        deviceModel: body.deviceModel || prev.deviceModel || 'Android Phone',
        appVersion: body.appVersion || prev.appVersion || '1.2.0',
        currentActivity: body.currentActivity || 'Playing Game',
        screenState: body.screenState || prev.screenState || 'Home Hub',
        isInGame: Boolean(body.isInGame),
        isPaused: Boolean(body.isPaused),
        isLevelCompleted: Boolean(body.isLevelCompleted),
        isGameOver: Boolean(body.isGameOver),
        gameMode: body.gameMode || 'LEVEL_1',
        currentLevelNumber: Number(body.currentLevelNumber ?? prev.currentLevelNumber ?? 0),
        currentLevelTitle: body.currentLevelTitle || prev.currentLevelTitle || 'Challenge',
        levelTargetScore: Number(body.levelTargetScore ?? prev.levelTargetScore ?? 0),
        levelTargetLines: Number(body.levelTargetLines ?? prev.levelTargetLines ?? 0),
        currentScore: Number(body.currentScore ?? prev.currentScore ?? 0),
        linesInCurrentRound: Number(body.linesInCurrentRound ?? prev.linesInCurrentRound ?? 0),
        movesInCurrentRound: Number(body.movesInCurrentRound ?? prev.movesInCurrentRound ?? 0),
        currentRoundSeconds: Number(body.currentRoundSeconds ?? prev.currentRoundSeconds ?? 0),
        highScore: Number(body.highScore ?? prev.highScore ?? 0),
        coins: Number(body.coins ?? prev.coins ?? 500),
        hearts: Number(body.hearts ?? prev.hearts ?? 5),
        gems: Number(body.gems ?? prev.gems ?? 5),
        farabeHelperLevel: Number(body.farabeHelperLevel ?? prev.farabeHelperLevel ?? 1),
        kittenHelperLevel: Number(body.kittenHelperLevel ?? prev.kittenHelperLevel ?? 1),
        highestUnlockedLevel: Number(body.highestUnlockedLevel ?? prev.highestUnlockedLevel ?? 1),
        completedLevelsCount: Number(body.completedLevelsCount ?? prev.completedLevelsCount ?? 0),
        completedLevels: Array.isArray(body.completedLevels) ? body.completedLevels : (prev.completedLevels || []),
        claimedGiftsCount: Number(body.claimedGiftsCount ?? prev.claimedGiftsCount ?? 0),
        lastClaimedGiftId: body.lastClaimedGiftId || prev.lastClaimedGiftId || '',
        lastClaimedGiftTitle: body.lastClaimedGiftTitle || prev.lastClaimedGiftTitle || '',
        claimedTasks: Array.isArray(body.claimedTasks) ? body.claimedTasks : (prev.claimedTasks || []),
        totalGamesPlayed: Number(body.totalGamesPlayed ?? prev.totalGamesPlayed ?? 0),
        totalLinesCleared: Number(body.totalLinesCleared ?? prev.totalLinesCleared ?? 0),
        sessionActiveSeconds: Number(body.sessionActiveSeconds ?? prev.sessionActiveSeconds ?? 0),
        totalPlayTimeSeconds: Number(body.totalPlayTimeSeconds ?? prev.totalPlayTimeSeconds ?? 0),
        recentEvents: Array.isArray(body.recentEvents) && body.recentEvents.length > 0 ? body.recentEvents : (prev.recentEvents || []),
        isForeground: body.isForeground !== false,
        ipAddress: clientIp || prev.ipAddress || 'local',
        firstSeenAt: prev.firstSeenAt || new Date(now).toISOString(),
        lastSeenAt: new Date(now).toISOString(),
        lastHeartbeatMs: now,
        pendingCommand: prev.pendingCommand || null
      };

      const pendingCommand = players[playerId].pendingCommand || null;
      if (pendingCommand) {
        players[playerId].pendingCommand = null;
      }

      writePlayers(players);
      const currentCfg = readConfig();
      return sendJson(res, 200, {
        ok: true,
        serverTimeMs: now,
        configVersion: Number(currentCfg.configVersion || 1),
        appVersionCode: Number(currentCfg.appVersionCode || 1),
        pendingCommand
      });
    }

    // 3B-2. Queue a Live Remote-Control Command for a Specific Family Player (or ALL)
    if (req.method === 'POST' && pathname === '/api/player-command') {
      const body = await readJsonBody(req);
      const players = readPlayers();
      const targetId = body.playerId || 'ALL';
      const cmd = {
        commandId: `cmd_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
        action: body.action || body.type || 'FORCE_SYNC',
        level: Number(body.level || 1),
        launchNow: Boolean(body.launchNow || body.autoStart),
        title: body.title || 'Surprise Gift from Farabe! 🎁',
        message: body.message || 'আম্মু ও পরিবারের জন্য বিশেষ উপহার ❤️',
        coins: Number(body.coins || 300),
        hearts: Number(body.hearts || 3),
        coinsDelta: Number(body.coinsDelta || 0),
        heartsDelta: Number(body.heartsDelta || 0),
        gemsDelta: Number(body.gemsDelta || 0),
        createdAt: new Date().toISOString()
      };

      let affected = 0;
      for (const pid of Object.keys(players)) {
        if (targetId === 'ALL' || pid === targetId) {
          players[pid].pendingCommand = cmd;
          affected++;
        }
      }
      writePlayers(players);
      return sendJson(res, 200, { ok: true, affected, command: cmd });
    }

    // 3C. Get Active & Recent Players List for Admin Studio
    if (req.method === 'GET' && pathname === '/api/telemetry/players') {
      const players = readPlayers();
      const now = Date.now();
      const list = Object.values(players).map(p => {
        const elapsedSec = Math.max(0, Math.floor((now - (p.lastHeartbeatMs || 0)) / 1000));
        const isOnline = p.isForeground !== false && elapsedSec <= 35;
        const isIdle = !isOnline && elapsedSec <= 120;
        const liveSessionSeconds = isOnline
          ? (Number(p.sessionActiveSeconds || 0) + Math.min(15, elapsedSec))
          : Number(p.sessionActiveSeconds || 0);
        const liveTotalSeconds = isOnline
          ? (Number(p.totalPlayTimeSeconds || 0) + Math.min(15, elapsedSec))
          : Number(p.totalPlayTimeSeconds || 0);
        return {
          ...p,
          isOnline,
          isIdle,
          secondsSinceLastSeen: elapsedSec,
          liveSessionSeconds,
          liveTotalSeconds
        };
      }).sort((a, b) => {
        if (a.isOnline !== b.isOnline) return a.isOnline ? -1 : 1;
        return (b.lastHeartbeatMs || 0) - (a.lastHeartbeatMs || 0);
      });

      return sendJson(res, 200, {
        ok: true,
        onlineCount: list.filter(p => p.isOnline).length,
        totalTracked: list.length,
        players: list
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
        voice: { ...(current.voice || {}), ...(body.voice || {}) },
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

    // 6. Upload Media (background, mom photo, helper avatar, sketchbook, mp3 song, apk)
    if (req.method === 'POST' && pathname === '/api/upload') {
      const targetType = urlObj.searchParams.get('type') || 'song';
      const origName = urlObj.searchParams.get('name') || req.headers['x-filename'] || 'upload.bin';
      if (targetType === 'apk') {
        try {
          for (const f of fs.readdirSync(UPLOADS_DIR)) {
            if (f.toLowerCase().endsWith('.apk')) {
              fs.unlinkSync(path.join(UPLOADS_DIR, f));
            }
          }
        } catch (_) {}
      }
      const cleanName = targetType === 'apk'
        ? 'FarabesMomBlock-latest.apk'
        : `${Date.now()}_${path.basename(origName).replace(/[^a-zA-Z0-9._-]/g, '_')}`;
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
      } else if (targetType === 'win_voice' || targetType === 'lose_voice' || targetType === 'combo_voice') {
        cfg.voice = cfg.voice || {
          winVoiceUrl: '',
          loseVoiceUrl: '',
          comboVoiceUrl: '',
          enableWinVoice: true,
          enableLoseVoice: true,
          enableComboVoice: true,
          duckMusicWhileSpeaking: true
        };
        if (targetType === 'win_voice') {
          cfg.voice.winVoiceUrl = relUrl;
          cfg.voice.enableWinVoice = true;
          bumpVersion(cfg, 'আম্মু, ফারাবীর নতুন জয়ের ভয়েস মেসেজ এসেছে! 🎙️❤️', 'জিতলে বা নতুন রেকর্ড করলে আমার কণ্ঠস্বর শুনতে পাবে!');
        } else if (targetType === 'lose_voice') {
          cfg.voice.loseVoiceUrl = relUrl;
          cfg.voice.enableLoseVoice = true;
          bumpVersion(cfg, 'আম্মু, ফারাবীর ভালোবাসার ভয়েস নোট এসেছে! 🎙️🌸', 'গেম ওভারে তোমার জন্য আমার উৎসাহের কথা!');
        } else {
          cfg.voice.comboVoiceUrl = relUrl;
          cfg.voice.enableComboVoice = true;
          bumpVersion(cfg, 'আম্মু, নতুন কম্বো ভয়েস যোগ হয়েছে! 🎙️✨', 'বড় কম্বো করলে ফারাবীর কণ্ঠ শুনবে!');
        }
      } else if (targetType === 'apk') {
        const reqVersionCode = Number(urlObj.searchParams.get('versionCode'));
        const reqVersionName = urlObj.searchParams.get('versionName') || '';
        const reqReleaseNotes = urlObj.searchParams.get('releaseNotes') || '';
        cfg.appVersionCode = reqVersionCode > 0 ? reqVersionCode : ((Number(cfg.appVersionCode) || 3) + 1);
        if (reqVersionName) cfg.appVersionName = reqVersionName;
        if (reqReleaseNotes) cfg.appReleaseNotes = reqReleaseNotes;
        cfg.appDownloadUrl = '/uploads/FarabesMomBlock-latest.apk';
        bumpVersion(
          cfg,
          `আম্মু ও পরিবার, গেমের নতুন ভার্সন v${cfg.appVersionName || cfg.appVersionCode} এসেছে!`,
          'নতুন ফিচার পেতে গেমের ভেতর Update বাটনে ট্যাপ করো!'
        );
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

    // 8. Serve Uploaded Media Files (with automatic fallback to GitHub cloud-sync for seeded assets)
    if (req.method === 'GET' && pathname.startsWith('/uploads/')) {
      const fileName = path.basename(pathname.slice('/uploads/'.length));
      const fullPath = path.join(UPLOADS_DIR, fileName);
      if (!fs.existsSync(fullPath)) {
        const cfg = readConfig();
        const ghBase = (cfg.githubRawBaseUrl || '').replace(/\/+$/, '');
        if (ghBase) {
          res.writeHead(302, {
            'Location': `${ghBase}/uploads/${encodeURIComponent(fileName)}`,
            'Access-Control-Allow-Origin': '*'
          });
          return res.end();
        }
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
