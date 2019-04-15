#!/bin/bash

JNAERATOR_JAR = ../external/JNAerator/jnaerator/target/jnaerator-0.13-SNAPSHOT-shaded.jar
FL_INC = ../../external/fluidsynth/include/fluidsynth
ANDROID_NDK = ~/android-sdk-linux/ndk-bundle
FL_ANDROID = ../external/fluidsynth/doc/android

SF3 = src/main/assets/FluidR3Mono_GM.sf3

all: libs/fluidsynth.jar libs/fluidsynth-assetloader.jar fluidsynth

.PHONY: prepare-fluidsynth
prepare-fluidsynth:
	cd $(FL_ANDROID) && make -f Makefile.android prepare

$(JNAERATOR_JAR): jnaerator.stamp
	cd ../external/JNAerator && mvn clean install && cd ../..

jnaerator.stamp:
	touch jnaerator.stamp

libs/fluidsynth.jar: $(JNAERATOR_JAR)
	mkdir -p libs && cd libs && \
	java -jar ../$(JNAERATOR_JAR) \
	-I ../../external/fluidsynth/include/ \
	-I $(ANDROID_NDK)/usr/include/ \
	-library fluidsynth \
	-mode Jar \
	-runtime JNA \
	-f -v \
	-skipDeprecated \
	$(FL_INC)/synth.h \
	$(FL_INC)/sfont.h \
	$(FL_INC)/audio.h \
	$(FL_INC)/midi.h \
	$(FL_INC)/voice.h \
	$(FL_INC)/event.h \
	$(FL_INC)/gen.h \
	$(FL_INC)/ladspa.h \
	$(FL_INC)/log.h \
	$(FL_INC)/misc.h \
	$(FL_INC)/mod.h \
	$(FL_INC)/seqbind.h \
	$(FL_INC)/settings.h \
	$(FL_INC)/shell.h \
	$(FL_INC)/types.h \
	&& \
	cd ..

libs/fluidsynth-assetloader.jar: $(JNAERATOR_JAR)
	mkdir -p libs && cd libs && \
	java -jar ../$(JNAERATOR_JAR) \
	-I ../../external/fluidsynth/include/ \
	-I $(ANDROID_NDK)/usr/include/ \
	-library fluidsynth-assetloader \
	-mode Jar \
	-runtime JNA \
	-f \
	-skipDeprecated \
	-noLibBundle \
	../$(FL_ANDROID)/fluidsynth-assetloader/fluid_androidasset.h \
	&& \
	cd ..

.PHONY: fluidsynth
fluidsynth:
	cd $(FL_ANDROID) && \
	make -f Makefile.android all || echo "ignore this failure..." && \
	make -f Makefile.android dist
	mkdir -p src/main/jniLibs
	cp -R $(FL_ANDROID)/dist/* src/main/jniLibs

clean:
	cd $(FL_ANDROID) && make -f Makefile.android clean
	rm -rf src/main/jniLibs/*/*.so libs/*.jar libs/_jnaerator.*

wipe-cerbero:
	cd $(FL_ANDROID) && make -f Makefile.android wipe
