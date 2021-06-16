package fluidsynth

import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary.fluid_sfont_t

class SoundFont : FluidsynthObject
{
    constructor(handle : fluid_sfont_t)
        : super (handle, false)
    {
    }

    val native: fluid_sfont_t
        get() = h as fluid_sfont_t

    override fun onClose()
    {
    }

}