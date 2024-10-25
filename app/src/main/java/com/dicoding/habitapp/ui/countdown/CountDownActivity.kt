package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE
import java.util.concurrent.TimeUnit

class CountDownActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"

        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        val viewModel = ViewModelProvider(this)[CountDownViewModel::class.java]

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        val tvCountDown = findViewById<TextView>(R.id.tv_count_down)

        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this){
            tvCountDown.text = it
        }

        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.

        viewModel.eventCountDownFinish.observe(this) {
            updateButtonState(!it)
        }

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            viewModel.startTimer()
            updateButtonState(true)

            val initialTime = viewModel.getInitialTime()
            if (initialTime != null) {
                val data = workDataOf(
                    HABIT_ID to habit.id,
                    HABIT_TITLE to habit.title
                )
                val notificationWorkManager: WorkRequest =
                    OneTimeWorkRequestBuilder<NotificationWorker>()
                        .setInputData(data)
                        .setInitialDelay(initialTime, TimeUnit.MILLISECONDS)
                        .build()

                WorkManager
                    .getInstance(this)
                    .enqueue(notificationWorkManager)
            }
        }


        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            viewModel.resetTimer()
            updateButtonState(false)
            WorkManager.getInstance(this).cancelAllWork()
        }
    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}