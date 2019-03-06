package fluidsynth

import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary as library

open class SoundFontLoader : FluidsynthObject {
    abstract class SoundFontLoaderLoadDelegate {
        class NativeInvoker : library.fluid_sfloader_load_t {
            constructor(java: SoundFontLoaderLoadDelegate) {
                this.java = java
            }

            private val java: SoundFontLoaderLoadDelegate

            override fun apply(sfLoader: Pointer?, filename: Pointer?): PointerByReference {
                if (filename == null)
                    throw IllegalArgumentException ("null filename")
                return java.apply(SoundFontLoader(PointerByReference(sfLoader), false), filename.getString (0)).getHandle()
            }
        }

        public val native = NativeInvoker(this)

        abstract fun apply(loader: SoundFontLoader, filename: String): SoundFont
    }

    abstract class SoundFontLoaderFreeDelegate {
        class NativeInvoker : library.fluid_sfloader_free_t {
            constructor(java: SoundFontLoaderFreeDelegate) {
                this.java = java
            }

            override fun apply(sfLoader: Pointer?) {
                java.apply(SoundFontLoader(PointerByReference(sfLoader), false))
            }

            public val java: SoundFontLoaderFreeDelegate
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

        var library = fluidsynth.FluidsynthLibrary.INSTANCE

        fun newDefaultSoundFontLoader(settings: Settings): SoundFontLoader {
            return SoundFontLoader(library.new_fluid_defsfloader(settings.getHandle()), true)
        }
    }

    constructor(load: SoundFontLoaderLoadDelegate, free: SoundFontLoaderFreeDelegate)
        : super(library.new_fluid_sfloader(load.native::apply, free.native::apply), true)

    constructor (handle: PointerByReference, needsDisposal: Boolean)
            : super(handle, needsDisposal)

    fun setCallbacks(callbacks: SoundFontLoaderCallbacks) {
        library.fluid_sfloader_set_callbacks(getHandle(),
                { f -> if (f == null) throw IllegalArgumentException ("null filename"); callbacks.open( f.getString(0)) },
                { b, l, h -> callbacks.read(b, l.toLong(), h) },
                { h, p, i -> callbacks.seek(h, p.toInt(), i) },
                { h -> NativeLong(callbacks.tell(h).toLong()) },
                { h -> callbacks.close(h) })
    }

    override fun onClose() {
        library.delete_fluid_sfloader(getHandle())
    }

    fun getData(): Pointer? {
        return library.fluid_sfloader_get_data(getHandle())
    }

    fun setData(v: Pointer?) {
        library.fluid_sfloader_set_data(getHandle(), v)
    }
}
