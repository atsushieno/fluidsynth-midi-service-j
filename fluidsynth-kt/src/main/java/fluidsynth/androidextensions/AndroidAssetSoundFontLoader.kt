package fluidsynth.androidextensions

import android.content.res.AssetManager
import com.sun.jna.Pointer
import fluidsynth.FluidsynthLibrary as library
import fluidsynth.Settings
import fluidsynth.SoundFontLoader
import java.io.InputStream
import kotlin.collections.HashMap

/* FIXME:
    These callbacks likely need to be "pinned"
 */
class AndroidAssetSoundFontLoader : SoundFontLoader
{
    companion object {
        var library = fluidsynth.FluidsynthLibrary.INSTANCE
    }

    public val callbacks : AssetLoaderCallbacks

    constructor(settings : Settings, assetManager : AssetManager)
    : super (library.new_fluid_defsfloader (settings.getHandle()), true)
    {
        callbacks = AssetLoaderCallbacks(assetManager)
        setCallbacks (callbacks)
    }

    override fun onClose() {
    }

    class AssetLoaderCallbacks(assetManager: AssetManager) : SoundFontLoaderCallbacks()
    {

        private val am : AssetManager = assetManager

        private val streams = HashMap<Pointer, InputStream> ()

        private var counter : Long = 0

        override fun open (filename : String) : Pointer?
        {
            val stream = am.open (filename, AssetManager.ACCESS_RANDOM) ?: throw IllegalArgumentException ("Asset for the argument file does not exist: $filename")
            ++counter
            val ptr =Pointer(counter)
            streams[ptr] = stream
            return ptr
        }

        override fun close (sfHandle : Pointer?) : Int
        {
            val stream = streams [sfHandle] ?: throw IllegalArgumentException ("Asset for the argument pointer does not exist: $sfHandle")
            stream.close ()
            return 0
        }

        private var buffer : ByteArray = ByteArray(1024)

        override fun read (buf : Pointer?, count : Long, sfHandle : Pointer?) : Int
        {
            if (buf == null)
                throw IllegalArgumentException ("null buffer")
            if (count > buffer.size)
                buffer = ByteArray(count.toInt())
            if (count > Int.MAX_VALUE)
                throw UnsupportedOperationException ()
            val stream = streams [sfHandle] ?: throw IllegalArgumentException ("Asset for the argument pointer does not exist: $sfHandle")
            val ret = stream.read (buffer, 0, count.toInt())

            buf.write(0, buffer, 0, ret)
            return ret
        }

        private val SEEK_BEGIN = 0
        private val SEEK_CUR = 1
        private val SEEK_END = 2

        override fun seek (sfHandle : Pointer?, offset : Int, origin : Int) : Int
        {
            System.out.println("SEEK 1")
            val stream = streams [sfHandle] ?: throw IllegalArgumentException ("Asset for the argument pointer does not exist: $sfHandle")
            when (origin) {
                SEEK_BEGIN -> {
                    System.out.printf("SEEK BEGIN: %d", offset)
                    stream.reset()
                    stream.skip(offset.toLong())
                    System.out.println("SEEK BEGIN DONE")
                    return offset
                }
                SEEK_CUR -> {
                    System.out.printf("SEEK CUR: %d", offset)
                    val remaining = stream.available()
                    stream.reset()
                    val available = stream.available()
                    val current = available - remaining
                    val newPos = current + offset
                    stream.skip (newPos.toLong())
                    System.out.println("SEEK CUR DONE")
                    return newPos
                }
                SEEK_END -> {
                    System.out.printf("SEEK END: %d", offset)
                    stream.reset()
                    val available = stream.available()
                    val newPos = available + offset
                    stream.skip(newPos.toLong())
                    System.out.println("SEEK END DONE")
                    return newPos
                }
                else -> {
                    System.out.println("SEEK THROW")
                    throw IllegalArgumentException ("Unexpected seek origin: $origin")
                }
            }
        }

        override fun tell (sfHandle : Pointer?) : Int
        {
            val stream = streams [sfHandle] ?: throw IllegalArgumentException ("Asset for the argument pointer does not exist: $sfHandle")
            val remaining = stream.available()
            stream.reset()
            val available = stream.available()
            val ret = available - remaining
            stream.skip(ret.toLong())
            return ret
        }
    }
}
