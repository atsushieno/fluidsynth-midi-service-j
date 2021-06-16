package fluidsynth

import com.sun.jna.Pointer
import fluidsynth.FluidsynthLibrary.fluid_sfloader_t
import fluidsynth.FluidsynthLibrary.fluid_sfont_t
import fluidsynth.FluidsynthLibrary.fluid_long_long_t
import fluidsynth.FluidsynthLibrary as library

open class SoundFontLoader : FluidsynthObject {
    abstract class SoundFontLoaderLoadDelegate {
        class NativeInvoker(private val java: SoundFontLoaderLoadDelegate) : library.fluid_sfloader_load_t {

            override fun apply(sfLoader: fluid_sfloader_t?, filename: Pointer?): fluid_sfont_t {
                if (filename == null)
                    throw IllegalArgumentException ("null filename")
                return java.apply(SoundFontLoader(sfLoader!!, false), filename.getString (0)).native
            }
        }

        val native = NativeInvoker(this)

        abstract fun apply(loader: SoundFontLoader, filename: String): SoundFont
    }

    abstract class SoundFontLoaderFreeDelegate {
        class NativeInvoker(val java: SoundFontLoaderFreeDelegate) : library.fluid_sfloader_free_t {

            override fun apply(sfLoader: fluid_sfloader_t) {
                java.apply(SoundFontLoader(sfLoader, false))
            }

        }

        val native = NativeInvoker(this)

        abstract fun apply(loader: SoundFontLoader)
    }

    abstract class SoundFontLoaderCallbacks {
        // used as fluid_sfloader_callback_open_t
        abstract fun open(filename: String): Pointer?

        // used as fluid_sfloader_callback_read_t
        abstract fun read(buf: Pointer?, count: Long, sfHandle: Pointer?): Int

        // used as fluid_sfloader_callback_seek_t
        abstract fun seek(sfHandle: Pointer?, offset: Int, origin: Int): Int

        // used as fluid_sfloader_callback_tell_t
        abstract fun tell(sfHandle: Pointer?): Int

        // used as fluid_sfloader_callback_close_t
        abstract fun close(sfHandle: Pointer?): Int
    }

    companion object {

        val library = fluidsynth.FluidsynthLibrary.INSTANCE

        fun newDefaultSoundFontLoader(settings: Settings): SoundFontLoader {
            return SoundFontLoader(library.new_fluid_defsfloader(settings.native), true)
        }
    }

    constructor(load: SoundFontLoaderLoadDelegate, free: SoundFontLoaderFreeDelegate)
        : super(library.new_fluid_sfloader(load.native::apply, free.native::apply), true)

    constructor (handle: fluid_sfloader_t, needsDisposal: Boolean)
            : super(handle, needsDisposal)

    val native: fluid_sfloader_t
        get() = h as fluid_sfloader_t

    fun setCallbacks(callbacks: SoundFontLoaderCallbacks) {
        library.fluid_sfloader_set_callbacks(native,
                { f -> if (f == null) throw IllegalArgumentException ("null filename"); callbacks.open( f.getString(0)) },
                { b, l, h -> callbacks.read(b, l.toNative() as Long, h) },
                { h, p, i -> callbacks.seek(h, p.toNative() as Int, i) },
                { h -> fluid_long_long_t(Pointer(callbacks.tell(h).toLong())) },
                { h -> callbacks.close(h) })
    }

    override fun onClose() {
        library.delete_fluid_sfloader(native)
    }

    fun getData(): Pointer? {
        return library.fluid_sfloader_get_data(native)
    }

    fun setData(v: Pointer?) {
        library.fluid_sfloader_set_data(native, v)
    }
}
