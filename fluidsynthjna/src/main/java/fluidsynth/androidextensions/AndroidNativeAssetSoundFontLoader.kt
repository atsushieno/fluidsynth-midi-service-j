package fluidsynth.androidextensions

import android.content.res.AssetManager
import android.media.midi.MidiDevice
import com.sun.jna.JNIEnv
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary as library
import fluidsynth.Settings
import fluidsynth.SoundFontLoader
import fluidsynth.Synth


class NativeHandler
{
    companion object {
        val library_assetloader = fluidsynthassetloader.FluidsynthAssetloaderLibrary.INSTANCE

        var asset_manager_java : AssetManager? = null

        @JvmStatic
        external fun setAssetManagerContext(assetManager: AssetManager)

        fun getAssetSfLoader(settings : Settings, assetManager : AssetManager) : PointerByReference
        {
            if (asset_manager_java == null) {
                System.loadLibrary("fluidsynth")
                System.loadLibrary("fluidsynth-assetloader")
                asset_manager_java = assetManager
                setAssetManagerContext(assetManager)
            }
            return library_assetloader.new_fluid_android_asset_sfloader(settings.getHandle(), null)
        }
    }
}

public class AndroidNativeAssetSoundFontLoader : SoundFontLoader
{
    constructor(settings : Settings, assetManager : AssetManager)
            : super (NativeHandler.getAssetSfLoader(settings, assetManager), true)

    override fun onClose() {
    }
}