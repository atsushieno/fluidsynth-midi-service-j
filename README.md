# What is this?

[![fluidsynth-midi-service-j demo](http://img.youtube.com/vi/ZVyDmaV4Ihw/0.jpg)](http://www.youtube.com/watch?v=ZVyDmaV4Ihw "fluidsynth-midi-service-j demo")

It is an Android MIDI device service implementation based on [Fluidsynth software synthesizer](https://github.com/Fluidsynth/fluidsynth/) (@atsushieno is the author of its OpenSLES and Oboe driver). It comes with some sample dogfooding app UI.

Since Android 6.0 (Marshmallow), it started to provide the standard way to
receive and send MIDI messages via USB, BLE or any other means through
[`android.media.midi` API](https://developer.android.com/reference/android/media/midi/package-summary). (Details are described at [Android Open Source Project](https://source.android.com/devices/audio/midi).)

By implementing [`android.media.midi.MidiDeviceService` class](https://developer.android.com/reference/android/media/midi/MidiDeviceService), it is possible
to create a new virtual MIDI device, completely based on software.
With Android audio API, it is also possible to reuse existing software
MIDI synthesizers that runs on Linux (if its audio dependency is isolated).

Fluidsynth is one of such a software synthesizer and is (considerably) easy
to port to Android. It is written in C with a handful of C library dependencies.

(Apart from Android MIDI API, it is also possible to use libfluidsynth as a mere library that is used only for an app, without offering MIDI ports as a service. But now that it is part of the official Fluidsynth build, it's not specific to this project anymore.)


## Compared to other Android ports

There are some people who build Fluidsynth for Android, but what makes
this port special is that it also provides audible driver sources, not just
dummy output. It makes use of [OpenSL ES API](https://developer.android.com/ndk/guides/audio/opensl/) as well as [Oboe Audio API](https://github.com/google/Oboe). Both are now based on Fluidsynth master, as our patches are now merged there.

More background can be found at https://dev.to/atsushieno/fluidsynth-20x-for-android-4j6b

(I have no idea whether other Fluidsynth Android ports not supporting has some special contract with Fluidsynth developers and they don't have to provide sources or not.)


## Demo movie

[demo movie](docs/demo.mp4) (How can I embed a video that can be played on github?)


## Prebuilt binaries

Beta packages are available at [DeployGate](https://dply.me/l0etkk).

We have GitHub Actions setup with artifact setup for `fluidsynth-jna.jar` (mere JNA bindings by JNAerator) and `fluidsynth-kt.aar` (Object-oriented bindings in Kotlin).

## Building

Building the entire app is complicated because it needs to build fluidsynth for Android as well as generating automated Java API binding from libfluidsynth C headers.

These are the basic build steps:

- go to `fluidsynth-kt` directory and run `make fluidsynth`.
- run `./gradlew assembleRelease` (etc.) to build Kotlin app.

You will need make, wget, and Maven (mvn) installed too.


### Dependencies

It depends on our private fork of Fluidsynth which is slightly modified version of the official [fluidsynth github repo](https://github.com/Fluidsynth/fluidsynth). The official sources contain some
Android build scripts, which in turn builds glib-2.0 and co, which are fluidsynth's dependencies.

Our fork enhances MIDI 2.0 support, yet it is not very different from the original so far.

(Historically we used GStreamer/Cerbero build system, but as of 2021 we do not need it anymore.)

It bundles `FluidR3Mono_GM.sf3` which is bundled into apk as Android assets. It is released under MIT license (see the source directory).

For comsumption in Kotlin/JVM-based Android application, we use [JNA](https://github.com/java-native-access/jna) and [JNAerator](https://github.com/nativelibs4java/JNAerator) to provide the Java binding for libfluidsynth API. JNAerator has not been maintained upstream to accept newer versions, but we use some derived works as well as `gradle-jnaerator-plugin` instead of the original Maven pom version.

The rest of the Java application is written in Kotlin.


## Scope of the project

This application is built as a proof-of-concept and dogfooding for fluidsynth Android audio drivers. It wouldn't bring best user experiences and features. For example, the MIDI player is based on Kotlin which is not optimal.
