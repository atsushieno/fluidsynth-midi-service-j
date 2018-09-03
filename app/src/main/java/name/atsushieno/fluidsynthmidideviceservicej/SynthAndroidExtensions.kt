package name.atsushieno.fluidsynthmidideviceservicej

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import java.io.File

public class SynthAndroidExtensions
{
    companion object {
        public fun getSoundFonts (soundFonts : MutableList<String?>, context : Context, predefinedTempPath : String) {
            // OBB support
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                var obbMgr = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager;
                var obbs = context.obbDirs.flatMap { d -> d.listFiles { f -> f.extension.equals(".obb") }.asIterable() }; // FIXME: case
                for (obbDir in obbs.filter { f -> obbMgr.isObbMounted(f.absolutePath) }.map { f -> obbMgr.getMountedObbPath(f.absolutePath) })
                    for (sf2 in File(obbDir).listFiles { f -> f.extension.equals(".sf2") }) // FIXME: case
                        soundFonts.add(sf2.absolutePath);
            }

            // Assets
            for (asset in context.assets.list(""))
                if (asset.endsWith(".sf2")) // FIXME: case
                    soundFonts.add(asset);
            // temporary local files for debugging
            var tempPath = File(predefinedTempPath);
            if (tempPath.exists())
                for (sf2 in tempPath.listFiles { f -> f.extension.equals(".sf2") })
                    if (!soundFonts.any { f -> File(f).name.equals(sf2.name) })
                        soundFonts.add(sf2.absolutePath);
        }
    }
}