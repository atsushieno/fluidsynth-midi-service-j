package name.atsushieno.fluidsynthmidideviceservicej

import android.content.Context
import android.media.midi.MidiReceiver
import android.util.Log
import fluidsynth.androidextensions.AndroidLogger
import fluidsynth.androidextensions.AndroidNativeAssetSoundFontLoader
import fluidsynth.AudioDriver
import fluidsynth.Settings
import fluidsynth.SoundFontLoader
import fluidsynth.Synth

internal fun Byte.toUnsigned() = if (this < 0) 256 + this else this.toInt()

class FluidsynthMidiReceiver (val service: Context) : MidiReceiver()
{
    private val settings: Settings
    private val syn: Synth
    private val adriver: AudioDriver
    private val asset_sfloader: SoundFontLoader

    private var is_disposed = false

    init {
        System.setProperty ("jna.nosys", "false") // https://github.com/java-native-access/jna/issues/384#issuecomment-441405266
        AndroidLogger.installAndroidLogger()

        val am = ApplicationModel(service)
        settings = Settings ()
        settings.getEntry (ConfigurationKeys.SynthThreadSafeApi).setIntValue (0)
        settings.getEntry (ConfigurationKeys.SynthGain).setDoubleValue (am.audioGainPercentage / 100.0) // See https://github.com/atsushieno/fluidsynth-midi-service-j/issues/7
        //settings.getEntry (ConfigurationKeys.AudioDriver).setStringValue ("opensles")
        //settings.getEntry (ConfigurationKeys.AudioSampleFormat).setStringValue ("float")
        settings.getEntry ("audio.oboe.sharing-mode").setStringValue(if (am.audioExclusiveUse) "Exclusive" else "Shared")
        settings.getEntry ("audio.oboe.performance-mode").setStringValue(am.performanceMode)
        settings.getEntry (ConfigurationKeys.SynthSampleRate).setDoubleValue (am.sampleRate.toDouble())
        settings.getEntry (ConfigurationKeys.AudioPeriodSize).setIntValue (am.framesPerBuffer)
        //settings.getEntry (ConfigurationKeys.AudioOboeAudioErrorRecoveryMode).setStringValue ("Stop")

        // We should be able to use this alternatively, but it still has some issue that callbacks are reset in the middle, more GC pinning is likely required.
        //asset_sfloader = AndroidAssetSoundFontLoader(settings, context.assets)
        asset_sfloader = AndroidNativeAssetSoundFontLoader(settings, service.assets)
        syn = Synth (settings)
        syn.handleError = fun (errorCode: Int, wrapperError: String, nativeError: String) : Boolean {
            Log.d("FluidsynthMidiService", "$wrapperError (error code $errorCode: $nativeError)")
            when (wrapperError) {
                "noteoff operation failed" -> return true
                else -> return false
            }
        }
        syn.addSoundFontLoader (asset_sfloader)

        for (sf in am.soundFonts)
            syn.loadSoundFont (sf, false)

        adriver = AudioDriver (syn.getSettings(), syn)
        syn.systemReset()
    }

    fun isDisposed() : Boolean{
        return is_disposed
    }

    fun dispose()
    {
        //asset_sfloader.close ()
        adriver.close ()
        syn.close ()
        settings.close ()
        is_disposed = true
    }

    override fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long) {
        // FIXME: consider timestamp
        if (msg == null)
            throw IllegalArgumentException ("null msg")
        var off = offset
        var c = count
        var runningStatus = 0
        while (c > 0) {
            var stat = msg[off].toUnsigned()
            if (stat < 0x80) {
                stat = runningStatus
            } else {
                off++
                c--
            }
            runningStatus = stat
            val ch = stat and 0x0F
            when (stat and 0xF0) {
                0x80 -> syn.noteOff(ch, msg[off].toUnsigned())
                0x90 -> {
                    if (msg[off + 1].toInt() == 0)
                        syn.noteOff(ch, msg[off].toUnsigned())
                    else
                        syn.noteOn(ch, msg[off].toUnsigned(), msg[off + 1].toUnsigned())
                }
                0xA0 -> {
                    // No PAf in fluidsynth?
                }
                0xB0 -> syn.cc(ch, msg[off].toUnsigned(), msg[off + 1].toUnsigned())
                0xC0 -> syn.programChange(ch, msg[off].toUnsigned())
                0xD0 -> syn.channelPressure(ch, msg[off].toUnsigned())
                0xE0 -> syn.pitchBend(ch, msg[off].toUnsigned() + msg[off + 1].toUnsigned() * 0x80)
                0xF0 -> syn.sysex(msg.copyOfRange(off, off + c - 1), null)
            }
            when (stat and 0xF0) {
                0xC0,0xD0 -> {
                    off++
                    c--
                }
                0xF0 -> {
                    off += c - 1
                    c = 0
                }
                else -> {
                    off += 2
                    c -= 2
                }
            }
        }
    }
}