package dev.atsushieno.fluidsynthmidideviceservicej

import androidx.lifecycle.*
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
        private val service = ownerService

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
            service.disposeReceivers()
        }
    }

    private var fluidsynth_receivers = mutableListOf<FluidsynthMidiReceiver>()

    internal fun disposeReceivers()
    {
        for (r in fluidsynth_receivers)
            r.dispose()
    }

    override fun onGetInputPortReceivers(): Array<MidiReceiver> {
        if (fluidsynth_receivers.isEmpty()) {
            fluidsynth_receivers = mutableListOf()
            for (i in 0 until deviceInfo.ports.size)
                fluidsynth_receivers.add(FluidsynthMidiReceiver(this))
        }
        for (i in 0 until deviceInfo.ports.size) {
            if (fluidsynth_receivers [i].isDisposed())
                fluidsynth_receivers [i] = FluidsynthMidiReceiver(this)
        }
        return fluidsynth_receivers.toTypedArray()
    }

    private val dispatcher = ServiceLifecycleDispatcher(this)

    override fun onCreate() {
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        lifecycle.addObserver(FluidsynthLifecycleObserver(this))
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

