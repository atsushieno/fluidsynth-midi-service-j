package name.atsushieno.fluidsynthmidideviceservicej

import android.content.Context
import android.media.AudioManager

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
}