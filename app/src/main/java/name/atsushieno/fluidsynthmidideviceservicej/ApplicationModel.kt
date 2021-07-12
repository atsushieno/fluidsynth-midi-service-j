package name.atsushieno.fluidsynthmidideviceservicej

import android.content.Context
import android.media.AudioManager
import android.media.midi.MidiReceiver
import dev.atsushieno.ktmidi.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ApplicationModel(context: Context) {
    /*
    companion object
    {
        lateinit var instance: ApplicationModel

        fun getInstance(context: Context) : ApplicationModel
        {
            if (!Companion::instance.isInitialized || instance.context != context.applicationContext)
                instance = ApplicationModel(context.applicationContext)
            return instance
        }
    }*/

    var portCount = 1
    val soundFonts : MutableList<String>

    var sampleRate : Int = 44100
    var framesPerBuffer : Int
    var audioExclusiveUse : Boolean = false
    var performanceMode : String = "None"
    var audioGainPercentage : Int = 100

    init {
        val manager = context.getSystemService (Context.AUDIO_SERVICE) as AudioManager
        framesPerBuffer = manager.getProperty (AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER).toInt()
        sampleRate = manager.getProperty (AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE).toInt()
        soundFonts = mutableListOf()
        SynthAndroidExtensions.getSoundFonts (soundFonts, context, null)
    }

    var sampleRateString
        get() = sampleRate.toString()
        set(v) { sampleRate = v.toInt() }
    var framesPerBufferString
        get() = framesPerBuffer.toString()
        set(v) { framesPerBuffer = v.toInt() }
    var playerStateString
        get() = if (player != null && player!!.state == PlayerState.PLAYING) "Stop" else "Play"
        set(v) { /* not expected to update player state via this property*/ }

    var player: MidiPlayer? = null

    class Listener : OnMidiEventListener
    {
        override fun onEvent(e: MidiEvent) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private var tmp_arr = ByteArray(3)

    private lateinit var midiOutput : MidiOutput

    suspend fun playMusic(musicAsset: String, receiver: FluidsynthMidiReceiver) {
        val music = MidiMusic()
        val stream = receiver.service.assets.open(musicAsset)
        music.read(stream.readBytes().toList())
        stream.close()

        val midiOutput = emptyMidiAccess.openOutputAsync(emptyMidiAccess.outputs.first().id)
        val p = MidiPlayer(music, midiOutput)
        p.addOnMessageListener(object: OnMidiMessageListener {
            override fun onMessage(msg: MidiMessage) {
                val e = msg.event
                if (e.extraData != null) {
                    // FIXME: ugh, this is ugly. Can we make changes to how we pass data array?
                    if (tmp_arr.size < e.extraDataLength + 1)
                        tmp_arr = ByteArray(e.extraDataLength + 1)
                    tmp_arr[0] = e.statusByte
                    e.extraData!!.copyInto(tmp_arr, 1, e.extraDataOffset, e.extraDataOffset + e.extraDataLength)
                    receiver.send(tmp_arr, 0, tmp_arr.size)
                } else {
                    var size = MidiEvent.fixedDataSize(e.statusByte)
                    tmp_arr[0] = e.statusByte
                    tmp_arr[1] = e.msb
                    tmp_arr[2] = e.lsb
                    receiver.send(tmp_arr, 0, size.toInt(), 0)
                }
            }
        })
        p.playbackCompletedToEnd = Runnable { player = null }
        p.play()
        this.player = p
    }

    fun isPlayingMusic() = player != null && player!!.state == PlayerState.PLAYING

    fun stopMusic()
    {
        if (player == null)
            return
        player!!.stop()
    }
}