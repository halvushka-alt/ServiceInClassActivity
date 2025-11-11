package edu.temple.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerService: TimerService.TimerBinder? = null
    private var isBound = false

    private lateinit var textView: TextView

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            textView.text = msg.what.toString()
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            timerService = service as TimerService.TimerBinder
            timerService?.setHandler(handler)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            timerService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        findViewById<Button>(R.id.startButton).setOnClickListener {
            if (isBound) {
                if (timerService?.paused == true || timerService?.isRunning == false)
                    timerService?.start(100)
                else
                    timerService?.pause()
            }
        }

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            if (isBound) {
                timerService?.stop()
                textView.text = "100"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, TimerService::class.java).also {
            startService(it)
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }
}