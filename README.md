# What is this?

It is an Android MIDI device service implementation based on [Fluidsynth software synthesizer](https://github.com/Fluidsynth/fluidsynth/).

Since Android 6.0 (Marshmallow), it started to provide the standard way to
receive and send MIDI messages via USB, BLE or any other means through
[`android.media.midi` API](https://developer.android.com/reference/android/media/midi/package-summary). (Details are described at [Android Open Source Project](https://source.android.com/devices/audio/midi).)

By implementing [`android.media.midi.MidiDeviceService` class](https://developer.android.com/reference/android/media/midi/MidiDeviceService), it is possible
to create a new virtual MIDI device, completely based on software.
With Android audio API, it is also possible to reuse existing software
MIDI synthesizers that runs on Linux (if its audio dependency is isolated).

Fluidsynth is one of such a software synthesizer and is (considerably) easy
to port to Android. It is written in C with a handful of C library dependencies.

There are some people who build Fluidsynth for Android, but what makes
this port special is that it also provides audible driver sources, not just
dummy output.

(Currently it is done using [OpenSL ES API](https://developer.android.com/ndk/guides/audio/opensl/). There is an [ongoing effort](https://github.com/atsushieno/fluidsynth-midi-service-j/issues/6) to support [Oboe Audio API](https://github.com/google/Oboe).)

More background can be found at https://dev.to/atsushieno/fluidsynth-20x-for-android-4j6b

## Dependencies

Currently it depends on atsushieno's own fork of fluidsynth which adds some
Android build scripts, which in turn depends on [Cerbero build system](https://cgit.freedesktop.org/gstreamer/cerbero/) to build glib-2.0 and co, one of fluidsynth's dependencies.

The fluidsynth fork branch for OpenSLES support is found at https://github.com/atsushieno/fluidsynth-fork/tree/opensles-v2 .

It also downloads FluidR3_G* soundfonts which is OSS-friendlily released.
Those sf2 files are bundled into apk as Android assets.

For comsumption in Java-based Android application, we use [JNA](https://github.com/java-native-access/jna) and [JNAerator](https://github.com/nativelibs4java/JNAerator) to provide Java binding for libfluidsynth API.

The rest of the Java application is written in Kotlin.

## Prebuilt binaries

Right now there is no application package as the Java application does not
really do anything yet.

To avoid the most difficult part, building libfluidsynth.so with OpenSLES support, we have a set of prebuilt shared libraries so that anyone who just wants to build your own synthesizer apps can reuse it (there was a couple of inquiry and request them in the past):
https://www.dropbox.com/s/081mnfzhgavjb0y/android-fluidsynth-opensles-binaries-9a4c265.zip?dl=0


