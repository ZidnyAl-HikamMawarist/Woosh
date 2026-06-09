# Build and Deployment Guide

## Overview

Panduan lengkap untuk build mobile app, deploy Laravel backend, dan setup Firebase untuk production.

---

## 📱 MOBILE APP BUILD

### Prerequisites

- Android Studio installed
- JDK 11+ installed
- Android SDK 31+ installed
- Gradle 7.0+ installed

### Build Steps

#### Step 1: Clean Build

```bash
cd Woosh
./gradlew clean
```

**What it does**: Removes all previous build artifacts and caches.

#### Step 2: Build Debug APK

```bash
./gradlew assembleDebug
```

**Output**: `app/build/outputs/apk/debug/app-debug.apk`

**Use for**: Development and testing

#### Step 3: Build Release APK

```bash
./gradlew assembleRelease
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

**Use for**: Production deployment

**Note**: Requires signing configuration in `app/build.gradle.kts`

### Signing Configuration (For Release Build)

**File**: `app/build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/keystore.jks")
            storePassword = "your-store-password"
            keyAlias = "your-key-alias"
            keyPassword = "your-key-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### Install on Device/Emulator

```bash
# Debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Release APK
adb install app/build/outputs/apk/release/app-release.apk
```

### Verify Installation

```bash
# Check if app is installed
adb shell pm list packages | grep woosh

# Launch app
adb shell am start -n com.example.woosh/.MainActivity

# View logs
adb logcat | grep woosh
```

---

## 🖥️ LARAVEL BACKEND DEPLOYMENT

### Prerequisites

- PHP 8.1+ installed
- Composer installed
- MySQL 5.7+ installed
- Git installed

### Local Development Setup

#### Step 1: Clone Repository

```bash
cd admin-woosh
git clone <repo-url> .
```

#### Step 2: Install Dependencies

```bash
composer install
npm install
```

#### Step 3: Environment Configuration

```bash
cp .env.example .env
php artisan key:generate
```

**Edit `.env`**:
```env
APP_NAME=Woosh
APP_ENV=local
APP_DEBUG=true
APP_URL=http://localhost:8000

DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=woosh
DB_USERNAME=root
DB_PASSWORD=

FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CREDENTIALS=storage/app/firebase-auth.json
```

#### Step 4: Database Setup

```bash
# Create database
mysql -u root -e "CREATE DATABASE woosh;"

# Run migrations
php artisan migrate

# Seed data (optional)
php artisan db:seed
```

#### Step 5: Firebase Setup

1. Download service account JSON from Firebase Console
2. Place in `storage/app/firebase-auth.json`
3. Update `.env` with project ID

#### Step 6: Start Development Server

```bash
php artisan serve --host=0.0.0.0 --port=8000
```

**Access**: `http://localhost:8000`

### Production Deployment

#### Step 1: Prepare Server

```bash
# SSH into server
ssh user@your-server.com

# Create app directory
mkdir -p /var/www/woosh
cd /var/www/woosh

# Clone repository
git clone <repo-url> .
```

#### Step 2: Install Dependencies

```bash
composer install --no-dev --optimize-autoloader
npm install --production
```

#### Step 3: Configure Environment

```bash
cp .env.example .env

# Edit .env for production
nano .env
```

**Production `.env` settings**:
```env
APP_ENV=production
APP_DEBUG=false
APP_URL=https://your-domain.com

DB_CONNECTION=mysql
DB_HOST=your-db-host
DB_DATABASE=woosh_prod
DB_USERNAME=woosh_user
DB_PASSWORD=strong-password

FIREBASE_PROJECT_ID=your-project-id
FIREBASE_CREDENTIALS=/var/www/woosh/storage/app/firebase-auth.json
```

#### Step 4: Setup Database

```bash
# Create database
mysql -u root -p -e "CREATE DATABASE woosh_prod;"

# Run migrations
php artisan migrate --force

# Seed data
php artisan db:seed --force
```

#### Step 5: Setup Web Server (Nginx)

**File**: `/etc/nginx/sites-available/woosh`

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/woosh/public;

    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    index index.php;

    charset utf-8;

    location / {
        try_files $uri $uri/ /index.php?$query_string;
    }

    location = /favicon.ico { access_log off; log_not_found off; }
    location = /robots.txt  { access_log off; log_not_found off; }

    error_page 404 /index.php;

    location ~ \.php$ {
        fastcgi_pass unix:/var/run/php/php8.1-fpm.sock;
        fastcgi_param SCRIPT_FILENAME $realpath_root$fastcgi_script_name;
        include fastcgi_params;
    }

    location ~ /\.(?!well-known).* {
        deny all;
    }
}
```

**Enable site**:
```bash
sudo ln -s /etc/nginx/sites-available/woosh /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

#### Step 6: Setup SSL (Let's Encrypt)

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

#### Step 7: Setup Cron Jobs

```bash
# Edit crontab
crontab -e

# Add Laravel scheduler
* * * * * cd /var/www/woosh && php artisan schedule:run >> /dev/null 2>&1
```

#### Step 8: Setup Supervisor (For Queue)

**File**: `/etc/supervisor/conf.d/woosh.conf`

```ini
[program:woosh-queue]
process_name=%(program_name)s_%(process_num)02d
command=php /var/www/woosh/artisan queue:work --sleep=3 --tries=3
autostart=true
autorestart=true
numprocs=4
redirect_stderr=true
stdout_logfile=/var/www/woosh/storage/logs/queue.log
```

**Start supervisor**:
```bash
sudo supervisorctl reread
sudo supervisorctl update
sudo supervisorctl start woosh-queue:*
```

#### Step 9: Setup Monitoring

```bash
# Check Laravel logs
tail -f /var/www/woosh/storage/logs/laravel.log

# Check Nginx logs
tail -f /var/log/nginx/error.log
tail -f /var/log/nginx/access.log
```

---

## 🔥 FIREBASE SETUP

### Prerequisites

- Firebase project created
- Service account JSON downloaded
- Firestore database created

### Firestore Rules

**File**: `firestore.rules`

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;
      
      // Tickets subcollection
      match /tickets/{ticketId} {
        allow read, write: if request.auth.uid == uid;
      }
      
      // Notifications subcollection
      match /notifications/{notifId} {
        allow read: if request.auth.uid == uid;
        allow write: if request.auth.token.admin == true;
      }
      
      // Point history subcollection
      match /point_history/{historyId} {
        allow read: if request.auth.uid == uid;
        allow write: if request.auth.token.admin == true;
      }
    }
    
    // Trips collection (public read, admin write)
    match /trips/{tripId} {
      allow read: if true;
      allow write: if request.auth.token.admin == true;
    }
  }
}
```

**Deploy rules**:
```bash
firebase deploy --only firestore:rules
```

### Firebase Authentication

1. Go to Firebase Console
2. Enable Authentication
3. Enable Email/Password provider
4. Enable Google provider (optional)
5. Configure sign-in methods

### Firebase Cloud Messaging (FCM)

1. Go to Firebase Console
2. Enable Cloud Messaging
3. Get Server API Key
4. Add to Laravel `.env`:
   ```env
   FIREBASE_FCM_KEY=your-server-api-key
   ```

### Firebase Storage (Optional)

1. Go to Firebase Console
2. Enable Cloud Storage
3. Create bucket
4. Configure security rules

---

## 🧪 TESTING BEFORE DEPLOYMENT

### Unit Tests

```bash
# Run tests
php artisan test

# Run specific test
php artisan test tests/Feature/ApiTest.php

# Run with coverage
php artisan test --coverage
```

### Integration Tests

```bash
# Test API endpoints
php artisan test tests/Feature/ApiControllerTest.php

# Test Firebase sync
php artisan test tests/Feature/FirebaseSyncTest.php
```

### Manual Testing

```bash
# Test user registration
curl -X POST http://localhost:8000/api/v1/sync-user \
  -d "firebase_uid=test&name=Test&email=test@example.com&phone=08123456789"

# Test trip listing
curl http://localhost:8000/api/v1/trips

# Test ticket booking
curl -X POST http://localhost:8000/api/v1/book-ticket \
  -d "firebase_uid=test&trip_id=1&seats=A1,A2&total_price=500000"
```

---

## 📊 DEPLOYMENT CHECKLIST

### Pre-Deployment

- [ ] All code committed to git
- [ ] All tests passing
- [ ] Database migrations tested
- [ ] Firebase rules deployed
- [ ] Environment variables configured
- [ ] SSL certificate ready
- [ ] Backups created
- [ ] Monitoring setup

### Deployment Day

- [ ] Deploy Laravel backend
- [ ] Run database migrations
- [ ] Deploy Firebase rules
- [ ] Build and release Android app
- [ ] Test all critical flows
- [ ] Monitor logs for errors
- [ ] Notify users

### Post-Deployment

- [ ] Monitor error logs
- [ ] Monitor performance metrics
- [ ] Collect user feedback
- [ ] Fix critical issues
- [ ] Plan next release

---

## 🚨 ROLLBACK PROCEDURE

### If Something Goes Wrong

#### Rollback Laravel

```bash
# Revert to previous commit
git revert HEAD

# Or reset to specific commit
git reset --hard <commit-hash>

# Restart services
sudo systemctl restart nginx
sudo systemctl restart php8.1-fpm
```

#### Rollback Database

```bash
# Rollback migrations
php artisan migrate:rollback

# Or rollback specific migration
php artisan migrate:rollback --step=1
```

#### Rollback Mobile App

```bash
# Uninstall current version
adb uninstall com.example.woosh

# Install previous version
adb install app/build/outputs/apk/release/app-release-v1.0.apk
```

---

## 📈 MONITORING & MAINTENANCE

### Daily Checks

```bash
# Check Laravel logs
tail -f /var/www/woosh/storage/logs/laravel.log

# Check Nginx logs
tail -f /var/log/nginx/error.log

# Check database
mysql -u root -p -e "SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM tickets;"

# Check Firestore
# Firebase Console → Firestore → Monitor
```

### Weekly Maintenance

```bash
# Clear cache
php artisan cache:clear

# Clear logs
php artisan logs:clear

# Backup database
mysqldump -u root -p woosh_prod > backup-$(date +%Y%m%d).sql

# Backup Firestore
firebase firestore:export gs://your-bucket/backup-$(date +%Y%m%d)
```

### Monthly Review

- Review error logs
- Analyze performance metrics
- Plan optimizations
- Update dependencies
- Security audit

---

## 🔐 SECURITY CHECKLIST

### Before Production

- [ ] HTTPS enabled
- [ ] Firestore rules set to production
- [ ] API rate limiting enabled
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention (use parameterized queries)
- [ ] CSRF protection enabled
- [ ] CORS properly configured
- [ ] Secrets not in code
- [ ] Database backups automated
- [ ] Monitoring and alerting setup

### Ongoing

- [ ] Regular security updates
- [ ] Dependency scanning
- [ ] Log monitoring
- [ ] Access control review
- [ ] Incident response plan

---

## 📞 TROUBLESHOOTING

### Build Fails

```bash
# Clean and rebuild
./gradlew clean build

# Check Gradle version
./gradlew --version

# Check Java version
java -version

# Update Gradle
./gradlew wrapper --gradle-version 7.6
```

### Database Connection Error

```bash
# Check MySQL is running
mysql -u root -p -e "SELECT 1;"

# Check credentials in .env
cat .env | grep DB_

# Test connection
php artisan tinker
>>> DB::connection()->getPdo();
```

### Firebase Connection Error

```bash
# Check credentials file exists
ls -la storage/app/firebase-auth.json

# Check project ID
cat storage/app/firebase-auth.json | grep project_id

# Test connection
php artisan tinker
>>> app('firebase.firestore')->collection('trips')->count();
```

### API Not Responding

```bash
# Check Laravel is running
ps aux | grep "php artisan serve"

# Check port 8000 is open
netstat -tlnp | grep 8000

# Restart Laravel
php artisan serve --host=0.0.0.0 --port=8000
```

---

## 📚 USEFUL COMMANDS

### Laravel

```bash
# Start development server
php artisan serve --host=0.0.0.0 --port=8000

# Run migrations
php artisan migrate

# Rollback migrations
php artisan migrate:rollback

# Clear cache
php artisan cache:clear

# Clear logs
php artisan logs:clear

# Run tests
php artisan test

# Generate API documentation
php artisan scribe:generate
```

### Android

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat | grep woosh

# Clear app data
adb shell pm clear com.example.woosh
```

### Firebase

```bash
# Deploy rules
firebase deploy --only firestore:rules

# Export data
firebase firestore:export gs://bucket/backup

# Import data
firebase firestore:import gs://bucket/backup

# View logs
firebase functions:log
```

### MySQL

```bash
# Connect to database
mysql -u root -p

# Show databases
SHOW DATABASES;

# Use database
USE woosh;

# Show tables
SHOW TABLES;

# Backup database
mysqldump -u root -p woosh > backup.sql

# Restore database
mysql -u root -p woosh < backup.sql
```

---

## ✨ SUMMARY

✅ Mobile app build process documented  
✅ Laravel deployment process documented  
✅ Firebase setup documented  
✅ Testing procedures documented  
✅ Monitoring and maintenance documented  
✅ Troubleshooting guide provided  

**Next Steps**:
1. Follow build steps for mobile app
2. Follow deployment steps for Laravel
3. Setup Firebase rules
4. Run all tests
5. Deploy to production
6. Monitor and maintain

---

**Last Updated**: May 26, 2026  
**Version**: 1.0  
**Status**: Ready for Production

