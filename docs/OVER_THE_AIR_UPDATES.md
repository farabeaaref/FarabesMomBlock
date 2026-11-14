# Over-The-Air (OTA) Updates Guide for Mom's App ❤️

## How It Works

**FARABE'S MOM – BLOCK** now includes an automatic in-app updater.
When Mom opens the game on her phone, it automatically checks your update manifest online. If a new version exists, Mom receives a warm Bengali prompt:
> **"আম্মু, নতুন সারপ্রাইজ এসেছে! ❤️"**  
> *"ফারাবী তোমার জন্য নতুন গান ও সুন্দর ফিচার যোগ করেছে। এখনই আপডেট করে নাও!"*  
> **[এখনই আপডেট / Update Now]**

When she taps the button:
1. The app downloads the new APK with an animated progress bar.
2. Android's native installer opens automatically.
3. Mom taps **"Update"**.
4. **All her game scores, unlocked flowers, garden progress, and letters remain 100% saved!**

---

## How to Release an Update in 3 Simple Steps

### Step 1: Add your new music, photos, or features
- Add your new songs to `app/src/main/assets/custom_music/`.
- In `app/build.gradle.kts`, bump the version numbers:
  ```kotlin
  versionCode = 2  // increment by 1
  versionName = "1.1.0"
  ```

### Step 2: Build the new Release APK
Run in terminal:
```powershell
.\gradlew.bat assembleRelease
```
Your compiled APK will be at:
`app/build/outputs/apk/release/app-release.apk`

### Step 3: Publish to GitHub Releases (100% Free)
1. Go to your GitHub repository (or create a free public/private repo `FarabesMomBlock`).
2. Create a **New Release** (tag `v1.1.0`).
3. Attach `app-release.apk` to the release.
4. Update `version.json` in your repo:
   ```json
   {
     "versionCode": 2,
     "versionName": "1.1.0",
     "downloadUrl": "https://github.com/<your-username>/FarabesMomBlock/releases/download/v1.1.0/app-release.apk",
     "titleBengali": "আম্মু, নতুন সারপ্রাইজ এসেছে! ❤️",
     "descriptionBengali": "ফারাবী তোমার জন্য নতুন গান ও সুন্দর ফিচার যোগ করেছে। এখনই আপডেট করে নাও!",
     "releaseNotes": "নতুন গান ও ভালোবাসার বার্তা যোগ করা হয়েছে।"
   }
   ```

That's it! Next time Mom opens the game (or taps **Settings ➔ Check for Updates**), she will be notified and can update with 1 tap.
