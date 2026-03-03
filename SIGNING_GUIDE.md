# APK Signing Configuration Guide

## Overview
NovelDokusha uses APK signing for release builds. This guide explains how to configure signing for both local development and CI/CD.

---

## 🔐 Signing Files

### Production Keystore
- **File:** `HarzBaiQ.jks`
- **Location:** Project root (gitignored)
- **Type:** JKS (Java KeyStore)

### Local Development
- **File:** `key.properties`
- **Location:** Project root (gitignored)
- **Purpose:** Local signing configuration

### CI/CD (GitHub Actions)
- **File:** `custom.properties` (generated during build)
- **Location:** Project root (temporary)
- **Purpose:** CI signing configuration from secrets

---

## 📝 Local Development Setup

### Step 1: Create key.properties

Create a file named `key.properties` in the project root:

```properties
storeFile=HarzBaiQ.jks
storePassword=YOUR_KEYSTORE_PASSWORD
keyAlias=harzbaiq
keyPassword=YOUR_KEY_PASSWORD
```

### Step 2: Build Signed APK

```bash
./gradlew assembleRelease -PlocalPropertiesFilePath=key.properties
```

### Step 3: Verify APK

```bash
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

---

## 🚀 CI/CD Setup (GitHub Actions)

### Required Secrets

Add these secrets to your GitHub repository:

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `SIGNING_KEY` | Base64 encoded keystore file | `LS0tLS1CRUdJTi...` |
| `KEY_STORE_PASSWORD` | Keystore password | `your_password` |
| `KEY_PASSWORD` | Key password | `your_password` |
| `ALIAS` | Key alias | `harzbaiq` |

### How to Get Base64 Encoded Keystore

```bash
# On your local machine
base64 -w 0 HarzBaiQ.jks | pbcopy  # macOS
# or
base64 -w 0 HarzBaiQ.jks | xclip -selection clipboard  # Linux
```

Then paste the output into GitHub Secrets as `SIGNING_KEY`.

---

## 📋 Workflow Process

### GitHub Actions Build:

1. **Checkout code**
   ```yaml
   uses: actions/checkout@v4
   ```

2. **Setup Java**
   ```yaml
   uses: actions/setup-java@v4
   with:
     java-version: 17
   ```

3. **Decode Keystore**
   ```bash
   echo -n $SIGNING_KEY | base64 -d > HarzBaiQ.jks
   ```

4. **Create Signing Properties**
   ```bash
   echo storeFile=HarzBaiQ.jks > custom.properties
   echo storePassword=$KEY_STORE_PASSWORD >> custom.properties
   echo keyAlias=$ALIAS >> custom.properties
   echo keyPassword=$KEY_PASSWORD >> custom.properties
   ```

5. **Build Signed APK**
   ```bash
   ./gradlew assembleRelease -PlocalPropertiesFilePath=custom.properties
   ```

6. **Upload Release**
   - APK is uploaded to GitHub Release
   - Build logs are saved as artifacts

---

## 🔍 Verification

### Check Signing Configuration

```bash
# View current signing config
cat key.properties

# Verify keystore exists
ls -la HarzBaiQ.jks

# Check keystore info
keytool -list -v -keystore HarzBaiQ.jks -alias harzbaiq
```

### Verify Signed APK

```bash
# Using apksigner
apksigner verify --verbose app-release.apk

# Using jarsigner
jarsigner -verify -verbose -certs app-release.apk
```

---

## ⚠️ Security Best Practices

1. **Never commit** keystore files to git
2. **Never commit** `key.properties` to git
3. **Use environment variables** or secrets for passwords
4. **Backup your keystore** securely (can't recover if lost!)
5. **Use strong passwords** (minimum 12 characters)

---

## 📁 Git Ignore Configuration

Ensure these files are in `.gitignore`:

```gitignore
# Signing files
*.jks
*.keystore
key.properties
custom.properties
storeFile.jks

# Build outputs
build/
*.apk
*.aab
```

---

## 🛠️ Troubleshooting

### Error: "Keystore file not found"
**Solution:** Check that `HarzBaiQ.jks` exists in project root

### Error: "Invalid keystore password"
**Solution:** Verify passwords in `key.properties` match your keystore

### Error: "Key alias not found"
**Solution:** Check alias name with:
```bash
keytool -list -keystore HarzBaiQ.jks
```

### Build succeeds but APK not signed
**Solution:** Check `app/build.gradle.kts` signingConfigs section

---

## 📞 Support

If you encounter issues:
1. Check build logs for specific errors
2. Verify all secrets are correctly set
3. Test locally with `key.properties` first
4. Ensure keystore file is valid JKS format

---

**Last Updated:** 2025-03-03
**Version:** 2.3.9
