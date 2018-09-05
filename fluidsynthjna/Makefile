#!/bin/bash

JNAERATOR_JAR = ../external/JNAerator/jnaerator/target/jnaerator-0.13-SNAPSHOT-shaded.jar
FL_INC = ../../external/fluidsynth/include/fluidsynth

all: libs/fluidsynth.jar fluidsynth

$(JNAERATOR_JAR): jnaerator.stamp
	cd ../external/JNAerator && mvn clean install && cd ../..

jnaerator.stamp:
	touch jnaerator.stamp

libs/fluidsynth.jar: $(JNAERATOR_JAR)
	cd libs && \
	java -jar ../$(JNAERATOR_JAR) \
	-root name.atsushieno \
	-I ../../external/fluidsynth/include/ \
	-library fluidsynth \
	-mode Jar \
	-runtime JNA \
	-f \
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

fluidsynth:
	cd ../external/fluidsynth/android && make -f Makefile.android all || echo "ignore this failure..." && make -f Makefile.android dist
	mkdir -p src/main/jniLibs
	cp -R ../external/fluidsynth/android/dist/* src/main/jniLibs

