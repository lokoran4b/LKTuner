#!/usr/bin/env bash
set -e

SDK_DIR="/home/lokoran/Antigravity/android-sdk"
BUILD_TOOLS="$SDK_DIR/build-tools/34.0.0"
PLATFORM="$SDK_DIR/platforms/android-34/android.jar"
PROJECT_DIR="/home/lokoran/Antigravity/LKTuner"
BUILD_DIR="$PROJECT_DIR/build"

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

echo "=== 8. Firmando APK con apksigner ==="
"$BUILD_TOOLS/apksigner" sign \
    --ks /home/lokoran/.android/debug.keystore \
    --ks-pass pass:android \
    --key-pass pass:android \
    --out "$PROJECT_DIR/LKTuner.apk" \
    "$BUILD_DIR/LKTuner-aligned.apk"

echo "=== 9. Verificando firma del APK ==="
"$BUILD_TOOLS/apksigner" verify "$PROJECT_DIR/LKTuner.apk"

echo "✅ Compilación exitosa: $PROJECT_DIR/LKTuner.apk"
