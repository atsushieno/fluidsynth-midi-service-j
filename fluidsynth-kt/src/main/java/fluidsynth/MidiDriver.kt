package fluidsynth

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary.fluid_midi_driver_t
import fluidsynth.FluidsynthLibrary.fluid_midi_event_t


class MidiDriver(settings: Settings, handler: MidiEventHandler) : FluidsynthObject(library.new_fluid_midi_driver(settings.native, HandleMidiEventFunc(handler), Pointer.NULL), true)
{
    val native: fluid_midi_driver_t
        get() = h as fluid_midi_driver_t

    interface MidiEventHandler
    {
        fun process(event: MidiEvent) : Int
    }

    class HandleMidiEventFunc(private val handler: MidiEventHandler) : FluidsynthLibrary.handle_midi_event_func_t
    {
        companion object {
            val library = FluidsynthLibrary.INSTANCE
        }

        override fun apply(data: Pointer, evt: fluid_midi_event_t) : Int {
            return handler.process(MidiEvent(evt))
        }
    }


    companion object {
        val library = FluidsynthLibrary.INSTANCE
    }

    override fun onClose() {
        library.delete_fluid_midi_driver(native)
    }
}

