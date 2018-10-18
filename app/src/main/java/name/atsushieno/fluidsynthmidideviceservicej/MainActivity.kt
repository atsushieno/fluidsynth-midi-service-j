package name.atsushieno.fluidsynthmidideviceservicej

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var midi : FluidsynthMidiReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.button.setOnClickListener {
            this.button.isEnabled = false

            if (!this::midi.isInitialized)
                midi = FluidsynthMidiReceiver(this.applicationContext);
            midi.send(arrayOf(0xC0.toByte(), 0).toByteArray(), 0, 2);
            midi.send(arrayOf(90.toByte(),40,120).toByteArray(), 0, 3);
            AsyncTask.execute {
                Thread.sleep(1000)
                midi.send(arrayOf (80.toByte(),40,0).toByteArray (), 0, 3)
            }

            this.button.isEnabled = true
        }
    }
}
