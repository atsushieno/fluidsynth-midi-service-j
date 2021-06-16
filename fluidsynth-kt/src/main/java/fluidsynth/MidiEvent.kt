package fluidsynth

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary.fluid_midi_event_t

class MidiEvent : FluidsynthObject
{
    constructor() : super(MidiDriver.library.new_fluid_midi_event(), true)
    {
    }

    constructor(evt: fluid_midi_event_t) : super(evt, false)
    {
    }
    
    val native: fluid_midi_event_t
        get() = h as fluid_midi_event_t

    override fun onClose() {
        MidiDriver.library.delete_fluid_midi_event(native)
    }

    fun getType() = MidiDriver.library.fluid_midi_event_get_type(native)
    fun setType(v: Int) = MidiDriver.library.fluid_midi_event_set_type(native, v)

    fun getChannel() = MidiDriver.library.fluid_midi_event_get_channel(native)
    fun setVhannel(v: Int) = MidiDriver.library.fluid_midi_event_set_channel(native, v)

    fun getKey() = MidiDriver.library.fluid_midi_event_get_key(native)
    fun setKey(v: Int) = MidiDriver.library.fluid_midi_event_set_key(native, v)

    fun getVelocity() = MidiDriver.library.fluid_midi_event_get_velocity(native)
    fun setVelocity(v: Int) = MidiDriver.library.fluid_midi_event_set_velocity(native, v)

    fun getControl() = MidiDriver.library.fluid_midi_event_get_control(native)
    fun setControl(v: Int) = MidiDriver.library.fluid_midi_event_set_control(native, v)

    fun getValue() = MidiDriver.library.fluid_midi_event_get_value(native)
    fun setValue(v: Int) = MidiDriver.library.fluid_midi_event_set_value(native, v)

    fun getProgram() = MidiDriver.library.fluid_midi_event_get_program(native)
    fun setProgram(v: Int) = MidiDriver.library.fluid_midi_event_set_program(native, v)

    fun getPitch() = MidiDriver.library.fluid_midi_event_get_pitch(native)
    fun setPitch(v: Int) = MidiDriver.library.fluid_midi_event_set_pitch(native, v)

    fun setSysex(bytes: ByteArray, size: Int, dynamic: Boolean) = callBytesSetter(native, bytes,size,dynamic) { h,b,s,d -> MidiDriver.library.fluid_midi_event_set_sysex(h, b, s, if (d) 1 else 0)}
    fun setText(bytes: ByteArray, size: Int, dynamic: Boolean) = callBytesSetter(native, bytes,size,dynamic) { h,b,s,d -> MidiDriver.library.fluid_midi_event_set_text(h, b, s, if (d) 1 else 0)}
    fun setLyrics(bytes: ByteArray, size: Int, dynamic: Boolean) = callBytesSetter(native, bytes,size,dynamic) { h,b,s,d -> MidiDriver.library.fluid_midi_event_set_lyrics(h, b, s, if (d) 1 else 0)}

    private fun callBytesSetter(handle: fluid_midi_event_t, bytes: ByteArray, size: Int, dynamic: Boolean, func: (handle: fluid_midi_event_t, bytes: Pointer, size: Int, dynamic: Boolean) -> Int) : Int
    {
        val mem = Memory(size.toLong())
        mem.write(0, bytes, 0, size)
        return func (handle, mem.getPointer(0), size, dynamic)
    }
}