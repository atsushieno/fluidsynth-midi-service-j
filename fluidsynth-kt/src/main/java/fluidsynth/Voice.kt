package fluidsynth

import com.sun.jna.ptr.PointerByReference

class Voice(handle: PointerByReference) : FluidsynthObject(handle, false)
{
    override fun onClose() {}
}