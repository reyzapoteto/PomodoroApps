package com.pomodoroapps.main

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.pomodoroapps.Adapter.TaskAdapter
import com.pomodoroapps.Database.DTO.PomodoroDTO
import com.pomodoroapps.Database.DatabaseHelper
import com.pomodoroapps.R
import com.pomodoroapps.databinding.ActivityTimerApps3Binding
import java.util.*

class TimerApps : AppCompatActivity() {

    private lateinit var binding: ActivityTimerApps3Binding
    private var ifTimeNotRunning: Boolean = false
    private lateinit var countdownTimer: CountDownTimer
    private var timeLeftinMilis: Long = 0
    private var startTimeMilis: Long = 0 // = 1 menit
    private var breakActive: Boolean = false
    private var workingActive: Boolean = true

    private lateinit var addBtn: Button
    private lateinit var cancelcBtn: Button
    private lateinit var etTitleTask: EditText
    private lateinit var rgPriority: RadioGroup
    private lateinit var rbHigh: RadioButton
    private lateinit var rbLow: RadioButton
    private lateinit var rbMedium: RadioButton
    private lateinit var parentAddTaskDialog: ConstraintLayout

    private lateinit var player: MediaPlayer
    private var sizeTimeWorking: Long = 0
    private var sizeBreakTime: Long = 0

    lateinit var dbHandler: DatabaseHelper
    var backgroundColor: Int = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder
    private var channelID = "com.pomodoroapps"

    private var taskList: MutableList<PomodoroDTO>? = null
    var taskMainAdapter: TaskAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_timer_apps3)

        dbHandler = DatabaseHelper(this)
        val intent = intent
        val timeWorking = intent.getStringExtra("working")
        val breakWorking = intent.getStringExtra("break")
        val motto = intent.getStringExtra("motto")


        player = MediaPlayer.create(this@TimerApps, R.raw.alarm_finish)
        sizeTimeWorking = timeWorking!!.toLong() * 60000
        sizeBreakTime = breakWorking!!.toLong() * 60000

        setMode()
        ifTimer()

        binding.tvMotto.text = motto

        binding.btnStart.setOnClickListener {
            startTimer()
        }

        binding.imgBtnBack.setOnClickListener {
            if (taskMainAdapter!!.itemCount == 0) {
                player.stop()
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                }
            } else {
                Toast.makeText(this, "Harap selesaikan Task Anda", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imgBtnAddTask.setOnClickListener {
            val dialog = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
            val builder = AlertDialog.Builder(this)
                .setView(dialog)
            builder.setCancelable(false)
            val alertDialog = builder.show()

            rgPriority = dialog.findViewById(R.id.rgPriorityTask) as RadioGroup
            rbLow = dialog.findViewById(R.id.rbLow) as RadioButton
            rbMedium = dialog.findViewById(R.id.rbMedium) as RadioButton
            rbHigh = dialog.findViewById(R.id.rbImportant) as RadioButton
            etTitleTask = dialog.findViewById(R.id.etTask) as EditText
            addBtn = dialog.findViewById(R.id.btnCreateTask) as Button
            cancelcBtn = dialog.findViewById(R.id.btnCancelTask) as Button
            parentAddTaskDialog = dialog.findViewById(R.id.parentDialogAddTask)

            rbLow.isChecked = true
            var radioSeleceted = "c"

            rgPriority.setOnCheckedChangeListener { _, checkId ->
                when (checkId) {

                    R.id.rbLow -> {
                        radioSeleceted = "c"
                    }
                    R.id.rbMedium -> {
                        radioSeleceted = "b"
                    }
                    R.id.rbImportant -> {
                        radioSeleceted = "a"
                    }
                }
            }

            addBtn.setOnClickListener {

                if (etTitleTask.text.isEmpty()) {
                    etTitleTask.error = "Masukkan Task"
                } else {
                    val taskDTO = PomodoroDTO()
                    taskDTO.name = etTitleTask.text.toString().trim()
                    taskDTO.priority = radioSeleceted
                    taskDTO.isCompleted = false

                    dbHandler.addTask(taskDTO)
                    refreshItemTask()
                    alertDialog.dismiss()
                }
            }
            cancelcBtn.setOnClickListener {
                alertDialog.dismiss()
            }
            alertDialog.show()

        }

        binding.btnPaused.setOnClickListener {
            pausedTimer()
        }
        binding.btnGiveup.setOnClickListener {
            resetTimer()
        }

        binding.btnStartAgain.setOnClickListener {
            startTimer()
        }
    }


    override fun onStart() {
        refreshItemTask()
        super.onStart()
    }

    @SuppressLint("ResourceAsColor")
    private fun setMode() {
        if (breakActive && !workingActive) { // break mode
            startTimeMilis = sizeBreakTime
            timeLeftinMilis = startTimeMilis
            getNotification(this, "Teman Waktu", "Sudah sudah,saatnya istirahat ", 101)
            binding.tvTitle.text = resources.getString(R.string.breakmode)
            backgroundColor = ContextCompat.getColor(this, R.color.lightGreen)
            binding.backgroundTimerParent.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.Green
                )
            )
            binding.btnGiveup.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.Green
                )
            )

            binding.backgroundTimer.background.setTint(
                ContextCompat.getColor(
                    this,
                    R.color.lightGreen
                )
            )
            binding.btnStartAgain.setTextColor(ContextCompat.getColor(this, R.color.Green))
            binding.btnPaused.setTextColor(ContextCompat.getColor(this, R.color.Green))
            binding.btnStart.setTextColor(ContextCompat.getColor(this, R.color.Green))
            binding.imgBtnAddTask.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.Green
                )
            )
            binding.imgBtnBack.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.Green
                )
            )
            refreshItemTask()

        } else if (!breakActive && workingActive) { //working mode
            startTimeMilis = sizeTimeWorking
            timeLeftinMilis = startTimeMilis
            binding.tvTitle.text = resources.getString(R.string.working)
            getNotification(this, "Teman Waktu", "Saatnya Bekerja , tetap semangat", 102)
            backgroundColor = ContextCompat.getColor(this, R.color.lightRed)
            binding.backgroundTimerParent.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.Red
                )
            )
            binding.btnGiveup.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.Red
                )
            )

            binding.backgroundTimer.background.setTint(
                ContextCompat.getColor(
                    this,
                    R.color.lightRed
                )
            )
            binding.btnStartAgain.setTextColor(ContextCompat.getColor(this, R.color.Red))
            binding.btnPaused.setTextColor(ContextCompat.getColor(this, R.color.Red))
            binding.btnStart.setTextColor(ContextCompat.getColor(this, R.color.Red))
            binding.imgBtnAddTask.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.Red
                )
            )
            binding.imgBtnBack.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.Red
                )
            )
            refreshItemTask()
        }
    }

    fun refreshItemTask() {

        taskList = dbHandler.getTask()
        val folderDTO = PomodoroDTO()
        taskMainAdapter =
            TaskAdapter(this, taskList!!, backgroundColor)
        binding.rvTask.adapter = taskMainAdapter

    }


    private fun ifTimer() {
        if (ifTimeNotRunning) {
            pausedTimer()
        } else {
            startTimer()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun pausedTimer() {
        countdownTimer.cancel()
        ifTimeNotRunning = false
        binding.btnStart.visibility = View.GONE
        binding.btnGiveup.visibility = View.VISIBLE
        binding.btnStartAgain.visibility = View.VISIBLE
        binding.btnPaused.visibility = View.GONE

        binding.tvTimer.text = "Paused"

    }

    private fun resetTimer() {
        timeLeftinMilis = startTimeMilis
        updateCountDown()
        countdownTimer.cancel()
        ifTimeNotRunning = true
        binding.btnStart.visibility = View.VISIBLE
        binding.btnGiveup.visibility = View.GONE
        binding.btnPaused.visibility = View.GONE
    }

    private fun startTimer() {
        countdownTimer = object : CountDownTimer(timeLeftinMilis, 1000) {
            override fun onTick(p0: Long) {
                timeLeftinMilis = p0
                updateCountDown()
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                ifTimeNotRunning = false
                breakActive = !breakActive
                workingActive = !workingActive

                setMode()
                setAlarmSound()
                binding.btnStart.visibility = View.VISIBLE
                binding.btnGiveup.visibility = View.GONE
                binding.btnStartAgain.visibility = View.GONE
                binding.btnPaused.visibility = View.GONE

                if (binding.btnStart.isPressed) {
                    setMode()
                    ifTimer()
                }
            }

        }.start()

        ifTimeNotRunning = true
        binding.btnStart.visibility = View.GONE
        binding.btnStartAgain.visibility = View.GONE
        binding.btnGiveup.visibility = View.VISIBLE
        binding.btnPaused.visibility = View.VISIBLE

    }

    private fun setAlarmSound() {
        val timeAlarm: CountDownTimer = object : CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                player.start()
            }

            override fun onFinish() {
                //code fire after finish
                player.stop()
            }
        }
        timeAlarm.start()

    }

    private fun updateCountDown() {
        val hours = (timeLeftinMilis.toInt() / 1000) / 3600
        val minutes = ((timeLeftinMilis.toInt() / 1000) % 3600) / 60
        val second = (timeLeftinMilis.toInt() / 1000) % 60

        val timeLeftText = String.format(Locale.getDefault(), "%02d:%02d", minutes, second)
        val timeLeftHoursText =
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, second)

        if (hours > 0) {
            binding.tvTimer.text = timeLeftHoursText
        } else {
            binding.tvTimer.text = timeLeftText
        }

    }

    private fun getNotification(context: Context, title: String, message: String, notifId: Int) {

        channelID = "Channel 1"
        val channelName = "Notification Alarm"

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.background_priority)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.transparent))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setSound(alarmSound)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            builder.setChannelId(channelID)
            notificationManager.createNotificationChannel(channel)

        } else {

            val notification = builder.build()
            notificationManager.notify(notifId, notification)

        }
    }
}