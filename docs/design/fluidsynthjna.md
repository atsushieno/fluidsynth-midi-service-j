# Fluidsynth for Kotlin/JVM

`fluidsynth-kt` is a Kotlin/JVM binding for libfluidsynth API. It is actually an Object-Oriented wrapper of `fluidsynth-jna`.

`fluidsynth-jna` builds upon [JNA](https://github.com/java-native-access/jna) and its build is highly automated by using [JNAerator](https://github.com/nativelibs4java/JNAerator) without any source code modification. The resulting API is just like Fluidsynth C API which is quite inconvenient in Kotlin/JVM land.

It should also be noted that we make use of [opendesignflow/gradle-jnaerator-plugin](https://github.com/opendesignflow/gradle-jnaerator-plugin) which is a modernized fork of [shevek/gradle-jnaerator-plugin](https://github.com/shevek/gradle-jnaerator-plugin).

fluidsynth-kt may be still usable in Java language, but I don't plan to support it.

## Using JNAerator in 2021: keeping things alive

JNA is still actively developed and ready for Android. But the original JNAerator development has been completely stopped for >5 years.

There are some people who fork it and try to keep it usable in their own ways. I once actually migrated to such a solution once, when I was still using Maven pom-based solution.

After I migrated to Gradle JNAerator plugin, my use of JNAerator seems to be based on the original JNAerator maven package from years ago (0.12).

Depending on which build system is used, the resulting code can be quite different. It might be the set of options that runs JNAerator.

Our current build makes use of Gradle JNAerator Plugin with not particular option.

## Gradle JNAerator Plugin instead of official maven setup

On Android projects everything is based on Gradle in 2021. Maven is used as a package management system, not the name of the tool to directly deal with.

[shevek/gradle-jnaerator-plugin](https://github.com/shevek/gradle-jnaerator-plugin) looked like a perfect solution to those who want to automate JNAerator invocation within Android project. Sadly it was quite outdated. Fortunately a fork at [opendesignflow/gradle-jnaerator-plugin](https://github.com/opendesignflow/gradle-jnaerator-plugin) is doing things perfectly. Not on Maven Central, but we could add a custom maven URL for its sonatype repo.

## jna-runtime and jna-runtime@aar: resolving Reference conflicts

jnaerator comes with `jnaerator-runtime` package. This makes things complicated, because it has `jna-5.6.0-runtime` as a dependency. JNA supports Android in that it also offers AAR package, and we have to use it because JNA itself requires its native glue library which is platform/ABI specific. In fluidsynth-kt `build.gradle`, we specify this dependency as:

```
    implementation 'net.java.dev.jna:jna:5.6.0@aar'
```

Note the last `@aar` part. This is basically a different entity from the non-AAR (JAR) version of the library.

What if we keep `jnaerator-runtime` reference? It results in:

```
> A failure occurred while executing com.android.build.gradle.internal.tasks.CheckDuplicatesRunnable
   > Duplicate class com.sun.jna.AltCallingConvention found in modules jna-5.6.0 (net.java.dev.jna:jna:5.6.0) and jna-5.6.0-runtime (net.java.dev.jna:jna:5.6.0)
     Duplicate class com.sun.jna.Callback found in modules jna-5.6.0 (net.java.dev.jna:jna:5.6.0) and jna-5.6.0-runtime (net.java.dev.jna:jna:5.6.0)
     Duplicate class com.sun.jna.Callback$UncaughtExceptionHandler found in modules jna-5.6.0 (net.java.dev.jna:jna:5.6.0) and jna-5.6.0-runtime (net.java.dev.jna:jna:5.6.0)
     ...
```

This does not work. To resolve the problem we either have to build JNAerator runtime for ourselves (which was what we were doing with Maven earlier), or we make it to not depend on it.

Fortunately we don't have to have it as a dependency in the current codebase. I assume that it is because our generated code does not involve runtime-dependent strongly-typed code generation.

## Technical alternatives

At the first stage, there was no particular reason on why I chose JNA. But since JNAerator development has stopped, I was looking for better alternatives

- JavaCPP: it was one of the most probable alternative (actually I cannot think of any other viable alternative) for us. JavaCPP is still actively developed, and it tries to solve the problem in simpler way, generates C glue code that brings in trade off for per-ABI build complexity but makes everything simpler and customizible enough. I actually [tried to migrate to JavaCPP](https://github.com/atsushieno/fluidsynth-midi-service-j/issues/20#issuecomment-862865892). What made me stuck was that it resulted in weird `"missing <jni.h>"`. It should also be noted that in JavaCPP approach we are more probable to need bindings for dependencies as well e.g. bindings to POSIX C API (I do so in my [alsakt project](https://github.com/atsushieno/alsakt)). For fluidsynth I ended up to use JNA(erator) solution.
- JNR: No go. The developers hate to support Android. See how they closed the unresolved issues.
  https://github.com/jnr/jnr-ffi/issues/37
  https://github.com/jnr/jnr-ffi/issues/100
  https://github.com/jnr/jnr-ffi/issues/114
  https://github.com/jnr/jnr-ffi/issues/159
  https://github.com/jnr/jnr-ffi/issues/173
  https://github.com/jnr/jffi/issues/17
  https://github.com/jnr/jffi/issues/57

