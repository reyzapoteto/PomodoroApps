package com.pomodoroapps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.pomodoroapps.Adapter.TaskAdapter
import com.pomodoroapps.Database.DTO.PomodoroDTO
import com.pomodoroapps.Database.DatabaseHelper
import com.pomodoroapps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHandler: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.btnGoPomodoro.setOnClickListener {
            val etTimeWorking = binding.etWorkingTime.text.toString()
            val etTimeBreak = binding.etWorkingBreak.text.toString()
            val etMotto = binding.etMotto.text.toString()
            val statusDatabase = false
            dbHandler = DatabaseHelper(this)

            if (statusDatabase) {
                dbHandler.deleteDatabaseData()
                Toast.makeText(this, "Database Deleted", Toast.LENGTH_SHORT).show()
            } else {
                // do nothing
            }

            when {
                etTimeBreak.isEmpty() -> {
                    binding.etWorkingBreak.error = "Break time Harus Diisi"
                }
                etTimeWorking.isEmpty() -> {
                    binding.etWorkingTime.error = "Workign time Harus diisi"
                }
                else -> {
                    val intent = Intent(this, TimerApps::class.java)
                    intent.putExtra("working", etTimeWorking)
                    intent.putExtra("break", etTimeBreak)
                    intent.putExtra("motto", etMotto)
                    intent.putExtra("status", statusDatabase)

                    startActivity(intent)
                }
            }
        }
    }
}