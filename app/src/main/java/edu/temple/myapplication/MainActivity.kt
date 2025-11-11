package edu.temple.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.util.Log

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
            Log.d("MainActivity", "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            timerService = null
            Log.d("MainActivity", "Service disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)

        startButton.setOnClickListener {
            if (isBound) {
                when {
                    timerService?.paused == true -> {
                        timerService?.pause() // resume
                        Log.d("MainActivity", "Timer resumed")
                    }
                    timerService?.isRunning == false -> {
                        timerService?.start(100)
                        Log.d("MainActivity", "Timer started")
                    }
                    else -> {
                        timerService?.pause() // pause
                        Log.d("MainActivity", "Timer paused")
                    }
                }
            } else {
                Log.d("MainActivity", "Service not bound yet")
            }
        }

        stopButton.setOnClickListener {
            if (isBound) {
                timerService?.stop()
                textView.text = "0"
                Log.d("MainActivity", "Timer stopped")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, TimerService::class.java)
        startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Log.d("MainActivity", "Binding to service...")
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
            isBound = false
            Log.d("MainActivity", "Unbound from service")
        }
        stopService(Intent(this, TimerService::class.java))
    }
}