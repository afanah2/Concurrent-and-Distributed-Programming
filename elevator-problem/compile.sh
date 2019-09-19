#!/bin/sh

[ -d build ] || mkdir -p build
javac -cp src:$CLASSPATH -d build src/ca4006/Main.java
