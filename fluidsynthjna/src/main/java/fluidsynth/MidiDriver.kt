package fluidsynth

import com.sun.jna.Pointer

class HandleMidiEventFunc : FluidsynthLibrary.handle_midi_event_func_t
{
    companion object {
        val library = FluidsynthLibrary.INSTANCE
    }

    override fun apply(data: Pointer, evt: Pointer) : Int {
        throw NotImplementedError()
    }
}


class MidiDriver(settings: Settings, handler: HandleMidiEventFunc) : FluidsynthObject(library.new_fluid_midi_driver(settings.getHandle(), handler, Pointer.NULL), true)
{
    companion object {
        val library = FluidsynthLibrary.INSTANCE
    }

    override fun onClose() {
        library.delete_fluid_midi_driver(getHandle())
    }
}