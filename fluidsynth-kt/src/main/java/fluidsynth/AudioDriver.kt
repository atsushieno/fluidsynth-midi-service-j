package fluidsynth

import fluidsynth.FluidsynthLibrary.fluid_audio_driver_t

class AudioDriver : FluidsynthObject
{
    //public delegate int AudioHandler (byte [] data, float [][] outBuffer);

    companion object {
        var library = fluidsynth.FluidsynthLibrary.INSTANCE
    }

    constructor(settings : Settings, synth : Synth)
        : super (library.new_fluid_audio_driver (settings.native, synth.native), true)

    val native : fluid_audio_driver_t
        get() = h as fluid_audio_driver_t

    /*
    public constructor (settings : Settings, handler : AudioHandler, dat : ByteArray)
        : super (library.new_fluid_audio_driver2 (settings.getHandle (), (dt, len, nin, inBuffer, nout, outBuffer) => {
    try {
        var bufPtrs = Array<PointerByReference> (nout);
        Marshal.Copy (outBuffer, bufPtrs, 0, nout);
        var buf = Array<Array<Float>> (nout);
        for (int i = 0; i < nout; i++)
        Marshal.Copy (bufPtrs [i], buf [i], 0, len);
        handler (dt, buf);
        return 0;
    } catch (Exception) {
        return -1;
    }
}, dat), true)
    {
    }
    */

    override fun onClose() {
        library.delete_fluid_audio_driver (native)
    }
}
