package dev.atsushieno.fluidsynth

import com.sun.jna.ptr.PointerByReference

public class SoundFont : FluidsynthObject
{
    public constructor(handle : PointerByReference)
            : super (handle, false)
    {
    }

    override fun onClose()
    {
    }

}