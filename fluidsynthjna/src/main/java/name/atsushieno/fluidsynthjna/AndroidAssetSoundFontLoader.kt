package name.atsushieno.fluidsynthjna.androidextensions

import android.content.res.AssetManager
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import name.atsushieno.fluidsynth.FluidsynthLibrary
import name.atsushieno.fluidsynthjna.Settings
import name.atsushieno.fluidsynthjna.SoundFontLoader
import java.io.InputStream
import java.util.*
import javax.crypto.interfaces.PBEKey
import kotlin.collections.HashMap

public class AndroidAssetSoundFontLoader : SoundFontLoader
{
    companion object {
        val library = FluidsynthLibrary.INSTANCE;
    }

    public constructor(settings : Settings, assetManager : AssetManager)
    : super (library.new_fluid_defsfloader (settings.getHandle()), true)
    {
        setCallbacks (AssetLoaderCallbacks (assetManager));
    }

    override fun onClose() {
    }

    class AssetLoaderCallbacks : SoundFontLoaderCallbacks
    {
        public constructor(assetManager : AssetManager)
        {
            this.am = assetManager;
        }

        val am : AssetManager;

        val streams : HashMap<Pointer, InputStream> = HashMap<Pointer, InputStream> ();

        var counter : Long = 0;

        public override fun open (filename : String) : Pointer?
        {
            val stream = am.open (filename, AssetManager.ACCESS_RANDOM);
            if (stream == null)
                throw IllegalArgumentException ("Asset for the argument file does not exist: " + filename)
            val ptr =Pointer(counter++);
            streams[ptr] = stream;
            return ptr;
        }

        public override fun close (sfHandle : Pointer?) : Int
        {
            val stream = streams [sfHandle];
            if (stream == null)
                throw IllegalArgumentException ("Asset for the argument pointer does not exist: " + sfHandle)
            stream.close ();
            return 0;
        }

        var buffer : ByteArray = ByteArray(1024);

        public override fun read (buf : Pointer?, count : Long, sfHandle : Pointer?) : Int
        {
            if (buf == null)
                throw IllegalArgumentException ("null buffer");
            if (count > buffer.size)
                buffer = ByteArray(count.toInt());
            if (count > Int.MAX_VALUE)
                throw UnsupportedOperationException ();
            val stream = streams [sfHandle];
            if (stream == null)
                throw IllegalArgumentException ("Asset for the argument pointer does not exist: " + sfHandle)
            val ret = stream.read (buffer, 0, count.toInt());

            buf.write(0, buffer, 0, ret);
            return ret;
        }

        val SEEK_BEGIN = 0
        val SEEK_CUR = 1
        val SEEK_END = 2

        public override fun seek (sfHandle : Pointer?, offset : Int, origin : Int) : Int
        {
            val stream = streams [sfHandle];
            if (stream == null)
                throw IllegalArgumentException ("Asset for the argument pointer does not exist: " + sfHandle)
            when (origin) {
                SEEK_BEGIN -> {
                    stream.reset()
                    stream.skip(offset.toLong())
                    return offset
                }
                SEEK_CUR -> {
                    val remaining = stream.available()
                    stream.reset()
                    val available = stream.available()
                    val current = available - remaining
                    val newPos = current + offset
                    stream.skip (newPos.toLong())
                    return newPos
                }
                SEEK_END -> {
                    stream.reset()
                    val available = stream.available()
                    val newPos = available + offset
                    stream.skip(newPos.toLong())
                    return newPos
                }
                else -> throw IllegalArgumentException ("Unexpected seek origin: " + origin)
            }
        }

        public override fun tell (sfHandle : Pointer?) : Int
        {
            val stream = streams [sfHandle];
            if (stream == null)
                throw IllegalArgumentException ("Asset for the argument pointer does not exist: " + sfHandle)
            val remaining = stream.available()
            stream.reset()
            val available = stream.available()
            val ret = available - remaining
            stream.skip(ret.toLong())
            return ret;
        }
    }
}