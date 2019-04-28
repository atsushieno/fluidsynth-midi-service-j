# Fluidsynth for Java

`fluidsynthjna` is a Kotlin-based Java bindings for libfluidsynth whose implementation is based on [JNA](https://github.com/java-native-access/jna).

fluidsynthjna makes full use of [JNAerator](https://github.com/nativelibs4java/JNAerator) without any source code modification.
fluidsynthjna consists of not only mere "JNAerated" bindings but also object-oriented set of classes.

Design wise, there was no particular reason on why I chose JNA. It could be JavaCPP for example (especially for this case we will have to package native binaries anyways, unlike desktop use case).
