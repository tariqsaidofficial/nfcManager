#!/bin/bash

# NFC Manager - Release Keystore Generator
# This script creates a production-ready keystore for Google Play publishing

set -e

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║        🔐 NFC Manager - Release Keystore Generator         ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Configuration
KEYSTORE_FILE="nfcmanager-release.keystore"
KEY_ALIAS="nfcmanager-key"
VALIDITY_DAYS=10000  # ~27 years (Google Play requirement)

# Default values (can be customized)
CN="NFC Manager"
OU="Development"
O="DXBMark"
L="Dubai"
S="Dubai"
C="AE"

# Generate strong random password
STORE_PASSWORD=$(openssl rand -base64 24 | tr -d "=+/" | cut -c1-20)
KEY_PASSWORD=$(openssl rand -base64 24 | tr -d "=+/" | cut -c1-20)

echo "📋 Keystore Configuration:"
echo "   File: $KEYSTORE_FILE"
echo "   Alias: $KEY_ALIAS"
echo "   Validity: $VALIDITY_DAYS days (~27 years)"
echo "   Organization: $O"
echo "   Location: $L, $S, $C"
echo ""

# Check if keystore already exists
if [ -f "$KEYSTORE_FILE" ]; then
    echo "⚠️  Keystore already exists!"
    echo "   Location: $(pwd)/$KEYSTORE_FILE"
    read -p "   Do you want to overwrite it? (yes/no): " confirm
    if [ "$confirm" != "yes" ]; then
        echo "❌ Cancelled. Existing keystore preserved."
        exit 1
    fi
    rm -f "$KEYSTORE_FILE"
fi

echo "🔨 Generating keystore..."
echo ""

# Generate keystore using keytool
keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_FILE" \
    -alias "$KEY_ALIAS" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $VALIDITY_DAYS \
    -storepass "$STORE_PASSWORD" \
    -keypass "$KEY_PASSWORD" \
    -dname "CN=$CN, OU=$OU, O=$O, L=$L, S=$S, C=$C"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Keystore created successfully!"
    echo ""
    
    # Create gradle.properties with credentials
    GRADLE_PROPS="gradle.properties"
    
    echo "📝 Creating $GRADLE_PROPS..."
    cat > "$GRADLE_PROPS" << EOF
# NFC Manager - Release Signing Configuration
# ⚠️  KEEP THIS FILE SECURE - DO NOT COMMIT TO GIT

# Keystore configuration
MYAPP_UPLOAD_STORE_FILE=$KEYSTORE_FILE
MYAPP_UPLOAD_KEY_ALIAS=$KEY_ALIAS
MYAPP_UPLOAD_STORE_PASSWORD=$STORE_PASSWORD
MYAPP_UPLOAD_KEY_PASSWORD=$KEY_PASSWORD

# Android build configuration
android.useAndroidX=true
android.enableJetifier=true
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
kotlin.code.style=official
EOF
    
    echo "✅ $GRADLE_PROPS created!"
    echo ""
    
    # Create credentials backup file
    CREDS_FILE="KEYSTORE_CREDENTIALS.txt"
    cat > "$CREDS_FILE" << EOF
╔══════════════════════════════════════════════════════════════╗
║        NFC Manager - Keystore Credentials (CONFIDENTIAL)    ║
╚══════════════════════════════════════════════════════════════╝

⚠️  CRITICAL: Keep this file in a secure location!
⚠️  DO NOT share these credentials publicly
⚠️  DO NOT commit this file to version control

Keystore Information:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
File Name:       $KEYSTORE_FILE
Key Alias:       $KEY_ALIAS
Store Password:  $STORE_PASSWORD
Key Password:    $KEY_PASSWORD
Validity:        $VALIDITY_DAYS days (~27 years)

Distinguished Name (DN):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
CN (Common Name):        $CN
OU (Organizational Unit): $OU
O (Organization):        $O
L (Locality/City):       $L
S (State/Province):      $S
C (Country Code):        $C

Important Notes:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. Store this file in a secure password manager
2. Keep a backup of the keystore file in a safe location
3. If you lose the keystore, you cannot update your app on Google Play
4. Google Play requires the same keystore for all app updates

Created: $(date)
EOF
    
    echo "✅ Credentials saved to: $CREDS_FILE"
    echo ""
    
    # Update .gitignore
    if ! grep -q "$KEYSTORE_FILE" .gitignore 2>/dev/null; then
        echo "" >> .gitignore
        echo "# Release keystore and credentials" >> .gitignore
        echo "$KEYSTORE_FILE" >> .gitignore
        echo "$CREDS_FILE" >> .gitignore
        echo "gradle.properties" >> .gitignore
        echo "✅ Updated .gitignore"
    fi
    
    echo ""
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                    🎉 SUCCESS!                              ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo ""
    echo "📦 Files created:"
    echo "   ✅ $KEYSTORE_FILE (Keystore file)"
    echo "   ✅ $GRADLE_PROPS (Build configuration)"
    echo "   ✅ $CREDS_FILE (Credentials backup)"
    echo ""
    echo "🔒 Security checklist:"
    echo "   ✅ Keystore created with strong passwords"
    echo "   ✅ Credentials saved securely"
    echo "   ✅ .gitignore updated"
    echo ""
    echo "🚀 Next steps:"
    echo "   1. Backup $KEYSTORE_FILE to a secure location"
    echo "   2. Save $CREDS_FILE to your password manager"
    echo "   3. Run: ./gradlew bundleRelease"
    echo ""
    
else
    echo "❌ Failed to create keystore"
    exit 1
fi
