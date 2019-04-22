package name.atsushieno.fluidsynthmidideviceservicej

import android.arch.lifecycle.*
import android.content.Intent
import android.media.midi.MidiDeviceService
import android.media.midi.MidiDeviceStatus
import android.media.midi.MidiReceiver
import android.os.IBinder
import android.util.Log

class FluidsynthMidiDeviceService : MidiDeviceService(), LifecycleOwner
{
    class FluidsynthLifecycleObserver(ownerService : FluidsynthMidiDeviceService) : LifecycleObserver
    {
        val service = ownerService

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy ()
        {
            dispose ();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop()
        {
            dispose ();
        }

        fun dispose ()
        {
            Log.d("FluidsynthMidiService", "[MidiDeviceService] being disposed.")
            service.fluidsynth_receiver?.dispose()
            service.fluidsynth_receiver = null
        }
    }

    var fluidsynth_receiver : FluidsynthMidiReceiver? = null

    override fun onGetInputPortReceivers(): Array<MidiReceiver> {
        if (fluidsynth_receiver == null || fluidsynth_receiver!!.isDisposed()) {
            fluidsynth_receiver = FluidsynthMidiReceiver(this.applicationContext)
            lifecycle.addObserver(FluidsynthLifecycleObserver(this))
        }
        return arrayOf (fluidsynth_receiver as MidiReceiver)
    }

    val dispatcher = ServiceLifecycleDispatcher(this)

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    @SuppressWarnings("deprecation")
    override fun onStart(intent: Intent?, startId: Int) {
        dispatcher.onServicePreSuperOnStart();
        super.onStart(intent, startId)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun getLifecycle(): Lifecycle {
        return dispatcher.lifecycle
    }
}

