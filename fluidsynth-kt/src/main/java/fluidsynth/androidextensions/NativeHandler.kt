package fluidsynth.androidextensions

import android.content.res.AssetManager
import android.media.midi.MidiDevice
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import fluidsynth.*
import fluidsynth.FluidsynthLibrary.fluid_sfloader_t

class NativeHandler
{
    external fun setAssetManagerContext(assetManager: AssetManager)

    fun getAssetSfLoader(settings : Settings, assetManager : AssetManager) : fluid_sfloader_t
    {
        if (asset_manager_java == null) {
            System.loadLibrary("fluidsynth")
            System.loadLibrary("fluidsynth-assetloader")
            asset_manager_java = assetManager
            setAssetManagerContext(assetManager)
        }
        return FluidsynthAndroidAssetLoaderLibrary.INSTANCE.new_fluid_android_asset_sfloader(FluidsynthLibrary.fluid_settings_t(settings.native.pointer), Pointer.NULL)
    }

    companion object {
        val INSTANCE = NativeHandler()
        var asset_manager_java : AssetManager? = null
    }
}