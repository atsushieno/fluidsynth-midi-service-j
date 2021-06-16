@file:Suppress("unused")

package fluidsynth

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary.*
import java.nio.ByteBuffer

class Synth : FluidsynthObject {
    companion object {
        var library = fluidsynth.FluidsynthLibrary.INSTANCE

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
            : super(library.new_fluid_synth(settings.native), true)

    constructor (handle: PointerByReference)
            : super(handle, false)

    val native : fluid_synth_t
        get() = h as fluid_synth_t

    override fun onClose() {
        library.delete_fluid_synth(native)
    }

    fun getSettings(): Settings {
        return Settings(library.fluid_synth_get_settings(native))
    }


    var handleError: ((Int, String, String) -> Boolean)? = null

    private fun onError(errorCode: Int, message: String) {
        val err = getLastError()
        if (handleError == null || !(handleError?.invoke(errorCode, message, err)!!))
            throw FluidsynthInteropException("$message (native error: $err)")
    }

    fun noteOn(channel: Int, key: Int, vel: Int) {
        val ret = library.fluid_synth_noteon(native, channel, key, vel)
        if (ret != 0)
            onError(ret, "noteon operation failed (error code " + ret+ ")")
    }

    fun noteOff(channel: Int, key: Int) {
        // not sure if we should always raise exception, it seems that it also returns FUILD_FAILED for not-on-state note.
        val ret = library.fluid_synth_noteoff(native, channel, key)
        if (ret != 0)
            onError(ret, "noteoff operation failed")
    }

    fun cc(channel: Int, num: Int, v: Int) {
        val ret = library.fluid_synth_cc(native, channel, num, v)
        if (ret != 0)
            onError(ret, "control change operation failed")
    }

    fun getCC(channel: Int, num: Int): Int {
        val ref = IntByReference()
        val ret = library.fluid_synth_get_cc(native, channel, num, ref)
        if (ret != 0)
            onError(ret,"control change get operation failed")
        return ref.value
    }

    fun sysex(input: ByteArray, output: ByteArray?, dryrun: Boolean = false): Boolean {
        val outlen = IntByReference(output?.size ?: 0)
        val handled: IntByReference? = null

        // FIXME: it had better avoid extraneous memory allocation here.
        var inbuf = ByteBuffer.allocateDirect(input.size)
        inbuf.put(input)
        val inptr = Native.getDirectBufferPointer(inbuf)
        // FIXME: support output parameter?

        val ret = library.fluid_synth_sysex(native, inptr, input.size, null, outlen, handled, if (dryrun) 1 else 0)
        if (ret != 0)
            onError(ret,"sysex operation failed")

        return handled?.value != 0
    }

    fun pitchBend(channel: Int, v: Int) {
        val ret = library.fluid_synth_pitch_bend(native, channel, v)
        if (ret != 0)
            onError(ret, "pitch bend change operation failed")
    }

    fun getPitchBend(channel: Int): Int {
        val ref: IntByReference? = null
        var ret = library.fluid_synth_get_pitch_bend(native, channel, ref)
        if (ret != 0)
            onError(ret, "pitch bend get operation failed")
        return if (ref != null) ref.value else 0
    }

    fun pitchWheelSens(channel: Int, v: Int) {
        val ret = library.fluid_synth_pitch_wheel_sens(native, channel, v)
        if (ret != 0)
            onError(ret,"pitch wheel sens change operation failed")
    }

    fun getPitchWheelSens(channel: Int): Int {
        val ref: IntByReference? = null
        val ret = library.fluid_synth_get_pitch_wheel_sens(native, channel, ref)
        if (ret != 0)
            onError(ret,"pitch wheel sens get operation failed")
        return ref!!.value
    }

    fun programChange(channel: Int, program: Int) {
        val ret = library.fluid_synth_program_change(native, channel, program)
        if (ret != 0)
            onError(ret,"program change operation failed")
    }

    fun channelPressure(channel: Int, v: Int) {
        val ret = library.fluid_synth_channel_pressure(native, channel, v)
        if (ret != 0)
            onError(ret,"channel pressure change operation failed")
    }

    fun bankSelect(channel: Int, bank: Int) {
        val ret = library.fluid_synth_bank_select(native, channel, bank)
        if (ret != 0)
            onError(ret,"bank select operation failed")
    }

    fun soundFontSelect(channel: Int, soundFontId: Int) {
        val ret = library.fluid_synth_sfont_select(native, channel, soundFontId)
        if (ret != 0)
            onError(ret,"sound font select operation failed")
    }

    fun programSelect(channel: Int, soundFontId: Int, bank: Int, preset: Int) {
        val ret = library.fluid_synth_program_select(native, channel, soundFontId, bank, preset)
        if (ret != 0)
            onError(ret,"program select operation failed")
    }

    fun programSelectBySoundFontName(channel: Int, soundFontName: String, bank: Int, preset: Int) {
        val ret = library.fluid_synth_program_select_by_sfont_name(native, channel, soundFontName, bank, preset)
        if (ret != 0)
            onError(ret, "program select (by sound font name) operation failed")
    }

    fun getProgram(channel: Int, soundFontId: IntByReference, bank: IntByReference, preset: IntByReference) {
        val ret = library.fluid_synth_get_program(native, channel, soundFontId, bank, preset)
        if (ret != 0)
            onError(ret,"program get operation failed")
    }

    fun unsetProgram(channel: Int) {
        val ret = library.fluid_synth_unset_program(native, channel)
        if (ret != 0)
            onError(ret,"program unset operation failed")
    }

    /*ERROR
    fun getChannelInfo(channel: Int): Pointer {
        val info: PointerByReference? = null;
        if (library.fluid_synth_get_channel_info(native, channel, info) != 0)
            onError("channel info get operation failed");
        return info!!.value;
    }
    */

    fun programReset() {
        val ret = library.fluid_synth_program_reset(native)
        if (ret != 0)
            onError(ret,"program reset operation failed")
    }

    fun systemReset() {
        val ret = library.fluid_synth_system_reset(native)
        if (ret != 0)
            onError(ret,"system reset operation failed")
    }

    // fluid_synth_get_channel_preset() is deprecated, so I don't bind it.
    // Then fluid_synth_start() takes fluid_preset_t* which is returned only by this deprecated function, so I don't bind it either.
    // Then fluid_synth_stop() is paired by the function above, so I don't bind it either.

    fun loadSoundFont(filename: String, resetPresets: Boolean) {
        if (library.fluid_synth_sfload(native, filename, if (resetPresets) 1 else 0) == FLUID_FAILED)
            onError(FLUID_FAILED, "sound font load operation failed")
    }

    fun reloadSoundFont(id: Int) {
        if (library.fluid_synth_sfreload(native, id) == FLUID_FAILED)
            onError(FLUID_FAILED,"sound font reload operation failed")
    }

    fun unloadSoundFont(id: Int, resetPresets: Boolean) {
        if (library.fluid_synth_sfunload(native, id, if (resetPresets) 1 else 0) == FLUID_FAILED)
        onError(FLUID_FAILED,"sound font unload operation failed")
    }

    fun addSoundFont(soundFont: SoundFont) {
        if (library.fluid_synth_add_sfont(native, soundFont.native) == FLUID_FAILED)
            onError(FLUID_FAILED,"sound font add operation failed")
    }

    fun removeSoundFont(soundFont: SoundFont) {
        library.fluid_synth_remove_sfont(native, soundFont.native)
    }

    fun getFontCount(): Int {
        return library.fluid_synth_sfcount(native)
    }

    fun getSoundFont (index : Int) : SoundFont?
    {
        val ret = library.fluid_synth_get_sfont (native, index)
        return if (ret == Pointer.NULL) null else SoundFont(ret)
    }

    fun getSoundFontById (id : Int) : SoundFont?
    {
        val ret = library.fluid_synth_get_sfont_by_id (native, id)
        return if (ret == Pointer.NULL) null else SoundFont(ret)
    }

    fun getSoundFontByName (name : String) : SoundFont?
    {
        val ret = library.fluid_synth_get_sfont_by_name (native, name)
        return if (ret == Pointer.NULL) null else SoundFont(ret)
    }

    fun setBankOffset (soundFontId : Int, offset : Int)
    {
        val ret = library.fluid_synth_set_bank_offset (native, soundFontId, offset)
        if (ret != 0)
            onError (ret, "bank offset set operation failed")
    }

    fun getBankOffset (soundFontId : Int)
    {
        library.fluid_synth_get_bank_offset (native, soundFontId)
    }

    fun setReverb (roomSize : Double, damping : Double, width : Double, level : Double)
    {
        library.fluid_synth_set_reverb (native, roomSize, damping, width, level)
    }

    fun setReverbOn (enabled : Boolean)
    {
        library.fluid_synth_set_reverb_on (native, if (enabled) 1 else 0)
    }

    fun getReverbRoomSize() : Double {
        return library.fluid_synth_get_reverb_roomsize(native)
    }

    fun getReverbDamp() : Double {
        return library.fluid_synth_get_reverb_damp(native)
    }

    fun getReverbLevel () : Double {
        return library.fluid_synth_get_reverb_level(native)
    }

    fun getReverbWidth () : Double {
        return library.fluid_synth_get_reverb_width(native)
    }

    fun setChorus (numVoices : Int, level : Double, speed : Double, depthMS : Double, type : Int/*FluidChorusMod*/)
    {
        library.fluid_synth_set_chorus (native, numVoices, level, speed, depthMS, type)
    }

    fun setChorusOn (enabled : Boolean)
    {
        library.fluid_synth_set_chorus_on (native, if (enabled) 1 else 0)
    }

    fun getNumberOfChorusVoices () : Int {
        return library.fluid_synth_get_chorus_nr(native)
    }

    fun getChorusLevel () : Double {
        return library.fluid_synth_get_chorus_level(native)
    }

    fun getChorusType () : Int/*FluidChorusMod*/ {
        return library.fluid_synth_get_chorus_type(native)
    }

    fun getMidiChannelCount () : Int {
        return library.fluid_synth_count_midi_channels(native)
    }

    fun getAudioChannelCount () : Int {
        return library.fluid_synth_count_audio_channels(native)
    }

    fun getAudioGroupCount () : Int {
        return library.fluid_synth_count_audio_groups(native)
    }

    fun getEffectChannelCount () : Int {
        return library.fluid_synth_count_effects_channels(native)
    }

    fun setChannelRate (sampleRate : Float)
    {
        library.fluid_synth_set_sample_rate (native, sampleRate)
    }

    fun getGain () : Float {
        return library.fluid_synth_get_gain(native)
    }

    fun setGain (v : Float) {
        library.fluid_synth_set_gain(native, v)
    }

    fun getPolyphony () : Int {
        return library.fluid_synth_get_polyphony(native)
    }

    fun setPolyphony (v : Int) {
        library.fluid_synth_set_polyphony(native, v)
    }

    fun getActiveVoiceCount () : Int {
        return library.fluid_synth_get_active_voice_count(native)
    }

    fun getInternalBufferSize () : Int {
        return library.fluid_synth_get_internal_bufsize(native)
    }

    fun setInterpolationMethod (channel : Int, interpolationMethod : Int/*FluidInterpolation*/)
    {
        val ret = library.fluid_synth_set_interp_method (native, channel, interpolationMethod)
        if (ret != 0)
            onError (ret, "interpolation method set operation failed")
    }

    fun setGenerator (channel : Int, param : Int, v : Float)
    {
        val ret = library.fluid_synth_set_gen (native, channel, param, v)
        if (ret != 0)
            onError (ret, "generator set operation failed")
    }

    /*ERROR
    fun setGenerator (channel : Int, param : Int, v : Float, absolute : Boolean, normalized : Boolean)
    {
        if (library.fluid_synth_set_gen2 (native, channel, param, v, absolute, normalized) != 0)
            onError ("generator set2 operation failed");
    }
    */

    fun getGenerator (channel : Int, param : Int) : Float
    {
        return library.fluid_synth_get_gen (native, channel, param)
    }

    // <Tuning>

    /*ERROR
    fun createKeyTuning (bank : Int, prog: Int, name:String, pitch : DoubleArray)
    {
        if (pitch.size != 128)
            throw IllegalArgumentException ("pitch array must be of 128 elements.");
        if (library.fluid_synth_create_key_tuning (native, bank, prog, name, pitch) != 0)
            onError ("key tuning create operation failed");
    }
    */

    /*ERROR
    fun activateKeyTuning (bank : Int, prog: Int, name:String, pitch : DoubleArray, shouldApply : Boolean)
    {
        if (pitch.size != 128)
            throw IllegalArgumentException ("pitch array must be of 128 elements.");
        if (library.fluid_synth_activate_key_tuning (native, bank, prog, name, pitch, shouldApply) != 0)
            onError ("key tuning create operation failed");
    }
    */

    /*ERROR
    fun createOctaveTuning (bank : Int, prog: Int, name:String, pitch : DoubleArray)
    {
        if (pitch.size != 128)
            throw IllegalArgumentException ("pitch array must be of 128 elements.");
        if (library.fluid_synth_create_octave_tuning (native, bank, prog, name, pitch) != 0)
            onError ("key tuning create operation failed");
    }
    */

    /*ERROR
    fun activateOctaveTuning (bank : Int, prog: Int, name:String, pitch : DoubleArray, shouldApply: Boolean)
    {
        if (pitch.size != 128)
            throw IllegalArgumentException ("pitch array must be of 128 elements.");
        if (library.fluid_synth_activate_octave_tuning (native, bank, prog, name, pitch, shouldApply) != 0)
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
        if (library.fluid_synth_tune_notes (native, bank, prog, keys.size, keys, pitch, shouldApply) != 0)
            onError ("key tuning create operation failed");
    }
    */

    /*ERROR
    fun selectTuning (channel : Int, bank : Int, prog : Int)
    {
        if (library.fluid_synth_select_tuning (native, channel, bank, prog) != 0)
            onError ("tuning select operation failed");
    }
    */

    fun activateTuning (channel : Int, bank : Int, prog : Int, shouldApply : Boolean)
    {
        val ret = library.fluid_synth_activate_tuning (native, channel, bank, prog, if (shouldApply) 1 else 0)
        if (ret != 0)
            onError (ret,"tuning activate operation failed")
    }

    /*ERROR
    fun resetTuning (channel : Int)
    {
        if (library.fluid_synth_reset_tuning (native, channel) != 0)
            onError ("tuning reset operation failed");
    }
    */

    fun deactivateTuning (channel : Int, shouldApply : Boolean)
    {
        val ret = library.fluid_synth_deactivate_tuning (native, channel, if (shouldApply) 1 else 0)
        if (ret != 0)
            onError (ret,"tuning deactivate operation failed")
    }

    fun tuningIterationStart ()
    {
        library.fluid_synth_tuning_iteration_start (native)
    }

    fun tuningIterationNext (bank : IntByReference, prog : IntByReference) : Boolean
    {
        return library.fluid_synth_tuning_iteration_next (native, bank, prog) != 0
    }

    /*ERROR
    fun tuningDump (bank : Int, prog : Int, name : String) : DoubleArray
    {
        val ret = DoubleArray (128);
        val nm = ByteArray (64);
        library.fluid_synth_tuning_dump (native, bank, prog, nm, nm.size, ret);
        name = WString (nm, 0, nm.size).toString();
        return ret;
    }
    */

    // </Tuning>

    fun getCpuLoad () : Double {
        return library.fluid_synth_get_cpu_load(native)
    }

    private fun getLastError () : String {
        val ptr = library.fluid_synth_error (native)
        return com.sun.jna.WString(ptr).toString()
    }


    fun writeSample16 (length : Int, leftOut : Pointer/*ShortArray*/, leftOffset : Int, leftIncrement : Int, rightOut : Pointer/*ShortArray*/, rightOffset : Int, rightIncrement : Int)
    {
        val ret = library.fluid_synth_write_s16 (native, length, leftOut, leftOffset, leftIncrement, rightOut, rightOffset, rightIncrement)
        if (ret != 0)
            onError (ret, "16bit sample write operation failed")
    }

    fun writeSampleFloat (length : Int, leftOut : Pointer/*FloatArray*/, leftOffset : Int, leftIncrement : Int, rightOut : Pointer/*FloatArray*/, rightOffset : Int, rightIncrement : Int)
    {
        val ret = library.fluid_synth_write_float (native, length, leftOut, leftOffset, leftIncrement, rightOut, rightOffset, rightIncrement)
        if (ret != 0)
            onError (ret,"float sample write operation failed")
    }

    fun writeSampleFloat (length : Int, leftOut : PointerByReference /*Float[]*/, rightOut : PointerByReference/*Float[]*/)
    {
        val dummy = PointerByReference(Pointer.NULL)
        val dummy2 = PointerByReference(Pointer.NULL)
        val ret = library.fluid_synth_nwrite_float (native, length, leftOut, rightOut, dummy, dummy2)
        if (ret != 0)
            onError (ret,"float sample write operation failed")
    }

    fun process (length : Int, nIn : Int, inBuffer : PointerByReference/*FloatArray[]*/, nOut : Int, outBuffer : PointerByReference/*FloatArray[]*/)
    {
        val ret = library.fluid_synth_process (native, length, nIn, inBuffer, nOut, outBuffer)
        if (ret != 0)
            onError (ret, "float sample write operation failed")
    }

    fun addSoundFontLoader (loader : SoundFontLoader)
    {
        library.fluid_synth_add_sfloader (native, loader.native)
    }

    fun allocateVoice (sample : fluid_sample_t, channel : Int, key : Int, vel : Int) : Voice
    {
        val ret = library.fluid_synth_alloc_voice (native, sample, channel, key, vel)
        if (ret == Pointer.NULL)
            onError (0,"voice allocate operation failed")
        return Voice(ret)
    }

    fun startVoice (voice : Voice)
    {
        library.fluid_synth_start_voice (native, voice.native)
    }

    fun getVoiceList (voices : Array<Voice>, voiceId : Int) : Boolean
    {
        //val arr = Array<PointerByReference?>(voices.size, {_ -> null});
        val arr = Pointer(Native.malloc((Native.POINTER_SIZE * voices.size).toLong()))
        library.fluid_synth_get_voicelist (native, PointerByReference(arr), voices.size, voiceId)

        var i = 0
        while (i < voices.size) {
            val ptr = arr.getPointer((i * Native.POINTER_SIZE).toLong())
            if (ptr == Pointer.NULL)
                return false
            voices[i] = Voice(fluid_voice_t(ptr))
            i++
        }
        return true
    }

    /*ERROR
    fun setMidiRouter (router : PointerByReference)
    {
        library.fluid_synth_set_midi_router (native, router);
    }
    */
}

