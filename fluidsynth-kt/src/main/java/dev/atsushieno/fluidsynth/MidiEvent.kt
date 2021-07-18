package dev.atsushieno.fluidsynth

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

class MidiEvent : FluidsynthObject
{
    constructor() : super(MidiDriver.library.new_fluid_midi_event(), true)
    {
    }

    constructor(evt: PointerByReference) : super(evt, false)
    {
    }

    override fun onClose() {
        MidiDriver.library.delete_fluid_midi_event(getHandle())
    }

    fun getType() = MidiDriver.library.fluid_event_get_type(getHandle())
    fun setType(v: Int) = MidiDriver.library.fluid_midi_event_set_type(getHandle(), v)

    fun getChannel() = MidiDriver.library.fluid_event_get_channel(getHandle())
    fun setVhannel(v: Int) = MidiDriver.library.fluid_midi_event_set_channel(getHandle(), v)

    fun getKey() = MidiDriver.library.fluid_event_get_key(getHandle())
    fun setKey(v: Int) = MidiDriver.library.fluid_midi_event_set_key(getHandle(), v)

    fun getVelocity() = MidiDriver.library.fluid_event_get_velocity(getHandle())
    fun setVelocity(v: Int) = MidiDriver.library.fluid_midi_event_set_velocity(getHandle(), v)

    fun getControl() = MidiDriver.library.fluid_event_get_control(getHandle())
    fun setControl(v: Int) = MidiDriver.library.fluid_midi_event_set_control(getHandle(), v)

    fun getValue() = MidiDriver.library.fluid_event_get_value(getHandle())
    fun setValue(v: Int) = MidiDriver.library.fluid_midi_event_set_value(getHandle(), v)

    fun getProgram() = MidiDriver.library.fluid_event_get_program(getHandle())
    fun setProgram(v: Int) = MidiDriver.library.fluid_midi_event_set_program(getHandle(), v)

    fun getPitch() = MidiDriver.library.fluid_event_get_pitch(getHandle())
    fun setPitch(v: Int) = MidiDriver.library.fluid_midi_event_set_pitch(getHandle(), v)

    fun setSysex(bytes: ByteArray, size: Int, dynamic: Boolean) = callBytesSetter(getHandle(), bytes,size,dynamic, { h,b,s,d -> MidiDriver.library.fluid_midi_event_set_sysex(h, b, s, if (d) 1 else 0)})
    fun setText(bytes: ByteArray, size: Int, dynamic: Boolean) = callBytesSetter(getHandle(), bytes,size,dynamic, { h,b,s,d -> MidiDriver.library.fluid_midi_event_set_text(h, b, s, if (d) 1 else 0)})
    fun setLyrics(bytes: ByteArray, size: Int, dynamic: Boolean) = callBytesSetter(getHandle(), bytes,size,dynamic, { h,b,s,d -> MidiDriver.library.fluid_midi_event_set_lyrics(h, b, s, if (d) 1 else 0)})

    private fun callBytesSetter(handle: PointerByReference, bytes: ByteArray, size: Int, dynamic: Boolean, func: (handle: PointerByReference, bytes: Pointer, size: Int, dynamic: Boolean) -> Int) : Int
    {
        val mem = Memory(size.toLong())
        mem.write(0, bytes, 0, size)
        return func (handle, mem.getPointer(0), size, dynamic)
    }
}