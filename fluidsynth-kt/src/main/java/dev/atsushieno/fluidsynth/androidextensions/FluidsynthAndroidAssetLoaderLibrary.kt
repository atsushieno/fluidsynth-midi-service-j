package dev.atsushieno.fluidsynth.androidextensions

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.ptr.PointerByReference

interface FluidsynthAndroidAssetLoaderLibrary : Library {
    fun new_fluid_android_asset_sfloader(settings: PointerByReference, assetManager: PointerByReference) : PointerByReference

    companion object {
        const val JNA_LIBRARY_NAME = "fluidsynth-assetloader"
        val JNA_NATIVE_LIB = NativeLibrary.getInstance(JNA_LIBRARY_NAME)
        val INSTANCE = Native.loadLibrary(
            JNA_LIBRARY_NAME,
            FluidsynthAndroidAssetLoaderLibrary::class.java
        ) as FluidsynthAndroidAssetLoaderLibrary

    }
}
