package name.atsushieno.fluidsynthmidideviceservicej

import android.content.Context
import android.media.AudioManager
import android.media.midi.MidiReceiver
import name.atsushieno.fluidsynthjna.AudioDriver
import name.atsushieno.fluidsynthjna.Settings
import name.atsushieno.fluidsynthjna.SoundFontLoader
import name.atsushieno.fluidsynthjna.Synth
import name.atsushieno.fluidsynthjna.androidextensions.AndroidAssetSoundFontLoader
import kotlin.experimental.and


public class FluidsynthMidiReceiver// float or 16bits
public constructor(context: Context) : MidiReceiver()
{
    val predefined_temp_path = "/data/local/tmp/name.atsushieno.fluidsynthmidideviceservice";

    val syn: Synth;
    val adriver: AudioDriver;
    val asset_sfloader: SoundFontLoader;

    var is_disposed = false;

    init {
        val settings = Settings ()
        settings.getEntry (ConfigurationKeys.AudioSampleFormat).setStringValue ("16bits")
        val manager = context.getSystemService (Context.AUDIO_SERVICE) as AudioManager
        settings.getEntry(ConfigurationKeys.SynthSampleRate).setDoubleValue (11025.toDouble())
        val fpb = java.lang.Double.parseDouble (manager.getProperty (AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER))
        settings.getEntry(ConfigurationKeys.AudioPeriodSize).setIntValue (fpb.toInt())
        syn = Synth (settings)
        val sfs = MutableList<String?> (10, {_ -> null})
        SynthAndroidExtensions.getSoundFonts (sfs, context, predefined_temp_path)
        asset_sfloader = AndroidAssetSoundFontLoader(settings, context.getAssets())
        syn.addSoundFontLoader (asset_sfloader)
        for (sf in sfs)
            if (sf != null)
                syn.loadSoundFont (sf, false)
        adriver = AudioDriver (syn.getSettings(), syn)
    }

    public fun isDisposed() : Boolean{
        return is_disposed;
    }

    public fun dispose()
    {
        asset_sfloader.close ();
        adriver.close ();
        syn.close ();
        is_disposed = true;
    }

    override fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long) {
        // FIXME: consider timestamp
        if (msg == null)
            throw IllegalArgumentException ("null msg");
        val ch = msg[offset].toInt() and 0x0F
        when (msg[offset].toInt() and 0xF0) {
            0x80 -> syn.noteOff(ch, msg[offset + 1].toInt())
            0x90 -> {
                if (msg[offset + 2].toInt() == 0)
                    syn.noteOff(ch, msg[offset + 1].toInt())
                else
                    syn.noteOn(ch, msg[offset + 1].toInt(), msg[offset + 2].toInt())
            }
            0xA0 -> {
                // No PAf in fluidsynth?
            }
            0xB0 -> syn.cc(ch, msg[offset + 1].toInt(), msg[offset + 2].toInt())
            0xC0 -> syn.programChange(ch, msg[offset + 1].toInt())
            0xD0 -> syn.channelPressure(ch, msg[offset + 1].toInt())
            0xE0 -> syn.pitchBend(ch, msg[offset + 1] + msg[offset + 2] * 0x80)
            0xF0 -> syn.sysex(msg.copyOfRange(offset, count), null)
        }
    }
}