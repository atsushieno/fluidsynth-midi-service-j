package fluidsynth

import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference

abstract class FluidsynthObject : AutoCloseable
{
    val h : PointerType
    var needs_disposal : Boolean

    constructor(handle : PointerType, needsDisposal : Boolean)
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

    fun getHandle () : PointerType { return h }
}
