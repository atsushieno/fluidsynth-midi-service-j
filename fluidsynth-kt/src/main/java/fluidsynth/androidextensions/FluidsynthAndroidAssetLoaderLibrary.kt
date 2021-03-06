package fluidsynth.androidextensions

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary.fluid_settings_t
import fluidsynth.FluidsynthLibrary.fluid_sfloader_t

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
