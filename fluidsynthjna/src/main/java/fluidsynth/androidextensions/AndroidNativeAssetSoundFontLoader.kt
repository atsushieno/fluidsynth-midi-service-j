package fluidsynth.androidextensions

import android.content.res.AssetManager
import com.sun.jna.JNIEnv
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary
import fluidsynth.Settings
import fluidsynth.SoundFontLoader

class NativeHandler
{
    external fun setAssetManagerContext(assetManager: AssetManager)

    fun getAssetSfLoader(settings : Settings, assetManager : AssetManager) : PointerByReference
    {
        if (asset_manager_java == null) {
            System.loadLibrary("fluidsynth")
            asset_manager_java = assetManager
            setAssetManagerContext(assetManager)
        }
        return library.new_fluid_android_asset_sfloader(settings.getHandle(), null)
    }

    companion object {
        val library = FluidsynthLibrary.INSTANCE

        val INSTANCE = NativeHandler()
        var asset_manager_java : AssetManager? = null
    }
}

public class AndroidNativeAssetSoundFontLoader : SoundFontLoader
{
    /*
    companion object {
        /**
         * @file android.h<br></br>
         * @brief Functions for Android asset based soundfont loader.<br></br>
         * @defgroup Android Functions for android asset based soundfont loader<br></br>
         * Defines functions for Android asset based soundfont loader. Use new_fluid_android_asset_sfloader() to create a new sfloader. It is just a default sfloader with the callbacks which are Android Assets API.<br></br>
         * Original signature : `fluid_sfloader_t* new_fluid_android_asset_sfloader(JNIEnv*, fluid_settings_t*, jobject)`<br></br>
         * *native declaration : fluidsynth/android.h:14*
         */
        external fun new_fluid_android_asset_sfloader(env: JNIEnv, settings: PointerByReference, assetManager: AssetManager): PointerByReference

        fun getAssetSfLoader(settings : Settings, assetManager : AssetManager) : PointerByReference
        {
            Native.register(AndroidNativeAssetSoundFontLoader::class.java, "fluidsynth")
            return new_fluid_android_asset_sfloader (JNIEnv.CURRENT, settings.getHandle(), assetManager)
        }
        val library = AndroidFluidsynthLibrary.INSTANCE
    }
    */

    constructor(settings : Settings, assetManager : AssetManager)
            : super (NativeHandler.INSTANCE.getAssetSfLoader(settings, assetManager), true)

    override fun onClose() {
    }
}