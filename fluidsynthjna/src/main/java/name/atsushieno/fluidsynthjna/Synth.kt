@file:Suppress("unused")

package name.atsushieno.fluidsynthjna

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import name.atsushieno.fluidsynth.FluidsynthLibrary
import name.atsushieno.fluidsynth.FluidsynthLibrary.FLUID_FAILED
import java.nio.ByteBuffer

class Synth : FluidsynthObject {
    companion object {
        var library: FluidsynthLibrary = FluidsynthLibrary.INSTANCE

        @JvmStatic
        fun isSoundFont(filename: String): Boolean {
            return library.fluid_is_soundfont(filename) != 0
        }

        @JvmStatic
        fun isMidiFile(filename: String): Boolean {
            return library.fluid_is_midifile(filename) != 0
        }
    }

    constructor(settings: Settings)
            : super(library.new_fluid_synth(settings.getHandle()), true)

    constructor (handle: PointerByReference)
            : super(handle, false)

    override fun onClose() {
        library.delete_fluid_synth(getHandle())
    }

    fun getSettings(): Settings {
        return Settings(library.fluid_synth_get_settings(getHandle()))
    }


    var handleError: ((String, String) -> Boolean)? = null

    private fun onError(message: String) {
        val err = getLastError()
        if (handleError == null || !(handleError?.invoke(message, err)!!))
            throw FluidsynthInteropException("$message (native error: $err)")
    }

    fun noteOn(channel: Int, key: Int, vel: Int) {
        if (library.fluid_synth_noteon(getHandle(), channel, key, vel) != 0)
            onError("noteon operation failed")
    }

    fun noteOff(channel: Int, key: Int) {
        // not sure if we should always raise exception, it seems that it also returns FUILD_FAILED for not-on-state note.
        if (library.fluid_synth_noteoff(getHandle(), channel, key) != 0)
            onError("noteoff operation failed")
    }

    fun cc(channel: Int, num: Int, v: Int) {
        if (library.fluid_synth_cc(getHandle(), channel, num, v) != 0)
            onError("control change operation failed")
    }

    fun getCC(channel: Int, num: Int): Int {
        val ret = IntByReference()
        if (library.fluid_synth_get_cc(getHandle(), channel, num, ret) != 0)
            onError("control change get operation failed")
        return ret.value
    }

    fun sysex(input: ByteArray, output: ByteArray?, dryrun: Boolean = false): Boolean {
        val outlen = IntByReference(output?.size ?: 0)
        val handled: IntByReference? = null

        val inptr = Native.getDirectBufferPointer(ByteBuffer.wrap(input, 0, input.size))
        val outptr = Native.getDirectBufferPointer(ByteBuffer.wrap(output, 0, input.size))

        if (library.fluid_synth_sysex(getHandle(), inptr, input.size, outptr, outlen, handled, if (dryrun) 1 else 0) != 0)
            onError("sysex operation failed")
        return handled?.value != 0
    }

    fun pitchBend(channel: Int, v: Int) {
        if (library.fluid_synth_pitch_bend(getHandle(), channel, v) != 0)
            onError("pitch bend change operation failed")
    }

    fun getPitchBend(channel: Int): Int {
        val ret: IntByReference? = null
        if (library.fluid_synth_get_pitch_bend(getHandle(), channel, ret) != 0)
            onError("pitch bend get operation failed")
        return ret!!.value
    }

    fun pitchWheelSens(channel: Int, v: Int) {
        if (library.fluid_synth_pitch_wheel_sens(getHandle(), channel, v) != 0)
            onError("pitch wheel sens change operation failed")
    }

    fun getPitchWheelSens(channel: Int): Int {
        val ret: IntByReference? = null
        if (library.fluid_synth_get_pitch_wheel_sens(getHandle(), channel, ret) != 0)
            onError("pitch wheel sens get operation failed")
        return ret!!.value
    }

    fun programChange(channel: Int, program: Int) {
        if (library.fluid_synth_program_change(getHandle(), channel, program) != 0)
            onError("program change operation failed")
    }

    fun channelPressure(channel: Int, v: Int) {
        if (library.fluid_synth_channel_pressure(getHandle(), channel, v) != 0)
            onError("channel pressure change operation failed")
    }

    fun bankSelect(channel: Int, bank: Int) {
        if (library.fluid_synth_bank_select(getHandle(), channel, bank) != 0)
            onError("bank select operation failed")
    }

    fun soundFontSelect(channel: Int, soundFontId: Int) {
        if (library.fluid_synth_sfont_select(getHandle(), channel, soundFontId) != 0)
            onError("sound font select operation failed")
    }

    fun programSelect(channel: Int, soundFontId: Int, bank: Int, preset: Int) {
        if (library.fluid_synth_program_select(getHandle(), channel, soundFontId, bank, preset) != 0)
            onError("program select operation failed")
    }

    fun programSelectBySoundFontName(channel: Int, soundFontName: String, bank: Int, preset: Int) {
        if (library.fluid_synth_program_select_by_sfont_name(getHandle(), channel, soundFontName, bank, preset) != 0)
            onError("program select (by sound font name) operation failed")
    }

    fun getProgram(channel: Int, soundFontId: IntByReference, bank: IntByReference, preset: IntByReference) {
        if (library.fluid_synth_get_program(getHandle(), channel, soundFontId, bank, preset) != 0)
            onError("program get operation failed")
    }

    fun unsetProgram(channel: Int) {
        if (library.fluid_synth_unset_program(getHandle(), channel) != 0)
            onError("program unset operation failed")
    }

    /*ERROR
    fun getChannelInfo(channel: Int): Pointer {
        val info: PointerByReference? = null;
        if (library.fluid_synth_get_channel_info(getHandle(), channel, info) != 0)
            onError("channel info get operation failed");
        return info!!.value;
    }
    */

    fun programReset() {
        if (library.fluid_synth_program_reset(getHandle()) != 0)
            onError("program reset operation failed")
    }

    fun systemReset() {
        if (library.fluid_synth_system_reset(getHandle()) != 0)
            onError("system reset operation failed")
    }

    // fluid_synth_get_channel_preset() is deprecated, so I don't bind it.
    // Then fluid_synth_start() takes fluid_preset_t* which is returned only by this deprecated function, so I don't bind it either.
    // Then fluid_synth_stop() is paired by the function above, so I don't bind it either.

    fun loadSoundFont(filename: String, resetPresets: Boolean) {
        if (library.fluid_synth_sfload(getHandle(), filename, if (resetPresets) 1 else 0) == FLUID_FAILED)
            onError("sound font load operation failed")
    }

    fun reloadSoundFont(id: Int) {
        if (library.fluid_synth_sfreload(getHandle(), id) == FLUID_FAILED)
            onError("sound font reload operation failed")
    }

    fun unloadSoundFont(id: Int, resetPresets: Boolean) {
        if (library.fluid_synth_sfunload(getHandle(), id, if (resetPresets) 1 else 0) == FLUID_FAILED)
        onError("sound font unload operation failed")
    }

    fun addSoundFont(soundFont: SoundFont) {
        if (library.fluid_synth_add_sfont(getHandle(), soundFont.getHandle()) == FLUID_FAILED)
            onError("sound font add operation failed")
    }

    fun removeSoundFont(soundFont: SoundFont) {
        library.fluid_synth_remove_sfont(getHandle(), soundFont.getHandle())
    }

    fun getFontCount(): Int {
        return library.fluid_synth_sfcount(getHandle())
    }

    fun getSoundFont (index : Int) : SoundFont?
    {
        val ret = library.fluid_synth_get_sfont (getHandle(), index)
        return if (ret == Pointer.NULL) null else SoundFont (ret)
    }

    fun getSoundFontById (id : Int) : SoundFont?
    {
        val ret = library.fluid_synth_get_sfont_by_id (getHandle(), id)
        return if (ret == Pointer.NULL) null else SoundFont (ret)
    }

    fun getSoundFontByName (name : String) : SoundFont?
    {
        val ret = library.fluid_synth_get_sfont_by_name (getHandle(), name)
        return if (ret == Pointer.NULL) null else SoundFont (ret)
    }

    fun setBankOffset (soundFontId : Int, offset : Int)
    {
        if (library.fluid_synth_set_bank_offset (getHandle(), soundFontId, offset) != 0)
            onError ("bank offset set operation failed")
    }

    fun getBankOffset (soundFontId : Int)
    {
        library.fluid_synth_get_bank_offset (getHandle(), soundFontId)
    }

    fun setReverb (roomSize : Double, damping : Double, width : Double, level : Double)
    {
        library.fluid_synth_set_reverb (getHandle(), roomSize, damping, width, level)
    }

    fun setReverbOn (enabled : Boolean)
    {
        library.fluid_synth_set_reverb_on (getHandle(), if (enabled) 1 else 0)
    }

    fun getReverbRoomSize() : Double {
        return library.fluid_synth_get_reverb_roomsize(getHandle())
    }

    fun getReverbDamp() : Double {
        return library.fluid_synth_get_reverb_damp(getHandle())
    }

    fun getReverbLevel () : Double {
        return library.fluid_synth_get_reverb_level(getHandle())
    }

    fun getReverbWidth () : Double {
        return library.fluid_synth_get_reverb_width(getHandle())
    }

    fun setChorus (numVoices : Int, level : Double, speed : Double, depthMS : Double, type : Int/*FluidChorusMod*/)
    {
        library.fluid_synth_set_chorus (getHandle(), numVoices, level, speed, depthMS, type)
    }

    fun setChorusOn (enabled : Boolean)
    {
        library.fluid_synth_set_chorus_on (getHandle(), if (enabled) 1 else 0)
    }

    fun getNumberOfChorusVoices () : Int {
        return library.fluid_synth_get_chorus_nr(getHandle())
    }

    fun getChorusLevel () : Double {
        return library.fluid_synth_get_chorus_level(getHandle())
    }

    fun getChorusType () : Int/*FluidChorusMod*/ {
        return library.fluid_synth_get_chorus_type(getHandle())
    }

    fun getMidiChannelCount () : Int {
        return library.fluid_synth_count_midi_channels(getHandle())
    }

    fun getAudioChannelCount () : Int {
        return library.fluid_synth_count_audio_channels(getHandle())
    }

    fun getAudioGroupCount () : Int {
        return library.fluid_synth_count_audio_groups(getHandle())
    }

    fun getEffectChannelCount () : Int {
        return library.fluid_synth_count_effects_channels(getHandle())
    }

    fun setChannelRate (sampleRate : Float)
    {
        library.fluid_synth_set_sample_rate (getHandle(), sampleRate)
    }

    fun getGain () : Float {
        return library.fluid_synth_get_gain(getHandle())
    }

    fun setGain (v : Float) {
        library.fluid_synth_set_gain(getHandle(), v)
    }

    fun getPolyphony () : Int {
        return library.fluid_synth_get_polyphony(getHandle())
    }

    fun setPolyphony (v : Int) {
        library.fluid_synth_set_polyphony(getHandle(), v)
    }

    fun getActiveVoiceCount () : Int {
        return library.fluid_synth_get_active_voice_count(getHandle())
    }

    fun getInternalBufferSize () : Int {
        return library.fluid_synth_get_internal_bufsize(getHandle())
    }

    fun setInterpolationMethod (channel : Int, interpolationMethod : Int/*FluidInterpolation*/)
    {
        if (library.fluid_synth_set_interp_method (getHandle(), channel, interpolationMethod) != 0)
            onError ("interpolation method set operation failed")
    }

    fun setGenerator (channel : Int, param : Int, v : Float)
    {
        if (library.fluid_synth_set_gen (getHandle(), channel, param, v) != 0)
            onError ("generator set operation failed")
    }

    /*ERROR
    fun setGenerator (channel : Int, param : Int, v : Float, absolute : Boolean, normalized : Boolean)
    {
        if (library.fluid_synth_set_gen2 (getHandle(), channel, param, v, absolute, normalized) != 0)
            onError ("generator set2 operation failed");
    }
    */

    fun getGenerator (channel : Int, param : Int) : Float
    {
        return library.fluid_synth_get_gen (getHandle(), channel, param)
    }

    // <Tuning>

    /*ERROR
    fun createKeyTuning (bank : Int, prog: Int, name:String, pitch : DoubleArray)
    {
        if (pitch.size != 128)
            throw IllegalArgumentException ("pitch array must be of 128 elements.");
        if (library.fluid_synth_create_key_tuning (getHandle(), bank, prog, name, pitch) != 0)
            onError ("key tuning create operation failed");
    }
    */

    /*ERROR
    fun activateKeyTuning (bank : Int, prog: Int, name:String, pitch : DoubleArray, shouldApply : Boolean)
    {
        if (pitch.size != 128)
            throw IllegalArgumentException ("pitch array must be of 128 elements.");
        if (library.fluid_synth_activate_key_tuning (getHandle(), bank, prog, name, pitch, shouldApply) != 0)
            onError ("key tuning create operation failed");
    }
    */

    /*ERROR
    fun createOctaveTuning (bank : Int, prog: Int, name:String, pitch : DoubleArray)
    {
        if (pitch.size != 128)
            throw IllegalArgumentException ("pitch array must be of 128 elements.");
        if (library.fluid_synth_create_octave_tuning (getHandle(), bank, prog, name, pitch) != 0)
            onError ("key tuning create operation failed");
    }
    */

    /*ERROR
    fun activateOctaveTuning (bank : Int, prog: Int, name:String, pitch : DoubleArray, shouldApply: Boolean)
    {
        if (pitch.size != 128)
            throw IllegalArgumentException ("pitch array must be of 128 elements.");
        if (library.fluid_synth_activate_octave_tuning (getHandle(), bank, prog, name, pitch, shouldApply) != 0)
            onError ("key tuning create operation failed");
    }
    */

    /*ERROR
    fun tuneTones (bank : Int, prog: Int, keys : IntArray, pitch : DoubleArray, shouldApply: Boolean)
    {
        if (keys.size != 128)
            throw IllegalArgumentException ("key array must be of 128 elements.");
        if (pitch.size != 128)
            throw IllegalArgumentException ("pitch array must be of 128 elements.");
        if (library.fluid_synth_tune_notes (getHandle(), bank, prog, keys.size, keys, pitch, shouldApply) != 0)
            onError ("key tuning create operation failed");
    }
    */

    /*ERROR
    fun selectTuning (channel : Int, bank : Int, prog : Int)
    {
        if (library.fluid_synth_select_tuning (getHandle(), channel, bank, prog) != 0)
            onError ("tuning select operation failed");
    }
    */

    fun activateTuning (channel : Int, bank : Int, prog : Int, shouldApply : Boolean)
    {
        if (library.fluid_synth_activate_tuning (getHandle(), channel, bank, prog, if (shouldApply) 1 else 0) != 0)
            onError ("tuning activate operation failed")
    }

    /*ERROR
    fun resetTuning (channel : Int)
    {
        if (library.fluid_synth_reset_tuning (getHandle(), channel) != 0)
            onError ("tuning reset operation failed");
    }
    */

    fun deactivateTuning (channel : Int, shouldApply : Boolean)
    {
        if (library.fluid_synth_deactivate_tuning (getHandle(), channel, if (shouldApply) 1 else 0) != 0)
            onError ("tuning deactivate operation failed")
    }

    fun tuningIterationStart ()
    {
        library.fluid_synth_tuning_iteration_start (getHandle())
    }

    fun tuningIterationNext (bank : IntByReference, prog : IntByReference) : Boolean
    {
        return library.fluid_synth_tuning_iteration_next (getHandle(), bank, prog) != 0
    }

    /*ERROR
    fun tuningDump (bank : Int, prog : Int, name : String) : DoubleArray
    {
        val ret = DoubleArray (128);
        val nm = ByteArray (64);
        library.fluid_synth_tuning_dump (getHandle(), bank, prog, nm, nm.size, ret);
        name = WString (nm, 0, nm.size).toString();
        return ret;
    }
    */

    // </Tuning>

    fun getCpuLoad () : Double {
        return library.fluid_synth_get_cpu_load(getHandle())
    }

    private fun getLastError () : String {
        val ptr = library.fluid_synth_error (getHandle())
        return com.sun.jna.WString(ptr).toString()
    }


    fun writeSample16 (length : Int, leftOut : Pointer/*ShortArray*/, leftOffset : Int, leftIncrement : Int, rightOut : Pointer/*ShortArray*/, rightOffset : Int, rightIncrement : Int)
    {
        if (library.fluid_synth_write_s16 (getHandle(), length, leftOut, leftOffset, leftIncrement, rightOut, rightOffset, rightIncrement) != 0)
            onError ("16bit sample write operation failed")
    }

    fun writeSampleFloat (length : Int, leftOut : Pointer/*FloatArray*/, leftOffset : Int, leftIncrement : Int, rightOut : Pointer/*FloatArray*/, rightOffset : Int, rightIncrement : Int)
    {
        if (library.fluid_synth_write_float (getHandle(), length, leftOut, leftOffset, leftIncrement, rightOut, rightOffset, rightIncrement) != 0)
            onError ("float sample write operation failed")
    }

    fun writeSampleFloat (length : Int, leftOut : PointerByReference /*Float[]*/, rightOut : PointerByReference/*Float[]*/)
    {
        val dummy = PointerByReference(Pointer.NULL)
        val dummy2 = PointerByReference(Pointer.NULL)
        if (library.fluid_synth_nwrite_float (getHandle(), length, leftOut, rightOut, dummy, dummy2) != 0)
            onError ("float sample write operation failed")
    }

    fun process (length : Int, nIn : Int, inBuffer : PointerByReference/*FloatArray[]*/, nOut : Int, outBuffer : PointerByReference/*FloatArray[]*/)
    {
        if (library.fluid_synth_process (getHandle(), length, nIn, inBuffer, nOut, outBuffer) != 0)
            onError ("float sample write operation failed")
    }

    fun addSoundFontLoader (loader : SoundFontLoader)
    {
        library.fluid_synth_add_sfloader (getHandle(), loader.getHandle())
    }

    fun allocateVoice (sample : PointerByReference, channel : Int, key : Int, vel : Int) : Voice
    {
        val ret = library.fluid_synth_alloc_voice (getHandle(), sample, channel, key, vel)
        if (ret == Pointer.NULL)
            onError ("voice allocate operation failed")
        return Voice (ret)
    }

    fun startVoice (voice : Voice)
    {
        library.fluid_synth_start_voice (getHandle(), voice.getHandle())
    }

    fun getVoiceList (voices : Array<Voice>, voiceId : Int) : Boolean
    {
        //val arr = Array<PointerByReference?>(voices.size, {_ -> null});
        val arr = Pointer(Native.malloc((Native.POINTER_SIZE * voices.size).toLong()))
        library.fluid_synth_get_voicelist (getHandle(), PointerByReference(arr), voices.size, voiceId)

        var i = 0
        while (i < voices.size) {
            val ptr = arr.getPointer((i * Native.POINTER_SIZE).toLong())
            if (ptr == Pointer.NULL)
                return false
            voices[i] = Voice(PointerByReference(ptr))
            i++
        }
        return true
    }

    /*ERROR
    fun setMidiRouter (router : PointerByReference)
    {
        library.fluid_synth_set_midi_router (getHandle(), router);
    }
    */
}

