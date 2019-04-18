package fluidsynth.androidextensions

import android.media.midi.MidiDevice
import fluidsynth.FluidsynthObject

public class AMidi(device: MidiDevice) : FluidsynthObject(NativeHandler.INSTANCE.getAMidiDevice(device), true)
{
    companion object {
        var library = fluidsynthandroidamidi.FluidsynthAndroidAmidiLibrary.INSTANCE
    }

    override fun onClose() {
        NativeHandler.INSTANCE.releaseAMidiDevice(getHandle())
    }
}