package name.atsushieno.fluidsynthmidideviceservicej

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import java.io.File

class SynthAndroidExtensions
{
    companion object {
        fun getSoundFonts (soundFonts : MutableList<String>, context : Context, predefinedTempPath : String?) {
            // OBB support
            val obbMgr = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val obbs = context.obbDirs.flatMap { d -> d.listFiles { f -> f.extension == "obb" }.asIterable() } // FIXME: case
            for (obbDir in obbs.filter { f -> obbMgr.isObbMounted(f.absolutePath) }.map { f -> obbMgr.getMountedObbPath(f.absolutePath) })
                for (sf2 in File(obbDir).listFiles { f -> f.extension == "sf2" }) // FIXME: case
                    soundFonts.add(sf2.absolutePath)

            // Assets
            for (asset in context.assets.list(""))
                if (asset.endsWith(".sf2", true) || asset.endsWith(".sf3", true))
                    soundFonts.add(asset)
            // temporary local files for debugging
            if (predefinedTempPath != null) {
                val tempPath = File(predefinedTempPath)
                if (tempPath.exists())
                    for (sf2 in tempPath.listFiles { f -> f.extension == "sf2" })
                        if (!soundFonts.any { f -> File(f).name == sf2.name })
                            soundFonts.add(sf2.absolutePath)
            }
        }
    }
}