package name.atsushieno.ktmidi

import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

enum class PlayerState {
    STOPPED,
    PLAYING,
    PAUSED,
}

internal class MidiEventLooper(var messages: List<MidiMessage>, timeManager: MidiPlayerTimeManager, deltaTimeSpec: Int) : AutoCloseable
{
    var starting : Runnable? = null
    var finished : Runnable? = null
    var playbackCompletedToEnd: Runnable? = null

    private val time_manager: MidiPlayerTimeManager = timeManager
    private val delta_time_spec: Int = deltaTimeSpec

    private val pause_lock = ReentrantLock()
    private val pause_handle = pause_lock.newCondition()

    private var do_pause: Boolean = false
    private var do_stop: Boolean = false
    var tempoRatio : Double = 1.0

    var state : PlayerState
    private var event_idx = 0
    var currentTempo = MidiMetaType.DEFAULT_TEMPO
    var currentTimeSignature = ByteArray(4)
    var playDeltaTime: Int = 0
    private val event_received_handlers = arrayListOf<OnMidiEventListener>()

    init {
        if (deltaTimeSpec < 0)
            throw UnsupportedOperationException ("SMPTe-based delta time is not implemented in this player.")
        state = PlayerState.STOPPED
    }

    fun addOnEventReceivedListener(listener: OnMidiEventListener)
    {
        event_received_handlers.add(listener)
    }

    fun removeOnEventReceivedListener(listener: OnMidiEventListener)
    {
        event_received_handlers.remove(listener)
    }

    override fun close ()
    {
        if (state != PlayerState.STOPPED)
            stop ()
        mute ()
    }

    fun play ()
    {
        pause_lock.withLock { pause_handle.signal() }
        state = PlayerState.PLAYING
    }

    fun mute ()
    {
        for (i in 0..15)
            onEvent (MidiEvent (i + 0xB0, 0x78, 0, null))
    }

    fun pause ()
    {
        do_pause = true
        mute ()
    }

    fun playerLoop ()
    {
        starting?.run ()

        event_idx = 0
        playDeltaTime = 0
        var doWait = true
        while (true) {
            if (doWait) {
                pause_lock.withLock { pause_handle.await() }
                doWait = false
            }
            if (do_stop)
                break
            if (do_pause) {
                doWait = true
                do_pause = false
                state = PlayerState.PAUSED
                continue
            }
            if (event_idx == messages.size)
                break
            processMessage (messages [event_idx++])
        }
        do_stop = false
        mute ()
        state = PlayerState.STOPPED
        if (event_idx == messages.size)
            playbackCompletedToEnd?.run ()
        finished?.run ()
    }

    fun getContextDeltaTimeInMilliseconds (deltaTime : Int) : Int {
        return (currentTempo / 1000 * deltaTime / delta_time_spec / tempoRatio).toInt()
    }

    private fun processMessage (m : MidiMessage)
    {
        if (seek_processor != null) {
            val result = seek_processor!!.filterMessage (m)
            when (result) {
                SeekFilterResult.PASS_AND_TERMINATE,
                SeekFilterResult.BLOCK_AND_TERMINATE ->
                    seek_processor = null
            }

            when (result) {
                SeekFilterResult.BLOCK,
                SeekFilterResult.BLOCK_AND_TERMINATE ->
                return // ignore this event
            }
        }
        else if (m.deltaTime != 0) {
            val ms = getContextDeltaTimeInMilliseconds (m.deltaTime)
            time_manager.waitBy (ms)
            playDeltaTime += m.deltaTime
        }

        if (m.event.statusByte == 0xFF.toByte()) {
            if (m.event.msb == MidiMetaType.TEMPO)
                currentTempo = MidiMetaType.getTempo (m.event.data!!)
            else if (m.event.msb == MidiMetaType.TIME_SIGNATURE && m.event.data!!.size == 4)
                m.event.data.copyInto(currentTimeSignature, 4)
        }

        onEvent (m.event)
    }

    private fun onEvent (m: MidiEvent)
    {
        for (er in event_received_handlers)
            er.onEvent(m)
    }

    fun stop ()
    {
        if (state != PlayerState.STOPPED) {
            do_stop = true
            pause_lock.withLock { pause_handle.signal() }
            finished?.run ()
        }
    }

    private var seek_processor: SeekProcessor? = null

    // not sure about the interface, so make it non-public yet.
    internal fun seek (seekProcessor: SeekProcessor?, ticks: Int)
    {
        seek_processor = seekProcessor ?: SimpleSeekProcessor (ticks)
        event_idx = 0
        playDeltaTime = ticks
        mute ()
    }
}

interface OnMidiEventListener
{
    fun onEvent (e: MidiEvent)
}

// Provides asynchronous player control.
class MidiPlayer : AutoCloseable
{
    constructor(music: MidiMusic )
        : this (music, MidiAccessManager.EMPTY)
    {
    }

    constructor(music: MidiMusic , access: MidiAccess )
        : this (music, access, SimpleAdjustingMidiPlayerTimeManager ())
    {
    }

    constructor(music: MidiMusic, output: MidiOutput )
        : this (music, output, SimpleAdjustingMidiPlayerTimeManager ())
    {
    }

    constructor(music: MidiMusic, timeManager: MidiPlayerTimeManager )
        : this (music, MidiAccessManager.EMPTY, timeManager)
    {
    }

    constructor(music: MidiMusic , access: MidiAccess , timeManager: MidiPlayerTimeManager )
        : this (music, access.openOutputAsync (access.outputs.first ().id), timeManager)
    {
        should_dispose_output = true
    }

    constructor(music: MidiMusic, output: MidiOutput , timeManager: MidiPlayerTimeManager ) {
        this.music = music
        this.output = output

        messages = SmfTrackMerger.merge(music).tracks[0].messages
        player = MidiEventLooper(messages, timeManager, music.deltaTimeSpec.toUnsigned())
        player.starting = Runnable {
            // all control reset on all channels.
            for (i in 0..15) {
                buffer[0] = (i + 0xB0).toByte()
                buffer[1] = 0x79
                buffer[2] = 0
                output.send(buffer, 0, 3, 0)
            }
        }

        val listener = object : OnMidiEventListener {
            override fun onEvent(m: MidiEvent) {
                when (m.eventType) {
                    MidiEvent.NOTE_OFF,
                    MidiEvent.NOTE_ON -> {
                        if (channel_mask != null && channel_mask!![m.channel.toUnsigned()])
                            return // ignore messages for the masked channel.
                    }
                    MidiEvent.SYSEX,
                    MidiEvent.SYSEX_2 -> {
                        if (buffer.size <= m.data!!.size)
                            buffer = ByteArray(buffer.size * 2)
                        buffer[0] = m.statusByte
                        m.data.copyInto(buffer, 1,0, m.data.size - 1)
                        output.send(buffer, 0, m.data.size + 1, 0)
                        return
                    }
                    MidiEvent.META -> {
                        // do nothing.
                        return
                    }
                }
                val size = MidiEvent.fixedDataSize(m.statusByte)
                buffer[0] = m.statusByte
                buffer[1] = m.msb
                buffer[2] = m.lsb
                output.send(buffer, 0, size + 1, 0)
            }
        }
        addOnEventReceivedListener(listener)
    }

    fun addOnEventReceivedListener(listener: OnMidiEventListener)
    {
        player.addOnEventReceivedListener(listener)
    }

    fun removeOnEventReceivedListener(listener: OnMidiEventListener)
    {
        player.removeOnEventReceivedListener(listener)
    }

    private val player: MidiEventLooper
    // FIXME: it is still awkward to have it here. Move it into MidiEventLooper.
    private var sync_player_task: Job? = null
    private val output: MidiOutput
    private val messages: MutableList<MidiMessage>
    private val music: MidiMusic

    private var should_dispose_output: Boolean = false
    private var buffer = ByteArray(0x100)
    private var channel_mask : BooleanArray? = null

    var finished : Runnable?
        get () = player.finished
        set (v) { player.finished = v }

    var playbackCompletedToEnd : Runnable?
        get () = player.playbackCompletedToEnd
        set (v) { player.playbackCompletedToEnd = v }

    val state : PlayerState
        get () = player.state

    var tempoChangeRatio : Double
        get () = player.tempoRatio
        set (v) { player.tempoRatio = v }

    var tempo : Int
        get () = player.currentTempo
        set (v) { player.currentTempo = v }

    val npm : Int
        get () = (60.0 / tempo * 1000000.0).toInt()

    // You can break the data at your own risk but I take performance precedence.
    val timeSignature
        get () = player.currentTimeSignature

    val playDeltaTime
        get () = player.playDeltaTime

    val positionInTime : Long
        get () = music.getTimePositionInMillisecondsForTick (playDeltaTime).toLong()

    fun getTotalPlayTimeMilliseconds (): Int
    {
        return MidiMusic.getTotalPlayTimeMilliseconds (messages, music.deltaTimeSpec.toUnsigned())
    }

    override fun close ()
    {
        player.stop ()
        if (should_dispose_output)
            output.close ()
    }

    private fun startLoop ()
    {
        sync_player_task = GlobalScope.launch {
            player.playerLoop ()
            sync_player_task = null
        }
    }

    fun play ()
    {
        when (state) {
            PlayerState.PLAYING-> return // do nothing
            PlayerState.PAUSED-> { player.play (); return; }
            PlayerState.STOPPED-> {
                if (sync_player_task == null)
                    startLoop()
                player.play()
            }
        }
    }

    fun pause ()
    {
        when (state) {
            PlayerState.PLAYING ->  { player.pause (); return;}
            else -> return
        }
    }

    fun stop ()
    {
        when (state) {
            PlayerState.PAUSED,
            PlayerState.PLAYING -> player.stop ()
        }
    }

    fun seek (ticks: Int)
    {
        player.seek (null, ticks)
    }

    fun setChannelMask (channelMask: BooleanArray?)
    {
        if (channelMask != null && channelMask.size != 16)
            throw IllegalArgumentException ("Unexpected length of channelMask array; it must be an array of 16 elements.")
        channel_mask = channelMask
        // additionally send all sound off for the muted channels.
        for (ch in 0..15)
            if (channelMask == null || channelMask [ch])
                output.send (arrayOf((0xB0 + ch).toByte(), 120, 0).toByteArray(), 0, 3, 0)
    }
}

interface SeekProcessor
{
    fun filterMessage (message: MidiMessage ) : SeekFilterResult
}

enum class SeekFilterResult {
    PASS,
    BLOCK,
    PASS_AND_TERMINATE,
    BLOCK_AND_TERMINATE,
}

internal class SimpleSeekProcessor(ticks: Int) : SeekProcessor
{
    private var seek_to: Int = ticks
    private var current: Int = 0

    override fun filterMessage (message: MidiMessage ) : SeekFilterResult
    {
        current += message.deltaTime
        if (current >= seek_to)
            return SeekFilterResult.PASS_AND_TERMINATE
        when (message.event.eventType) {
            MidiEvent.NOTE_ON, MidiEvent.NOTE_OFF ->  return SeekFilterResult.BLOCK
        }
        return SeekFilterResult.PASS
    }
}
