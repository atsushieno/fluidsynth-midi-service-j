@file:Suppress("unused")

package name.atsushieno.ktmidi

import java.io.InputStream
import java.io.OutputStream

internal fun OutputStream.writeByte(b : Byte)
{
    val arr = byteArrayOf(b)
    this.write(arr, 0, 1)
}

class MidiMusic {
    companion object {
        fun read (stream : InputStream) : MidiMusic {
            val r = SmfReader (stream)
            r.read ()
            return r.music
        }

        fun getMetaEventsOfType (messages : Iterable<MidiMessage>, metaType : Byte) = sequence {
            var v = 0
            for (m in messages) {
                v += m.deltaTime
                if (m.event.eventType == MidiEvent.META && m.event.msb == metaType)
                    yield(MidiMessage(v, m.event))
            }
        }

        fun getTotalPlayTimeMilliseconds (messages : MutableList<MidiMessage>, deltaTimeSpec : Int) : Int
        {
            return getPlayTimeMillisecondsAtTick (messages, messages.sumBy { m -> m.deltaTime}, deltaTimeSpec)
        }

        fun getPlayTimeMillisecondsAtTick (messages : List<MidiMessage>, ticks : Int, deltaTimeSpec : Int) : Int
        {
            if (deltaTimeSpec < 0)
                throw UnsupportedOperationException ("non-tick based DeltaTime")
            else {
                var tempo : Int = MidiMetaType.DEFAULT_TEMPO
                var v = 0
                var t = 0
                for (m in messages) {
                    val deltaTime = t + if (m.deltaTime < ticks) m.deltaTime else ticks - t
                    v += (tempo / 1000 * deltaTime / deltaTimeSpec)
                    if (deltaTime != m.deltaTime)
                        break
                    t += m.deltaTime
                    if (m.event.eventType == MidiEvent.META && m.event.msb == MidiMetaType.TEMPO)
                        tempo = MidiMetaType.getTempo (m.event.data!!)
                }
                return v
            }
        }
    }

    val tracks : MutableList<MidiTrack> = ArrayList ()

    var deltaTimeSpec : Byte = 0

    var format : Byte = 0

    fun addTrack (track : MidiTrack) {
        this.tracks.add (track)
    }

    fun getMetaEventsOfType (metaType : Byte) : Iterable<MidiMessage> {
        if (format != 0.toByte())
            return SmfTrackMerger.merge (this).getMetaEventsOfType (metaType)
        return getMetaEventsOfType (tracks [0].messages, metaType).asIterable()
    }

    fun getTotalTicks () : Int {
        if (format != 0.toByte())
            return SmfTrackMerger.merge (this).getTotalTicks ()
        return tracks [0].messages.sumBy { m : MidiMessage -> m.deltaTime}
    }

    fun getTotalPlayTimeMilliseconds () : Int
    {
        if (format != 0.toByte())
            return SmfTrackMerger.merge (this).getTotalPlayTimeMilliseconds ()
        return getTotalPlayTimeMilliseconds (tracks [0].messages, deltaTimeSpec.toInt())
    }

    fun getTimePositionInMillisecondsForTick (ticks : Int) : Int {
        if (format != 0.toByte())
            return SmfTrackMerger.merge (this).getTimePositionInMillisecondsForTick (ticks)
        return getPlayTimeMillisecondsAtTick (tracks [0].messages, ticks, deltaTimeSpec.toInt())
    }

    init {
        this.format = 1
    }
}

class MidiTrack
{
    constructor ()
            : this (ArrayList<MidiMessage> ())

    constructor(messages : MutableList<MidiMessage>?)
    {
        if (messages == null)
            throw IllegalArgumentException ("null messages")
        this.messages = messages
    }

    var messages : MutableList<MidiMessage> = ArrayList ()

    fun addMessage (msg : MidiMessage)
    {
        messages.add (msg)
    }
}

class MidiMessage(val deltaTime: Int, evt: MidiEvent) {

    val event : MidiEvent = evt
}

class MidiCC
{
    companion object {

        const val BANK_SELECT = 0x00.toByte()
        const val MODULATION = 0x01.toByte()
        const val BREATH = 0x02.toByte()
        const val FOOT = 0x04.toByte()
        const val PORTAMENTO_TIME = 0x05.toByte()
        const val DTE_MSB = 0x06.toByte()
        const val VOLUME = 0x07.toByte()
        const val BALANCE = 0x08.toByte()
        const val PAN = 0x0A.toByte()
        const val EXPRESSION = 0x0B.toByte()
        const val EFFECT_CONTROL_1 = 0x0C.toByte()
        const val EFFECT_CONTROL_2 = 0x0D.toByte()
        const val GENERAL_1 = 0x10.toByte()
        const val GENERAL_2 = 0x11.toByte()
        const val GENERAL_3 = 0x12.toByte()
        const val GENERAL_4 = 0x13.toByte()
        const val BANK_SELECT_LSB = 0x20.toByte()
        const val MODULATION_LSB = 0x21.toByte()
        const val BREATH_LSB = 0x22.toByte()
        const val FOOT_LSB = 0x24.toByte()
        const val PORTAMENTO_TIME_LSB = 0x25.toByte()
        const val DTE_LSB = 0x26.toByte()
        const val VOLUME_LSB = 0x27.toByte()
        const val BALANCE_LSB = 0x28.toByte()
        const val PAN_LSB = 0x2A.toByte()
        const val EXPRESSION_LSB = 0x2B.toByte()
        const val EFFECT_1_LSB = 0x2C.toByte()
        const val EFFECT_2_LSB = 0x2D.toByte()
        const val GENERAL_1_LSB = 0x30.toByte()
        const val GENERAL_2_LSB = 0x31.toByte()
        const val GENERAL_3_LSB = 0x32.toByte()
        const val GENERAL_4_LSB = 0x33.toByte()
        const val HOLD = 0x40.toByte()
        const val PORTAMENTO_SWITCH = 0x41.toByte()
        const val SOSTENUTO = 0x42.toByte()
        const val SOFT_PEDAL = 0x43.toByte()
        const val LEGATO= 0x44.toByte()
        const val HOLD_2= 0x45.toByte()
        const val SOUND_CONTROLLER_1 = 0x46.toByte()
        const val SOUND_CONTROLLER_2 = 0x47.toByte()
        const val SOUND_CONTROLLER_3 = 0x48.toByte()
        const val SOUND_CONTROLLER_4 = 0x49.toByte()
        const val SOUND_CONTROLLER_5 = 0x4A.toByte()
        const val SOUND_CONTROLLER_6 = 0x4B.toByte()
        const val SOUND_CONTROLLER_7 = 0x4C.toByte()
        const val SOUND_CONTROLLER_8 = 0x4D.toByte()
        const val SOUND_CONTROLLER_9 = 0x4E.toByte()
        const val SOUND_CONTROLLER_10 = 0x4F.toByte()
        const val GENERAL_5= 0x50.toByte()
        const val GENERAL_6 = 0x51.toByte()
        const val GENERAL_7 = 0x52.toByte()
        const val GENERAL_8 = 0x53.toByte()
        const val PORTAMENTO_CONTROL = 0x54.toByte()
        const val RSD = 0x5B.toByte()
        const val EFFECT_1 = 0x5B.toByte()
        const val TREMOLO = 0x5C.toByte()
        const val EFFECT_2 = 0x5C.toByte()
        const val CSD = 0x5D.toByte()
        const val EFFECT_3 = 0x5D.toByte()
        const val CELESTE = 0x5E.toByte()
        const val EFFECT_4 = 0x5E.toByte()
        const val PHASER = 0x5F.toByte()
        const val EFFECT_5 = 0x5F.toByte()
        const val DTE_INCREMENT = 0x60.toByte()
        const val DTE_DECREMENT = 0x61.toByte()
        const val NRPN_LSB = 0x62.toByte()
        const val NRPN_MSB = 0x63.toByte()
        const val RPN_LSB = 0x64.toByte()
        const val RPN_MSB = 0x65.toByte()
        // Channel mode messages
        const val ALL_SOUND_OFF = 0x78.toByte()
        const val RESET_ALL_CONTROLLERS = 0x79.toByte()
        const val LOCAL_CONTROL = 0x7A.toByte()
        const val ALL_NOTES_OFF = 0x7B.toByte()
        const val OMNI_MODE_OFF = 0x7C.toByte()
        const val OMNI_MODE_ON = 0x7D.toByte()
        const val POLY_MODE_OFF = 0x7E.toByte()
        const val POLY_MODE_ON = 0x7F.toByte()
    }
}

class MidiRpnType
{
    companion object {

        const val PITCH_BEND_SENSITIVITY = 0.toByte()
        const val FINE_TUNING = 1.toByte()
        const val COARSE_TUNING = 2.toByte()
        const val TUNING_PROGRAM = 3.toByte()
        const val TUNING_BANK_SELECT = 4.toByte()
        const val MODULATION_DEPTH = 5.toByte()
    }
}

class MidiMetaType
{
    companion object {

        const val SEQUENCE_NUMBER = 0x00.toByte()
        const val TEXT = 0x01.toByte()
        const val COPYRIGHT = 0x02.toByte()
        const val TRACK_NAME= 0x03.toByte()
        const val INSTRUMENT_NAME = 0x04.toByte()
        const val LYRIC = 0x05.toByte()
        const val MARKER = 0x06.toByte()
        const val CUE = 0x07.toByte()
        const val CHANNEL_PREFIX= 0x20.toByte()
        const val END_OF_TRACK = 0x2F.toByte()
        const val TEMPO = 0x51.toByte()
        const val SMTPE_OFFSET = 0x54.toByte()
        const val TIME_SIGNATURE = 0x58.toByte()
        const val KEY_SIGNATURE = 0x59.toByte()
        const val SEQUENCER_SPECIFIC = 0x7F.toByte()

        const val DEFAULT_TEMPO = 500000

        fun getTempo (data : ByteArray) : Int
        {
            return (data[0].toInt() shl 16) + (data [1].toInt() shl 8) + data [2]
        }

        fun getBpm (data : ByteArray) : Double
        {
            return 60000000.0 / getTempo(data)
        }
    }
}

class MidiEvent
{
    companion object {

        const val NOTE_OFF : Byte = 0x80.toByte()
        const val NOTE_ON : Byte = 0x90.toByte()
        const val PAF : Byte = 0xA0.toByte()
        const val CC : Byte = 0xB0.toByte()
        const val PROGRAM : Byte = 0xC0.toByte()
        const val CAF : Byte = 0xD0.toByte()
        const val PITCH : Byte = 0xE0.toByte()
        const val SYSEX : Byte = 0xF0.toByte()
        const val SYSEX_2 : Byte = 0xF7.toByte()
        const val META : Byte = 0xFF.toByte()

        const val EndSysEx = 0xF7

        fun convert (bytes : Array<Byte>, index : Int, size : Int) = sequence {
            var i = index
            val end = index +size
            while (i < end) {
                if (bytes[i].toInt() == 0xF0) {
                    val tmp = bytes.copyOfRange(i, size)
                    yield (MidiEvent (0xF0.toByte(), 0, 0, tmp.toByteArray()))
                    i += size
                } else {
                    if (end < i + MidiEvent.fixedDataSize(bytes[i]))
                        throw Exception (String.format("Received data was incomplete to build MIDI status message for '%x' status.", bytes[i]))
                    val z = MidiEvent.fixedDataSize(bytes[i])
                    yield (MidiEvent (bytes[i], bytes [i+1], (if (z > 1) bytes [i+2] else 0), null))
                    i += z + 1
                }
            }
        }

        fun fixedDataSize (statusByte : Byte) : Byte =
                when ((statusByte.toInt() and 0xF0).toByte()) {
                    0xF0.toByte() -> 0 // including 0xF7, 0xFF
                    PROGRAM, CAF -> 1
                    else -> 2
                }
    }

    constructor (value : Int)
    {
        this.value = value
        this.data = null
    }

    constructor (type : Byte, arg1 : Byte, arg2 : Byte, data : ByteArray?)
    {
        this.value = type.toInt() + (arg1.toInt() shl 8) + (arg2.toInt() shl 16)
        this.data = data
    }

    var value : Int = 0

    // This expects EndSysEx byte _inclusive_ for F0 message.
    val data : ByteArray?

    val statusByte : Byte = (value and 0xFF).toByte()

    val eventType : Byte =
            when (statusByte) {
                META,
                SYSEX,
                SYSEX_2 -> this.statusByte
                else ->(value and 0xF0).toByte()
            }

    val msb : Byte = ((value and 0xFF00) shr 8).toByte()


    val lsb : Byte = ((value and 0xFF0000) shr 16).toByte()

    val metaType : Byte = msb

    val channel : Byte = (value and 0x0F).toByte()

    override fun toString () : String
    {
        return value.toString()
    }
}

class SmfWriter// default meta event writer.
(var stream: OutputStream) {

    var disableRunningStatus : Boolean = false

    private fun writeShort (v: Short)
    {
        stream.writeByte ((v / 0x100).toByte())
        stream.writeByte ((v % 0x100).toByte())
    }

    private fun writeInt (v : Int)
    {
        stream.writeByte ((v / 0x1000000).toByte())
        stream.writeByte ((v / 0x10000 and 0xFF).toByte())
        stream.writeByte ((v / 0x100 and 0xFF).toByte())
        stream.writeByte ((v % 0x100).toByte())
    }

    fun writeMusic (music : MidiMusic)
    {
        writeHeader (music.format.toShort(), music.tracks.size.toShort(), music.deltaTimeSpec.toShort())
        for (track in music.tracks)
            writeTrack (track)
    }

    fun writeHeader (format : Short, tracks : Short, deltaTimeSpec : Short)
    {
        stream.write (byteArrayOf('M'.toByte(), 'T'.toByte(), 'h'.toByte(), 'd'.toByte()), 0, 4)
        writeShort (0)
        writeShort (6)
        writeShort (format)
        writeShort (tracks)
        writeShort (deltaTimeSpec)
    }

    var metaEventWriter : (Boolean, MidiMessage, OutputStream?) -> Int = SmfWriterExtension.DEFAULT_META_EVENT_WRITER

    fun writeTrack (track : MidiTrack)
    {
        stream.write (byteArrayOf('M'.toByte(), 'T'.toByte(), 'r'.toByte(), 'k'.toByte()), 0, 4)
        writeInt (getTrackDataSize (track))

        var running_status : Byte = 0

        for (e in track.messages) {
            write7BitVariableInteger (e.deltaTime)
            when (e.event.eventType) {
                MidiEvent.META -> metaEventWriter(false, e, stream)
                MidiEvent.SYSEX, MidiEvent.SYSEX_2 -> {
                    stream.writeByte(e.event.eventType)
                    if (e.event.data != null) {
                        write7BitVariableInteger(e.event.data.size)
                        stream.write(e.event.data, 0, e.event.data.size)
                    }
                }
                else -> {
                    if (disableRunningStatus || e.event.statusByte != running_status)
                        stream.writeByte(e.event.statusByte)
                    val len = MidiEvent.fixedDataSize (e.event.eventType)
                    stream.writeByte(e.event.msb)
                    if (len > 1)
                        stream.writeByte(e.event.lsb)
                    if (len > 2)
                        throw Exception ("Unexpected data size: $len")
                }
            }
            running_status = e.event.statusByte
        }
    }

    private fun getVariantLength (value : Int) : Int
    {
        if (value < 0)
            throw IllegalArgumentException (String.format ("Length must be non-negative integer: %d", value))
        if (value == 0)
            return 1
        var ret = 0
        var x : Int = value
        while (x != 0) {
            ret++
            x = x shr 7
        }
        return ret
    }

    private fun getTrackDataSize (track : MidiTrack ) : Int
    {
        var size = 0
        var runningStatus : Byte = 0
        for (e in track.messages) {
            // delta time
            size += getVariantLength (e.deltaTime)

            // arguments
            when (e.event.eventType) {
                MidiEvent.META -> size += metaEventWriter (true, e, null)
                MidiEvent.SYSEX, MidiEvent.SYSEX_2 -> {
                    size++
                    if (e.event.data != null) {
                        size += getVariantLength(e.event.data.size)
                        size += e.event.data.size
                    }
                }
                else -> {
                    // message type & channel
                    if (disableRunningStatus || runningStatus != e.event.statusByte)
                        size++
                    size += MidiEvent.fixedDataSize(e.event.eventType)
                }
            }

            runningStatus = e.event.statusByte
        }
        return size
    }

    private fun write7BitVariableInteger (value : Int)
    {
        write7BitVariableInteger (value, false)
    }

    private fun write7BitVariableInteger (value : Int, shifted : Boolean)
    {
        if (value == 0) {
            stream.writeByte ((if (shifted) 0x80 else 0).toByte())
            return
        }
        if (value >= 0x80)
            write7BitVariableInteger (value shr 7, true)
        stream.writeByte (((value and 0x7F) + if (shifted) 0x80 else 0).toByte())
    }

}

class SmfWriterExtension
{
    companion object {

        val DEFAULT_META_EVENT_WRITER : (Boolean, MidiMessage, OutputStream?) -> Int = { b,m,o -> defaultMetaWriterFunc (b,m,o) }

        private fun defaultMetaWriterFunc (lengthMode : Boolean, e : MidiMessage , stream : OutputStream?) : Int
        {
            if (e.event.data == null || stream == null)
                return 0

            if (lengthMode) {
                // [0x00] 0xFF metaType size ... (note that for more than one meta event it requires step count of 0).
                val repeatCount : Int = e.event.data.size / 0x7F
                if (repeatCount == 0)
                    return 3 + e.event.data.size
                val mod : Int = e.event.data.size % 0x7F
                return repeatCount * (4 + 0x7F) - 1 + if (mod > 0) 4 + mod else 0
            }

            var written = 0
            val total : Int = e . event.data.size
            while (written < total) {
                if (written > 0)
                    stream.writeByte(0) // step
                stream.writeByte(0xFF.toByte())
                stream.writeByte(e.event.metaType)
                val size = Math.min(0x7F, total - written)
                stream.writeByte(size.toByte())
                stream.write(e.event.data, written, size)
                written += size
            }
            return 0
        }

        val vsqMetaTextSplitter : (Boolean, MidiMessage, OutputStream) -> Int = { b,m,o -> vsqMetaTextSplitterFunc (b,m,o) }

        private fun vsqMetaTextSplitterFunc (lengthMode : Boolean, e : MidiMessage , stream : OutputStream?) : Int
        {
            if (e.event.data == null)
                return 0

            // The split should not be applied to "Master Track"
            if (e.event.data.size < 0x80) {
                return DEFAULT_META_EVENT_WRITER(lengthMode, e, stream)
            }

            if (lengthMode) {
                // { [0x00] 0xFF metaType DM:xxxx:... } * repeat + 0x00 0xFF metaType DM:xxxx:mod...
                // (note that for more than one meta event it requires step count of 0).
                val repeatCount = e.event.data.size / 0x77
                if (repeatCount == 0)
                    return 11 + e.event.data.size
                val mod = e.event.data.size % 0x77
                return repeatCount * (12 + 0x77) - 1 + if (mod > 0) 12+mod else 0
            }

            if (stream == null)
                return 0


            var written = 0
            val total: Int = e.event.data.size
            var idx = 0
            do {
                if (written > 0)
                    stream.writeByte(0.toByte()) // step
                stream.writeByte(0xFF.toByte())
                stream.writeByte(e.event.metaType)
                val size = Math.min(0x77, total - written)
                stream.writeByte((size + 8).toByte())
                stream.write(String.format("DM:{0:D04}:", idx++).toByteArray(), 0, 8)
                stream.write(e.event.data, written, size)
                written += size
            } while (written < total)
            return 0
        }
    }
}

class SmfReader(private var stream: InputStream) {

    var music = MidiMusic ()

    private val data = music

    fun read () {
        if (readByte() != 'M'.toByte()
                || readByte() != 'T'.toByte()
                || readByte() != 'h'.toByte()
                || readByte() != 'd'.toByte())
            throw parseError("MThd is expected")
        if (readInt32() != 6)
            throw parseError("Unexpected data size (should be 6)")
        data.format = readInt16().toByte()
        val tracks = readInt16()
        data.deltaTimeSpec = readInt16().toByte()
        for (i in 0..tracks)
            data.tracks.add(readTrack())
    }

    private fun readTrack () : MidiTrack {
        val tr = MidiTrack()
        if (
                readByte() != 'M'.toByte()
                || readByte() != 'T'.toByte()
                || readByte() != 'r'.toByte()
                || readByte() != 'k'.toByte())
            throw parseError("MTrk is expected")
        val trackSize = readInt32()
        current_track_size = 0
        var total = 0
        while (current_track_size < trackSize) {
            val delta = readVariableLength()
            tr.messages.add(readMessage(delta))
            total += delta
        }
        if (current_track_size != trackSize)
            throw parseError("Size information mismatch")
        return tr
    }

    private var current_track_size : Int = 0
    private var running_status : Byte = 0

    private fun readMessage (deltaTime : Int) : MidiMessage
    {
        val b = peekByte ()
        running_status = if (b < 0x80) running_status else readByte ()
        val len: Int
        when (running_status) {
            MidiEvent.SYSEX, MidiEvent.SYSEX_2, MidiEvent.META -> {
                val metaType = if (running_status == MidiEvent.META) readByte () else 0
                len = readVariableLength()
                val args = ByteArray(len)
                if (len > 0)
                    readBytes(args)
                return MidiMessage (deltaTime, MidiEvent (running_status, metaType, 0, args))
            }
            else -> {
                var value = running_status.toInt()
                value += readByte().toInt() shl 8
                if (MidiEvent.fixedDataSize(running_status) == 2.toByte())
                    value += readByte().toInt() shl 16
                return MidiMessage (deltaTime, MidiEvent (value))
            }
        }
    }

    private fun readBytes (args : ByteArray)
    {
        current_track_size += args.size
        var start = 0
        if (peek_byte >= 0) {
            args [0] = peek_byte.toByte()
            peek_byte = -1
            start = 1
        }
        val len = stream.read (args, start, args.size - start)
        try {
            if (len < args.size - start)
                throw parseError (String.format ("The stream is insufficient to read %d bytes specified in the SMF message. Only %d bytes read.", args.size, len))
        } finally {
            stream_position += len
        }
    }

    private fun readVariableLength () : Int
    {
        var v = 0
        var i = 0
        while (i < 4) {
            val b = readByte ()
            v = (v shl 7) + b
            if (b < 0x80)
                return v
            v -= 0x80
            i++
        }
        throw parseError ("Delta time specification exceeds the 4-byte limitation.")
    }

    private var peek_byte : Int = -1
    private var stream_position : Int = 0

    private fun peekByte () : Byte
    {
        if (peek_byte < 0)
            peek_byte = stream.read()
        if (peek_byte < 0)
            throw parseError ("Insufficient stream. Failed to read a byte.")
        return peek_byte.toByte()
    }

    private fun readByte () : Byte
    {
        try {

            current_track_size++
            if (peek_byte >= 0) {
                val b = peek_byte.toByte()
                peek_byte = -1
                return b
            }
            val ret = stream.read()
            if (ret < 0)
                throw parseError ("Insufficient stream. Failed to read a byte.")
            return ret.toByte()

        } finally {
            stream_position++
        }
    }

    private fun readInt16 () : Short
    {
        return ((readByte ().toInt() shl 8) + readByte ()).toShort()
    }

    private fun readInt32 () : Int
    {
        return (((readByte ().toInt() shl 8) + readByte ().toInt() shl 8) + readByte ().toInt() shl 8) + readByte ()
    }

    private fun parseError (msg : String) : Exception
    {
        return parseError (msg, null)
    }

    private fun parseError (msg : String, innerException : Exception? ) : Exception
    {
        if (innerException == null)
            throw SmfParserException (String.format ("$msg(at %s)", stream_position))
        else
            throw SmfParserException (String.format ("$msg(at %s)", stream_position), innerException)
    }
}

class SmfParserException : Exception
{
    constructor () : this ("SMF parser error")
    constructor (message : String) : super (message)
    constructor (message : String, innerException : Exception) : super (message, innerException)
}

class SmfTrackMerger(private var source: MidiMusic) {
    companion object {

        fun merge (source : MidiMusic) : MidiMusic
        {
            return SmfTrackMerger (source).getMergedMessages ()
        }
    }

    // FIXME: it should rather be implemented to iterate all
    // tracks with index to messages, pick the track which contains
    // the nearest event and push the events into the merged queue.
    // It's simpler, and costs less by removing sort operation
    // over thousands of events.
    private fun getMergedMessages () : MidiMusic {
        var l = ArrayList<MidiMessage>()

        for (track in source.tracks) {
            var delta = 0
            for (mev in track.messages) {
                delta += mev.deltaTime
                l.add(MidiMessage(delta, mev.event))
            }
        }

        if (l.size == 0) {
            val ret = MidiMusic()
            ret.deltaTimeSpec = source.deltaTimeSpec // empty (why did you need to sort your song file?)
            return ret
        }

        // Sort() does not always work as expected.
        // For example, it does not always preserve event
        // orders on the same channels when the delta time
        // of event B after event A is 0. It could be sorted
        // either as A->B or B->A.
        //
        // To resolve this ieeue, we have to sort "chunk"
        // of events, not all single events themselves, so
        // that order of events in the same chunk is preserved
        // i.e. [AB] at 48 and [CDE] at 0 should be sorted as
        // [CDE] [AB].

        val idxl = ArrayList<Int>(l.size)
        idxl.add(0)
        var prev = 0
        var i = 0
        while (i < l.size) {
            if (l[i].deltaTime != prev) {
                idxl.add(i)
                prev = l[i].deltaTime
            }
            i++
        }
        idxl.sortWith(Comparator{ i1, i2 -> l [i1].deltaTime-l [i2].deltaTime})

        // now build a new event list based on the sorted blocks.
        val l2 = ArrayList<MidiMessage>(l.size)
        var idx: Int
        while (i < idxl.size) {
            idx = idxl[i]
            prev = l[idx].deltaTime
            while (idx < l.size && l[idx].deltaTime == prev) {
                l2.add(l[idx])
                idx++
            }
            i++
        }
        l = l2

        // now messages should be sorted correctly.

        var waitToNext = l[0].deltaTime
        i = 0
        while (i < l.size - 1) {
            if (l[i].event.value != 0) { // if non-dummy
                val tmp = l[i + 1].deltaTime - l[i].deltaTime
                l[i] = MidiMessage(waitToNext, l[i].event)
                waitToNext = tmp
            }
            i++
        }
        l[l.size - 1] = MidiMessage(waitToNext, l[l.size - 1].event)

        val m = MidiMusic()
        m.deltaTimeSpec = source.deltaTimeSpec
        m.format = 0
        m.tracks.add(MidiTrack(l))
        return m
    }
}

class SmfTrackSplitter(var source: MutableList<MidiMessage>, deltaTimeSpec: Byte) {
    companion object {
        fun split(source: MutableList<MidiMessage>, deltaTimeSpec: Byte): MidiMusic {
            return SmfTrackSplitter(source, deltaTimeSpec).split()
        }
    }

    private var delta_time_spec = deltaTimeSpec
    private var tracks = HashMap<Int,SplitTrack> ()

    internal class SplitTrack(var trackID: Int) {

        var totalDeltaTime : Int
        var track : MidiTrack = MidiTrack ()

        fun addMessage (deltaInsertAt : Int, e : MidiMessage)
        {
            val e2 = MidiMessage (deltaInsertAt - totalDeltaTime, e.event)
            track.messages.add (e2)
            totalDeltaTime = deltaInsertAt
        }

        init {
            totalDeltaTime = 0
        }
    }

    private fun getTrack (track : Int) : SplitTrack
    {
        var t = tracks [track]
        if (t == null) {
            t = SplitTrack (track)
            tracks [track] = t
        }
        return t
    }

    // Override it to customize track dispatcher. It would be
    // useful to split note messages out from non-note ones,
    // to ease data reading.
    private fun getTrackID (e : MidiMessage) : Int
    {
        return when (e.event.eventType) {
            MidiEvent.META, MidiEvent.SYSEX, MidiEvent.SYSEX_2 -> -1
            else -> e.event.channel.toInt()
        }
    }

    private fun split () : MidiMusic
    {
        var totalDeltaTime = 0
        for (e in source) {
            totalDeltaTime += e.deltaTime
            val id: Int = getTrackID(e)
            getTrack(id).addMessage(totalDeltaTime, e)
        }

        val m = MidiMusic ()
        m.deltaTimeSpec = delta_time_spec
        for (t in tracks.values)
            m.tracks.add (t.track)
        return m
    }

    init {
        val mtr = SplitTrack (-1)
        tracks[-1] = mtr
    }
}
