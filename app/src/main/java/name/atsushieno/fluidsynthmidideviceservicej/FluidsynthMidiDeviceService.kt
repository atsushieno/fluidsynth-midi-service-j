package name.atsushieno.fluidsynthmidideviceservicej

import android.media.midi.MidiDeviceService
import android.media.midi.MidiReceiver

public class FluidsynthMidiDeviceService : MidiDeviceService()
{
    var fluidsynth_receiver : FluidsynthMidiReceiver? = null;

    override fun onGetInputPortReceivers(): Array<MidiReceiver> {
        if (fluidsynth_receiver == null || fluidsynth_receiver!!.isDisposed())
            fluidsynth_receiver = FluidsynthMidiReceiver (this);
        return arrayOf (fluidsynth_receiver as MidiReceiver);
    }
}