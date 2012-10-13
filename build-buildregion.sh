#!/bin/bash
set -e

# replace with version number if creating a release
# don't forget to update getVersion method and .info files
JAR=$PWD/BuildRegion-SNAPSHOT.zip

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

#jar cfv $JAR ./
zip -r $JAR *
