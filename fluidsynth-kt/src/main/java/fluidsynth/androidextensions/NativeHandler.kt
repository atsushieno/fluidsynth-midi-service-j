package fluidsynth.androidextensions

import android.content.res.AssetManager
import android.media.midi.MidiDevice
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthAssetLoaderLibrary
import fluidsynth.FluidsynthInteropException
import fluidsynth.FluidsynthLibrary.fluid_sfloader_t
import fluidsynth.Settings
import fluidsynth.Synth

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
        val ret = library_assetloader.new_fluid_android_asset_sfloader(FluidsynthAssetLoaderLibrary.fluid_settings_t(settings.native.pointer), null)
        if (ret == null)
            throw FluidsynthInteropException("Failed to get native asset soundfont loader")
        // FIXME: fix memory management
        return fluid_sfloader_t(ret.pointer)
    }

    companion object {
        val library_assetloader = FluidsynthAssetLoaderLibrary.INSTANCE

        val INSTANCE = NativeHandler()
        var asset_manager_java : AssetManager? = null
    }
}