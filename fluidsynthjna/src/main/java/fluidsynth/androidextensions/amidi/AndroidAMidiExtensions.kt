package fluidsynth.androidextensions.amidi

import android.content.res.AssetManager
import android.media.midi.MidiDevice
import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary as library
import fluidsynth.Settings
import fluidsynth.Synth


class NativeHandler
{
    companion object {
        val library_android_amidi= fluidsynthandroidamidi.FluidsynthAndroidAmidiLibrary.INSTANCE

        var asset_manager_java : AssetManager? = null

        external fun setAssetManagerContext(assetManager: AssetManager)
        external fun getMidiDeviceHandle(midiDevice: MidiDevice) : PointerByReference

        fun connectMidiDeviceToSynth(settings : Settings, midiDevice: MidiDevice, synth: Synth)
        {
            library_android_amidi.fluid_android_connect_midi_driver_to_synth(settings.getHandle(), synth.getHandle(), getMidiDeviceHandle(midiDevice).pointer)
        }
    }
}
