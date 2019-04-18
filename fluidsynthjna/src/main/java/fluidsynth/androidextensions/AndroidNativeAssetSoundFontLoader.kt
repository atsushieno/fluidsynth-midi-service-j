package fluidsynth.androidextensions

import android.content.res.AssetManager
import fluidsynth.*
import fluidsynth.FluidsynthLibrary as library

public class AndroidNativeAssetSoundFontLoader : SoundFontLoader
{
    constructor(settings : Settings, assetManager : AssetManager)
            : super (NativeHandler.INSTANCE.getAssetSfLoader(settings, assetManager), true)

    override fun onClose() {
    }
}
