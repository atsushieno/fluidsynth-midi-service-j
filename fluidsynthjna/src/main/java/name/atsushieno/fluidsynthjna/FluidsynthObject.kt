package name.atsushieno.fluidsynthjna

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

public abstract class FluidsynthObject : AutoCloseable
{
    var h : PointerByReference
    var needs_disposal : Boolean

    constructor(handle : PointerByReference, needsDisposal : Boolean)
    {
        this.h = handle
        this.needs_disposal = needsDisposal
    }

    override fun close ()
    {
        if (needs_disposal)
            onClose ()
        needs_disposal = false
    }

    abstract fun onClose ()

    fun getHandle () : PointerByReference { return h }
}
