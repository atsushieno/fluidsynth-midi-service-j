package fluidsynth

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference


class MidiDriver(settings: Settings, handler: MidiEventHandler) : FluidsynthObject(library.new_fluid_midi_driver(settings.getHandle(), HandleMidiEventFunc(handler), Pointer.NULL), true)
{
    interface MidiEventHandler
    {
        fun process(event: MidiEvent) : Int
    }

    class HandleMidiEventFunc : FluidsynthLibrary.handle_midi_event_func_t
    {
        companion object {
            val library = FluidsynthLibrary.INSTANCE
        }

        val handler : MidiEventHandler

        constructor(handler: MidiEventHandler)
        {
            this.handler = handler
        }

        override fun apply(data: Pointer, evt: Pointer) : Int {
            return handler.process(MidiEvent(PointerByReference(evt)))
        }
    }


    companion object {
        val library = FluidsynthLibrary.INSTANCE
    }

    override fun onClose() {
        library.delete_fluid_midi_driver(getHandle())
    }
}

