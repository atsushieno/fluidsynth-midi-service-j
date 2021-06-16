package fluidsynth.androidextensions

import android.content.res.AssetManager
import android.media.midi.MidiDevice
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthInteropException
import fluidsynth.Settings
import fluidsynth.Synth

class NativeHandler
{
    external fun setAssetManagerContext(assetManager: AssetManager)

    fun getAssetSfLoader(settings : Settings, assetManager : AssetManager) : PointerByReference
    {
        if (asset_manager_java == null) {
            System.loadLibrary("fluidsynth")
            System.loadLibrary("fluidsynth-assetloader")
            asset_manager_java = assetManager
            setAssetManagerContext(assetManager)
        }
        var ret = library_assetloader.new_fluid_android_asset_sfloader(settings.getHandle(), null)
        if (ret == null)
            throw FluidsynthInteropException("Failed to get native asset soundfont loader")
        return ret
    }

    companion object {
        val library_assetloader = fluidsynthassetloader.FluidsynthAssetloaderLibrary.INSTANCE

        val INSTANCE = NativeHandler()
        var asset_manager_java : AssetManager? = null
    }
}