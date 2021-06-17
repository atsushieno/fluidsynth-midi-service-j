package name.atsushieno.fluidsynthmidideviceservicej

import androidx.lifecycle.*
import android.content.Context
import androidx.databinding.DataBindingUtil
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import name.atsushieno.fluidsynthmidideviceservicej.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LifecycleObserver {

    class MainActivityViewModel : ViewModel()
    {
        lateinit var view: MainActivity
        lateinit var model: ApplicationModel

        fun setContext(owner: MainActivity)
        {
            view = owner
            model = ApplicationModel.getInstance(owner)
            performanceModeAdapter = ArrayAdapter(view, android.R.layout.simple_dropdown_item_1line, arrayOf("None", "LowLatency", "PowerSaving"))
            midiMusicAdapter = ArrayAdapter (view, android.R.layout.simple_dropdown_item_1line, view.assets.list("")!!.filter { f -> f.endsWith(".mid", true) })
            soundFontAdapter = ArrayAdapter (view, android.R.layout.simple_dropdown_item_1line, model.soundFonts)
        }

        lateinit var performanceModeAdapter : ArrayAdapter<String>
        lateinit var midiMusicAdapter: ArrayAdapter<String>
        lateinit var soundFontAdapter: ArrayAdapter<String>

        fun getSelectedSoundFont() = view.findViewById<Spinner>(R.id.spinner_soundfont).selectedItem as String
        fun getSelectedMusic() = view.findViewById<Spinner>(R.id.spinner_songs).selectedItem as String
    }

    lateinit var midi : FluidsynthMidiReceiver
    lateinit var midi_manager : MidiManager
    var midi_input : MidiInputPort? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onLifecyclePause () { disposeInput() }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onLifecycleStop () { disposeInput() }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifecycleDestroy () { disposeInput() }

    fun disposeInput()
    {
        vm.model.stopMusic()
        if (midi_input != null) {
            Log.d("FluidsynthMidiService", "[MainActivity] disposed midi input")
            midi_input?.close()
            midi_input = null
        }
    }

    lateinit var vm: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycle.addObserver(this)

        vm = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        vm.setContext(this)
        val binding : ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = vm

        findViewById<Button>(R.id.button_direct).setOnClickListener {
            findViewById<Button>(R.id.button_direct).isEnabled = false

            if (!this::midi.isInitialized)
                midi = FluidsynthMidiReceiver(this.applicationContext)
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
                midi = FluidsynthMidiReceiver(this.applicationContext)
            if (vm.model.isPlayingMusic()) {
                vm.model.stopMusic()
                findViewById<Button>(R.id.button_play_smf).text = getString(R.string.play)
            } else {
                vm.model.playMusic(vm.getSelectedMusic(), midi)
                vm.model.player!!.playbackCompletedToEnd = Runnable { runOnUiThread { findViewById<Button>(R.id.button_play_smf).text = getString (R.string.play) } }
                findViewById<Button>(R.id.button_play_smf).text = getString(R.string.stop)
            }
        }
    }

    fun play_client_midi (midi : MidiReceiver)
    {
        midi.send(arrayOf(0xB0.toByte(), 7, 120).toByteArray(), 0, 3)
        midi.send(arrayOf(0xC0.toByte(), 48).toByteArray(), 0, 2)
        midi.send(arrayOf(0x90.toByte(), 0x40, 120).toByteArray(), 0, 3)
        midi.send(arrayOf(0x90.toByte(), 0x44, 120).toByteArray(), 0, 3)
        midi.send(arrayOf(0x90.toByte(), 0x47, 120).toByteArray(), 0, 3)
        AsyncTask.execute {
            Thread.sleep(1000)
            midi.send(arrayOf(0x90.toByte(), 0x49, 120).toByteArray(), 0, 3)
            AsyncTask.execute {
                Thread.sleep(1000)
                midi.send(arrayOf(0x80.toByte(), 0x40, 0).toByteArray(), 0, 3)
                midi.send(arrayOf(0x80.toByte(), 0x44, 0).toByteArray(), 0, 3)
                midi.send(arrayOf(0x80.toByte(), 0x47, 0).toByteArray(), 0, 3)
                midi.send(arrayOf(0x80.toByte(), 0x49, 0).toByteArray(), 0, 3)
            }
        }
    }
}
