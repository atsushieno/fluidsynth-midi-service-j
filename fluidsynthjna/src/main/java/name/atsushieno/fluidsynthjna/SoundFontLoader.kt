package name.atsushieno.fluidsynthjna

import android.graphics.Point
import com.sun.jna.Native
import com.sun.jna.NativeLong
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import name.atsushieno.fluidsynth.FluidsynthLibrary

public open class SoundFontLoader : FluidsynthObject {
    public abstract class SoundFontLoaderLoadDelegate {
        class NativeInvoker : FluidsynthLibrary.fluid_sfloader_load_t {
            public constructor(java: SoundFontLoaderLoadDelegate) {
                this.java = java;
            }

            val java: SoundFontLoaderLoadDelegate;

            override fun apply(sfLoader: Pointer?, filename: Pointer?): PointerByReference {
                if (filename == null)
                    throw IllegalArgumentException ("null filename");
                return java.apply(SoundFontLoader(PointerByReference(sfLoader), false), filename.getString (0)).getHandle();
            }
        }

        val native = NativeInvoker(this);

        public abstract fun apply(loader: SoundFontLoader, filename: String): SoundFont;
    }

    public abstract class SoundFontLoaderFreeDelegate {
        class NativeInvoker : FluidsynthLibrary.fluid_sfloader_free_t {
            public constructor(java: SoundFontLoaderFreeDelegate) {
                this.java = java;
            }

            override fun apply(sfLoader: Pointer?) {
                java.apply(SoundFontLoader(PointerByReference(sfLoader), false));
            }

            val java: SoundFontLoaderFreeDelegate;
        }

        val native = NativeInvoker(this);

        public abstract fun apply(loader: SoundFontLoader);
    }

    public abstract class SoundFontLoaderCallbacks {
        // used as fluid_sfloader_callback_open_t
        public abstract fun open(filename: String): Pointer?;

        // used as fluid_sfloader_callback_read_t
        public abstract fun read(buf: Pointer?, count: Long, sfHandle: Pointer?): Int;

        // used as fluid_sfloader_callback_seek_t
        public abstract fun seek(sfHandle: Pointer?, offset: Int, origin: Int): Int;

        // used as fluid_sfloader_callback_tell_t
        public abstract fun tell(sfHandle: Pointer?): Int;

        // used as fluid_sfloader_callback_close_t
        public abstract fun close(sfHandle: Pointer?): Int;
    }

    companion object {

        val library = FluidsynthLibrary.INSTANCE;

        public fun NewDefaultSoundFontLoader(settings: Settings): SoundFontLoader {
            return SoundFontLoader(library.new_fluid_defsfloader(settings.getHandle()), true);
        }
    }

    public constructor(load: SoundFontLoaderLoadDelegate, free: SoundFontLoaderFreeDelegate)
        : super(library.new_fluid_sfloader(
            { loaderHandle, filename ->
                    if (filename == null)
                        throw IllegalArgumentException("null filename");
                    load.apply(SoundFontLoader(PointerByReference(loaderHandle), false), filename.getString(0)).getHandle();
            },
            { loaderHandle -> free.apply(SoundFontLoader(PointerByReference(loaderHandle), false)) }
        ), true)
    {
    }

    public constructor (handle: PointerByReference, needsDisposal: Boolean)
            : super(handle, needsDisposal) {
    }

    public fun setCallbacks(callbacks: SoundFontLoaderCallbacks) {
        library.fluid_sfloader_set_callbacks(getHandle(),
                { f -> if (f == null) throw IllegalArgumentException ("null filename"); callbacks.open( f.getString(0)) },
                { b, l, h -> callbacks.read(b, l.toLong(), h) },
                { h, p, i -> callbacks.seek(h, p.toInt(), i) },
                { h -> NativeLong(callbacks.tell(h).toLong()) },
                { h -> callbacks.close(h) });
    }

    override fun onClose() {
        library.delete_fluid_sfloader(getHandle());
    }

    public fun getData(): Pointer? {
        return library.fluid_sfloader_get_data(getHandle());
    }

    public fun setData(v: Pointer?) {
        library.fluid_sfloader_set_data(getHandle(), v);
    }
}
