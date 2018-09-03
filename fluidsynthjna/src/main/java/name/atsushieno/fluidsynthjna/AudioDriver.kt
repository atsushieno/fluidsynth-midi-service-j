package name.atsushieno.fluidsynthjna

import com.sun.jna.ptr.PointerByReference
import name.atsushieno.fluidsynth.FluidsynthLibrary

public class AudioDriver : FluidsynthObject
{
    companion object {
        val library = FluidsynthLibrary.INSTANCE;
    }

    //public delegate int AudioHandler (byte [] data, float [][] outBuffer);

    public constructor(settings : Settings, synth : Synth)
        : super (library.new_fluid_audio_driver (settings.getHandle(), synth.getHandle()), true)
    {
    }

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
        library.delete_fluid_audio_driver (getHandle());
    }
}
