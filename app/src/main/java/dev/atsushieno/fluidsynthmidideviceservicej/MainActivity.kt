package dev.atsushieno.fluidsynthmidideviceservicej

import android.app.Application
import androidx.lifecycle.*
import android.content.Context
import androidx.databinding.DataBindingUtil
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import dev.atsushieno.fluidsynthmidideviceservicej.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LifecycleObserver {

    // FIXME: this needs complete rewrite.
    class MainActivityViewModel(application: Application) : AndroidViewModel(application)
    {
        var model: ApplicationModel = ApplicationModel(application)

        var performanceModeAdapter : ArrayAdapter<String> =
            ArrayAdapter(application, android.R.layout.simple_dropdown_item_1line, arrayOf("None", "LowLatency", "PowerSaving"))
        var midiMusicAdapter: ArrayAdapter<String> =
            ArrayAdapter (application, android.R.layout.simple_dropdown_item_1line, application.assets.list("")!!.filter { f -> f.endsWith(".mid", true) })
        var soundFontAdapter: ArrayAdapter<String> =
            ArrayAdapter (application, android.R.layout.simple_dropdown_item_1line, model.soundFonts)

    }

    private lateinit var midi : FluidsynthMidiReceiver
    private lateinit var midi_manager : MidiManager
    private var midi_input : MidiInputPort? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onLifecyclePause () { disposeInput() }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onLifecycleStop () { disposeInput() }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifecycleDestroy () { disposeInput() }

    private fun disposeInput()
    {
        vm.model.stopMusic()
        if (midi_input != null) {
            Log.d("FluidsynthMidiService", "[MainActivity] disposed midi input")
            midi_input?.close()
            midi_input = null
        }
    }

    private lateinit var vm: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycle.addObserver(this)

        vm = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        val binding : ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = vm

        findViewById<Button>(R.id.button_direct).setOnClickListener {
            findViewById<Button>(R.id.button_direct).isEnabled = false

            if (!this::midi.isInitialized)
                midi = FluidsynthMidiReceiver(this)
            play_client_midi(midi)

            findViewById<Button>(R.id.button_direct).isEnabled = true
        }

        findViewById<Button>(R.id.button_client).setOnClickListener {

            if (!this::midi_manager.isInitialized) {
                findViewById<Button>(R.id.button_client).isEnabled = false

                midi_manager = this.applicationContext.getSystemService(Context.MIDI_SERVICE) as MidiManager
                val device = midi_manager.devices.first { d -> d.properties.getString(MidiDeviceInfo.PROPERTY_PRODUCT).equals("JFluidMidi") }
                midi_manager.openDevice(device, {
                    midi_input = it.openInputPort(0)
                    this.runOnUiThread { findViewById<Button>(R.id.button_client).isEnabled = true }
                    play_client_midi(midi_input!!)
                }, null)
            }
            else
                play_client_midi(midi_input!!)
        }

        findViewById<Button>(R.id.button_play_smf).setOnClickListener {

            if (!this::midi.isInitialized)
                midi = FluidsynthMidiReceiver(this)
            if (vm.model.isPlayingMusic()) {
                vm.model.stopMusic()
                findViewById<Button>(R.id.button_play_smf).text = getString(R.string.play)
            } else {
                GlobalScope.launch {
                    runBlocking { vm.model.playMusic(getSelectedMusic(), midi) }
                    vm.model.player!!.playbackCompletedToEnd = Runnable {
                        runOnUiThread { findViewById<Button>(R.id.button_play_smf).text = getString(R.string.play) }
                    }
                    runOnUiThread { findViewById<Button>(R.id.button_play_smf).text = getString(R.string.stop) }
                }
            }
        }
    }

    fun getSelectedSoundFont() = findViewById<Spinner>(R.id.spinner_soundfont).selectedItem as String
    fun getSelectedMusic() = findViewById<Spinner>(R.id.spinner_songs).selectedItem as String

    fun play_client_midi (midi : MidiReceiver)
    {
        midi.send(arrayOf(0xB0.toByte(), 7, 120).toByteArray(), 0, 3)
        midi.send(arrayOf(0xC0.toByte(), 48).toByteArray(), 0, 2)
        midi.send(arrayOf(0x90.toByte(), 0x40, 120).toByteArray(), 0, 3)
        midi.send(arrayOf(0x90.toByte(), 0x44, 120).toByteArray(), 0, 3)
        midi.send(arrayOf(0x90.toByte(), 0x47, 120).toByteArray(), 0, 3)
        GlobalScope.launch {
            delay(1000)
            midi.send(arrayOf(0x90.toByte(), 0x49, 120).toByteArray(), 0, 3)
            GlobalScope.launch {
                delay(1000)
                midi.send(arrayOf(0x80.toByte(), 0x40, 0).toByteArray(), 0, 3)
                midi.send(arrayOf(0x80.toByte(), 0x44, 0).toByteArray(), 0, 3)
                midi.send(arrayOf(0x80.toByte(), 0x47, 0).toByteArray(), 0, 3)
                midi.send(arrayOf(0x80.toByte(), 0x49, 0).toByteArray(), 0, 3)
            }
        }
    }
}
