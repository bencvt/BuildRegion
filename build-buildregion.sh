#!/bin/bash
set -e

# replace with version number if creating a release
# don't forget to update Controller.MOD_VERSION and mcmod.info files
JAR=$PWD/BuildRegion-2.0.2-SNAPSHOT.zip

rm $JAR || true

# ugh, these scripts exit with success status even if they failed...
# as a workaround check their output directories

echo "== Compiling =="
./recompile.sh
[ "$(ls bin/minecraft/net/minecraft/src/)" ] || exit 1

echo "== Reobfuscating =="
./reobfuscate.sh
[ "$(ls reobf/minecraft/)" ] || exit 1

echo "== Packaging $JAR =="
cd reobf/minecraft/
rm -rf META-INF

# resources
cp ../../src/minecraft/*.info .
cp ../../src/minecraft/com/bencvt/minecraft/buildregion/lang/*.properties com/bencvt/minecraft/buildregion/lang/

#jar cfv $JAR ./
zip -r $JAR *
