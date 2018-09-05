package name.atsushieno.fluidsynthjna

import com.sun.jna.ptr.PointerByReference

class Voice(handle: PointerByReference) : FluidsynthObject(handle, false)
{
    override fun onClose() {}
}