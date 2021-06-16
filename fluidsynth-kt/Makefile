#!/bin/bash

JNAERATOR_JAR = ../external/JNAerator/jnaerator/target/jnaerator-0.13-shaded.jar
FL_INC = ../../external/fluidsynth/include/fluidsynth
ANDROID_NDK = ~/Android/Sdk/ndk/21.3.6528147
FL_DOC_ANDROID = ../external/fluidsynth/test-android/build-scripts
FL_ANDROID = ../external/fluidsynth/test-android/build-scripts

SF3 = src/main/assets/FluidR3Mono_GM.sf3

all: libs/fluidsynth.jar libs/fluidsynth-assetloader.jar fluidsynth

$(JNAERATOR_JAR): jnaerator.stamp
	cd ../external/JNAerator && mvn clean install -Dmaven.test.skip=true  && cd ../..

jnaerator.stamp:
	touch jnaerator.stamp

libs/fluidsynth.jar: $(JNAERATOR_JAR)
	mkdir -p libs && cd libs && \
	java -Djdk.net.usePlainSocketImpl -jar ../$(JNAERATOR_JAR) \
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
	../$(FL_DOC_ANDROID)/fluidsynth-assetloader/fluid_androidasset.h \
	&& \
	cd ..

.PHONY: fluidsynth
fluidsynth:
	if [ ! -f .download.stamp ] ; then \
		cd $(FL_ANDROID) && bash ./download.sh ; \
		touch .download.stamp ; \
	fi
	cd $(FL_ANDROID) && bash ./build-all-archs.sh
	mkdir -p src/main/jniLibs
	cp -R $(FL_ANDROID)/build-artifacts/lib/* src/main/jniLibs

clean:
	cd $(FL_ANDROID) && rm -rf build-artifacts android-build-root ../../build_*/
	rm -rf src/main/jniLibs/*/*.so libs/*.jar libs/_jnaerator.*

