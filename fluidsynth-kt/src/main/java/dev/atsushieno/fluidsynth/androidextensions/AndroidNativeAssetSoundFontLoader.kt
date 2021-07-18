package dev.atsushieno.fluidsynth.androidextensions

import android.content.res.AssetManager
import dev.atsushieno.fluidsynth.*

public class AndroidNativeAssetSoundFontLoader : SoundFontLoader
{
    constructor(settings : Settings, assetManager : AssetManager)
            : super (NativeHandler.INSTANCE.getAssetSfLoader(settings, assetManager), true)

    override fun onClose() {
    }
}
