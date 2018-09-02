#!/bin/bash

JNAERATOR_JAR = ../external/JNAerator/jnaerator/target/jnaerator-0.13-SNAPSHOT-shaded.jar

all: libs/fluidsynth.jar fluidsynth

$(JNAERATOR_JAR): jnaerator.stamp
	cd ../external/JNAerator && mvn clean install && cd ../..

jnaerator.stamp:
	touch jnaerator.stamp

libs/fluidsynth.jar: $(JNAERATOR_JAR)
	java -jar $(JNAERATOR_JAR) \
	-root name.atsushieno \
	-I ../external/fluidsynth/include/ \
	-arch linux_x86 \
	-library fluidsynth \
	-mode StandaloneJar \
	-runtime JNA \
	-skipDeprecated \
	../external/fluidsynth/include/fluidsynth/synth.h \
	../external/fluidsynth/include/fluidsynth/sfont.h \
	../external/fluidsynth/include/fluidsynth/audio.h \
	../external/fluidsynth/include/fluidsynth/midi.h \
	../external/fluidsynth/include/fluidsynth/voice.h \
	../external/fluidsynth/include/fluidsynth/ramsfont.h \
	../external/fluidsynth/include/fluidsynth/gen.h \
	../external/fluidsynth/include/fluidsynth/log.h \
	../external/fluidsynth/include/fluidsynth/misc.h \
	../external/fluidsynth/include/fluidsynth/mod.h \
	../external/fluidsynth/include/fluidsynth/settings.h \
	../external/fluidsynth/include/fluidsynth/types.h \
	#
	cp fluidsynth.jar libs/

fluidsynth:
	cd ../external/fluidsynth && make -f Makefile.android all || echo "ignore this failure..." && make -f Makefile.android dist
	mkdir -p src/main/jniLibs
	cp -R ../external/fluidsynth/dist/android/* src/main/jniLibs

