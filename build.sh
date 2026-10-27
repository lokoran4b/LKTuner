#!/usr/bin/env bash
set -e

# Detectar Android SDK automáticamente
if [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
    SDK_DIR="$ANDROID_HOME"
elif [ -n "$ANDROID_SDK_ROOT" ] && [ -d "$ANDROID_SDK_ROOT" ]; then
    SDK_DIR="$ANDROID_SDK_ROOT"
elif [ -d "/home/lokoran/Antigravity/android-sdk" ]; then
    SDK_DIR="/home/lokoran/Antigravity/android-sdk"
else
    echo "Error: Android SDK no encontrado en \$ANDROID_HOME o /home/lokoran/Antigravity/android-sdk"
    exit 1
fi

BUILD_TOOLS=$(find "$SDK_DIR/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -n 1)
PLATFORM=$(find "$SDK_DIR/platforms" -name "android.jar" | sort -V | tail -n 1)

if [ -z "$BUILD_TOOLS" ] || [ -z "$PLATFORM" ]; then
    echo "Error: No se encontraron build-tools o platforms en $SDK_DIR"
    exit 1
fi

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$PROJECT_DIR/build"

echo "=== Entorno de compilación ==="
echo "SDK: $SDK_DIR"
echo "Build-tools: $BUILD_TOOLS"
echo "Platform: $PLATFORM"
echo "Proyecto: $PROJECT_DIR"

echo "=== 1. Limpiando y creando directorios de compilación ==="
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/compiled_res" "$BUILD_DIR/gen" "$BUILD_DIR/classes" "$BUILD_DIR/dex"

echo "=== 2. Compilando recursos con aapt2 ==="
"$BUILD_TOOLS/aapt2" compile --dir "$PROJECT_DIR/app/src/main/res" -o "$BUILD_DIR/compiled_res/resources.zip"

echo "=== 3. Enlazando recursos y generando R.java con aapt2 ==="
"$BUILD_TOOLS/aapt2" link "$BUILD_DIR/compiled_res/resources.zip" \
    -I "$PLATFORM" \
    --manifest "$PROJECT_DIR/app/src/main/AndroidManifest.xml" \
    --min-sdk-version 26 \
    --target-sdk-version 34 \
    --java "$BUILD_DIR/gen" \
    -o "$BUILD_DIR/app-unaligned.apk" \
    --auto-add-overlay

echo "=== 4. Compilando fuentes Java con javac ==="
find "$PROJECT_DIR/app/src/main/java" "$BUILD_DIR/gen" -name "*.java" > "$BUILD_DIR/sources.txt"
javac -encoding UTF-8 \
    --release 8 \
    -cp "$PLATFORM" \
    -d "$BUILD_DIR/classes" \
    @"$BUILD_DIR/sources.txt"

echo "=== 5. Convirtiendo bytecode a DEX con d8 ==="
find "$BUILD_DIR/classes" -name "*.class" > "$BUILD_DIR/classes.txt"
"$BUILD_TOOLS/d8" --min-api 26 \
    --lib "$PLATFORM" \
    --output "$BUILD_DIR/dex" \
    @"$BUILD_DIR/classes.txt"

echo "=== 6. Empaquetando classes.dex en el APK ==="
cd "$BUILD_DIR/dex"
zip -u "$BUILD_DIR/app-unaligned.apk" classes.dex
cd "$PROJECT_DIR"

echo "=== 7. Alineando APK con zipalign ==="
"$BUILD_TOOLS/zipalign" -p -f 4 "$BUILD_DIR/app-unaligned.apk" "$BUILD_DIR/LKTuner-aligned.apk"

echo "=== 8. Comprobando y generando keystore de depuración si es necesario ==="
KEYSTORE="$HOME/.android/debug.keystore"
if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$HOME/.android"
    keytool -genkey -v -keystore "$KEYSTORE" -storepass android \
        -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 \
        -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
fi

echo "=== 9. Firmando APK con apksigner ==="
"$BUILD_TOOLS/apksigner" sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --out "$PROJECT_DIR/LKTuner.apk" \
    "$BUILD_DIR/LKTuner-aligned.apk"

echo "=== 10. Verificando firma del APK ==="
"$BUILD_TOOLS/apksigner" verify "$PROJECT_DIR/LKTuner.apk"

echo "✅ Compilación exitosa: $PROJECT_DIR/LKTuner.apk"
