package fluidsynth

import com.sun.jna.ptr.PointerByReference
import fluidsynth.FluidsynthLibrary.fluid_voice_t

class Voice(handle: fluid_voice_t) : FluidsynthObject(handle, false)
{
    val native: fluid_voice_t
        get() = h as fluid_voice_t

    override fun onClose() {}
}