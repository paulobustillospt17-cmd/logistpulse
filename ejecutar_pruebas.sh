#!/usr/bin/env bash
# Compila y ejecuta todas las pruebas. Requiere JDK 11+.
set -e
cd "$(dirname "$0")"
rm -rf out && javac -encoding UTF-8 -d out src/logistpulse/*.java
java -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dfile.encoding=UTF-8 -cp out logistpulse.Pruebas
