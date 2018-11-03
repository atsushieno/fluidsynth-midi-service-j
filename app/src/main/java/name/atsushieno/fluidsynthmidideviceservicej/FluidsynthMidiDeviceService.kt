package name.atsushieno.fluidsynthmidideviceservicej

import android.media.midi.MidiDeviceService
import android.media.midi.MidiDeviceStatus
import android.media.midi.MidiReceiver

class FluidsynthMidiDeviceService : MidiDeviceService()
{
    var fluidsynth_receiver : FluidsynthMidiReceiver? = null

    override fun onGetInputPortReceivers(): Array<MidiReceiver> {
        if (fluidsynth_receiver == null || fluidsynth_receiver!!.isDisposed())
            fluidsynth_receiver = FluidsynthMidiReceiver (this.applicationContext)
        return arrayOf (fluidsynth_receiver as MidiReceiver)
    }

    override fun onDeviceStatusChanged(status: MidiDeviceStatus?) {
        if (status == null)
            throw IllegalArgumentException ("null status")
        if (status.isInputPortOpen(0))
            if (fluidsynth_receiver != null)
                fluidsynth_receiver!!.dispose()
        fluidsynth_receiver = null
    }
}