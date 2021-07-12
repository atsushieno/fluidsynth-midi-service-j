# This document is deprecated

This document describes the original build scripts which were now deprecated in the mainline Fluidsynth. It is left almost as is in this repo for historical record. (It was the biggest part of this project.)

# Problem and solution

To build Fluidsynth for Android, we have to come up with dependencies like glib, and optionally libsndfile if you want SoundFont v3 support, which it is not easy because of various subsequent dependencies. And Fluidsynth itself uses CMake to build itself, which means the dependencies must align with it.

I ended up to use [Cerbero](https://gstreamer.freedesktop.org/documentation/deploying/multiplatform-using-cerbero.html), a GStreamer build system which covers Android.


# Difficulty in building libraries for Android

Building existing linux libraries for Android is in general more painful than building libraries for host linux desktop itself. It is because...

- Cross compiling. It is not doable to run build toolchains on Android OS itself. You are supposed to use toolchains provided by Android NDK.
- Multiple ABIs. Android supports armeabi-v7a, arm64-v8a, x86, and x86_64. And to make it worse, the supported ABIs have changed (e.g. armv5 and MIPS) and it is possible to build for more than those officially supported.
- Bionic libc. It lacks some features and thus not all projects can build without source code changes.
- There is no shared file storage places between multiple programs (/usr/lib etc.) other than system-provided ones (and it is now limited to the public API).
- Non-standard build ecosystem. No pkg-config, npm, no cocoapod, no nuget, no maven... argurably. It results in that, if there are 10 dependencies on desktop, the corresponding Android build will have to set up 10 dependencies builds and integrate them all.
  - And building everything from sources means that (1) all the build systems have to support Android target, as well as that (2) they have to resolve those dependencies in non-standard way.
- Toolchains had been changed aggressively e.g. migration from GCC to Clang, moving to "unified headers", and those changes made it almost impossible to catch up to date. And what if any of your dependencies could not be up to date...

A lot of projects for Linux host don't offer dedicated builds for Android. Even if they provide Android build scripts, they are not complete. And even if they are complete, there is mostly "only for itself" and there is no integrated way to combine multiple projects.

For example, fluidsynth depends on glib, but the CI scripts in the official glib repo [does not build x86/x64 builds](https://gitlab.gnome.org/GNOME/glib/blob/master/.gitlab-ci/Dockerfile) (no need to mention the actual "build" scripts).


# Build / packaging system choices

There were handful of candidates, but none of them were appropriate building fluidsynth dependencies.

- cmake: supports various ways for dependency resolution. Basically it is a build system so that unless the maintainers choose cmake it is quite difficult for outsiders to catch up with the upstream build scripts and reflect the changes.
- ndk-build: it is only about build system. It's worse than cmake, simple because no one uses it outside Android ecosystem and you will have to maintain the build scripts *everywhere*.
- qmake: Qt runs on Android and therefore it must be easier to work on top of whatever premises Android. And it should provide decent ways to resolve dependencies. The problem here is that it is completely different build ecosystem than whatever we need (glib and libsndfile).
- [CDep](https://github.com/google/cdep/): Google developers attempted to build their own build system for purpose like what I explained here. Its critical defect, other than that it is not maintained anymore, is that it requires build related commits within the source tree, meaning that unless the maintainers support CDep it is always builder's work to manually merge the upstream changes. No one maintained the builds after all.
- AOSP: it may be possible to add extra libraries, but it takes too much time to build everything. Also, there are things that are not part of official API but are bundled in the AOSP (e.g. libogg) and it is unclear how to deal with them.


# How Cerbero fits there

Basically we need a working glib. glib is core of the entire GNOME desktop environment. GStreamer is based on the GNOME project structures. And it completely respects all the active Android ABIs (x86/x64/arm32/arm64).

Building glib is not the only goal here. We also need "development" files i.e. pkg-config files as well as C header files, which is another reason we appreciate Cerbero. It builds **and installs** the dependencies to its own dist directory, which can be then used in `PKG_CONFIG_PATH`, `PKG_CONFIG_LIBDIR`, etc.

Cerbero itself is designed to build only GStreamer and not intended for general-purpose build systems. Sometimes we need changes to it, like whenever they change the supported list of the builder OS.

However adding "recipes" is easy. I could add libsndfile.recipe in a few minutes. And unlike CDep it does not involve merged commits into the source tree, so it is maintainable.

Those changes are made at [atsushieno/cerbero](https://github.com/atsushieno/cerbero/).


# bitrise.yml

Here is my incomplete build script at [bitrise](https://www.bitrise.io/). It completes up to where libfluidsynth.so builds. It fails to build further, at libfluidsynth-assetloader.so (which I believe can be still easily fixed). I don't make it public because it takes like 40+ minutes to finish.

```
---
format_version: '6'
default_step_lib_source: https://github.com/bitrise-io/bitrise-steplib.git
project_type: android
trigger_map:
- push_branch: "*"
  workflow: primary
- pull_request_source_branch: "*"
  workflow: primary
workflows:
  deploy:
    description: |
      ## How to get a signed APK

      This workflow contains the **Sign APK** step. To sign your APK all you have to do is to:

      1. Click on **Code Signing** tab
      1. Find the **ANDROID KEYSTORE FILE** section
      1. Click or drop your file on the upload file field
      1. Fill the displayed 3 input fields:
       1. **Keystore password**
       1. **Keystore alias**
       1. **Private key password**
      1. Click on **[Save metadata]** button

      That's it! From now on, **Sign APK** step will receive your uploaded files.

      ## To run this workflow

      If you want to run this workflow manually:

      1. Open the app's build list page
      2. Click on **[Start/Schedule a Build]** button
      3. Select **deploy** in **Workflow** dropdown input
      4. Click **[Start Build]** button

      Or if you need this workflow to be started by a GIT event:

      1. Click on **Triggers** tab
      2. Setup your desired event (push/tag/pull) and select **deploy** workflow
      3. Click on **[Done]** and then **[Save]** buttons

      The next change in your repository that matches any of your trigger map event will start **deploy** workflow.
    steps:
    - activate-ssh-key@4.0.3:
        run_if: '{{getenv "SSH_RSA_PRIVATE_KEY" | ne ""}}'
    - git-clone@4.0.14: {}
    - cache-pull@2.0.1: {}
    - script@1.1.5:
        title: Do anything with Script step
    - install-missing-android-tools@2.3.5:
        inputs:
        - gradlew_path: "$PROJECT_LOCATION/gradlew"
    - change-android-versioncode-and-versionname@1.1.1:
        inputs:
        - build_gradle_path: "$PROJECT_LOCATION/$MODULE/build.gradle"
    - android-lint@0.9.5:
        inputs:
        - project_location: "$PROJECT_LOCATION"
        - module: "$MODULE"
        - variant: "$VARIANT"
    - android-unit-test@0.9.5:
        inputs:
        - project_location: "$PROJECT_LOCATION"
        - module: "$MODULE"
        - variant: "$VARIANT"
    - android-build@0.9.5:
        inputs:
        - project_location: "$PROJECT_LOCATION"
        - module: "$MODULE"
        - variant: "$VARIANT"
    - sign-apk@1.2.4:
        run_if: '{{getenv "BITRISEIO_ANDROID_KEYSTORE_URL" | ne ""}}'
    - deploy-to-bitrise-io@1.3.19: {}
    - cache-push@2.0.5: {}
  primary:
    steps:
    - activate-ssh-key@4.0.3:
        run_if: '{{getenv "SSH_RSA_PRIVATE_KEY" | ne ""}}'
    - git-clone@4.0.14: {}
    - cache-pull@2.0.1: {}
    - apt-get-install@0.9.0:
        inputs:
        - packages: autotools-dev automake autoconf libtool g++ autopoint make cmake
            bison flex yasm pkg-config gtk-doc-tools libxv-dev libx11-dev libpulse-dev
            python3-dev texinfo gettext build-essential pkg-config doxygen curl libxext-dev
            libxi-dev x11proto-record-dev libxrender-dev libgl1-mesa-dev libxfixes-dev
            libxdamage-dev libxcomposite-dev libasound2-dev libxml-simple-perl dpkg-dev
            debhelper build-essential devscripts fakeroot transfig gperf libdbus-glib-1-dev
            wget glib-networking libxtst-dev libxrandr-dev libglu1-mesa-dev libegl1-mesa-dev
            git subversion xutils-dev intltool ccache python3-setuptools autogen
    - script@1.1.5:
        title: Do anything with Script step
        inputs:
        - content: |-
            #!/usr/bin/env bash
            # fail if any commands fails
            set -e
            # debug log
            set -x

            # write your script here

            ln -s /opt/android-sdk-linux ~/android-sdk-linux
            ln -s /opt/android-ndk ~/android-sdk-linux/ndk-bundle

            git clone https://github.com/Kitware/CMake.git --depth 1 -b v3.14.3 cmake
            cd cmake && mkdir build && cd build && cmake .. && make && cd ../..

            export PATH=`pwd`/cmake/build/bin:$PATH

            cd doc/android && make -f Makefile.android prepare build

            # or run a script from your repository, like:
            # bash ./path/to/script.sh
            # not just bash, e.g.:
            # ruby ./path/to/script.rb
    - android-lint@0.9.5:
        inputs:
        - project_location: "$PROJECT_LOCATION"
        - module: "$MODULE"
        - variant: "$VARIANT"
    - android-unit-test@0.9.5:
        inputs:
        - project_location: "$PROJECT_LOCATION"
        - module: "$MODULE"
        - variant: "$VARIANT"
    - deploy-to-bitrise-io@1.3.19: {}
    - cache-push@2.0.5: {}
app:
  envs:
  - opts:
      is_expand: false
    PROJECT_LOCATION: android
  - opts:
      is_expand: false
    MODULE: app
  - opts:
      is_expand: false
    VARIANT: ''
```
