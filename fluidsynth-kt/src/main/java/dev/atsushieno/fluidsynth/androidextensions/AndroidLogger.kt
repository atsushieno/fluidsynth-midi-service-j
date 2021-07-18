package dev.atsushieno.fluidsynth.androidextensions

import com.sun.jna.Pointer
import dev.atsushieno.fluidsynth.FluidsynthLibrary.fluid_log_level.*
import dev.atsushieno.fluidsynth.FluidsynthLibrary as library

class AndroidLogger : library.fluid_log_function_t {

    companion object {
        var library = dev.atsushieno.fluidsynth.FluidsynthLibrary.INSTANCE

        val java = AndroidLogger()

        fun installAndroidLogger() {
            library.fluid_set_log_function(FLUID_PANIC, java::apply, Pointer.NULL)
            library.fluid_set_log_function(FLUID_ERR, java::apply, Pointer.NULL)
            library.fluid_set_log_function(FLUID_WARN, java::apply, Pointer.NULL)
            library.fluid_set_log_function(FLUID_INFO, java::apply, Pointer.NULL)
            library.fluid_set_log_function(FLUID_DBG, java::apply, Pointer.NULL)
        }
    }

    override fun apply(level: Int, message: Pointer?, data: Pointer?) {
        if (message == null) throw IllegalArgumentException("null message")
        when (level) {
            FLUID_PANIC -> android.util.Log.e("FluidsynthMidiServiceJ", "PANIC: " + message.getString(0))
            FLUID_ERR -> android.util.Log.e("FluidsynthMidiServiceJ", message.getString(0))
            FLUID_WARN -> android.util.Log.w("FluidsynthMidiServiceJ", message.getString(0))
            FLUID_INFO -> android.util.Log.i("FluidsynthMidiServiceJ", message.getString(0))
            FLUID_DBG -> android.util.Log.d("FluidsynthMidiServiceJ", message.getString(0))
            else -> throw IllegalArgumentException("Unexpected log level $level")
        }
    }
}
