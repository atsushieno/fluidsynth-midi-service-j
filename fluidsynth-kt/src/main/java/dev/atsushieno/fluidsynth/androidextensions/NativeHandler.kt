package dev.atsushieno.fluidsynth.androidextensions

import android.content.res.AssetManager
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import dev.atsushieno.fluidsynth.*

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
        return library_assetloader.new_fluid_android_asset_sfloader(settings.getHandle(), PointerByReference(Pointer.NULL))
    }

    companion object {
        val library_assetloader = FluidsynthAndroidAssetLoaderLibrary.INSTANCE

        val INSTANCE = NativeHandler()
        var asset_manager_java : AssetManager? = null
    }
}