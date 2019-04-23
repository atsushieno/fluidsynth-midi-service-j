package name.atsushieno.fluidsynthmidideviceservicej

import android.content.Context
import android.media.AudioManager
import android.media.midi.MidiReceiver
import name.atsushieno.ktmidi.*
import kotlin.experimental.and

class ApplicationModel(context: Context) {
    companion object
    {
        lateinit var instance: ApplicationModel

        fun getInstance(context: Context) : ApplicationModel
        {
            if (!Companion::instance.isInitialized || instance.context != context.applicationContext)
                instance = ApplicationModel(context.applicationContext)
            return instance
        }
    }

    val context: Context
    val soundFonts : MutableList<String>

    var sampleRate : Int = 44100
    var framesPerBuffer : Int
    var audioExclusiveUse : Boolean = false
    var performanceMode : String = "None"
    var audioGainPercentage : Int = 100

    init {
        this.context = context
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

    var player: MidiPlayer? = null

    class Listener : OnMidiEventListener
    {
        override fun onEvent(e: MidiEvent) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    val tmp_arr = ByteArray(3)

    fun playMusic(musicAsset: String, m: MidiReceiver)
    {
        var reader = SmfReader(context.assets.open(musicAsset))
        reader.read()
        var p = MidiPlayer(reader.music)
        p.addOnEventReceivedListener(object: OnMidiEventListener {
            override fun onEvent(e: MidiEvent) {
                if (e.data != null)
                    m.send (e.data, 0, e.data!!.size)
                else {
                    var size = MidiEvent.fixedDataSize(e.statusByte)
                    tmp_arr[0] = e.statusByte
                    tmp_arr[1] = e.msb
                    tmp_arr[2] = e.lsb
                    m.send(tmp_arr, 0, size.toInt(), 0)
                }
            }
        })
        p.playbackCompletedToEnd = object: Runnable {
            override fun run() {
                player = null
            }
        }
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