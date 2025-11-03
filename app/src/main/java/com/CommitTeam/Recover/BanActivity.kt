// BanActivity.kt
package com.CommitTeam.Recover

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BanActivity : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var reasonTextView: TextView
    private lateinit var bannedAtTextView: TextView
    private lateinit var banUntilTextView: TextView
    private lateinit var timerLabelTextView: TextView
    private lateinit var timerTextView: TextView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ban)

        statusTextView = findViewById(R.id.ban_status_text_view)
        reasonTextView = findViewById(R.id.ban_reason_text_view)
        bannedAtTextView = findViewById(R.id.ban_banned_at_text_view)
        banUntilTextView = findViewById(R.id.ban_ban_until_text_view)
        timerLabelTextView = findViewById(R.id.timer_label_text_view)
        timerTextView = findViewById(R.id.timer_text_view)

        val reason = intent.getStringExtra("REASON")
        val bannedAt = intent.getLongExtra("BANNED_AT", 0L)
        val banUntil = intent.getLongExtra("BAN_UNTIL", 0L)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

        reasonTextView.text = reason
        bannedAtTextView.text = "Дата блокировки: ${dateFormat.format(Date(bannedAt))}"

        if (banUntil == 0L) {
            // Вечный бан
            statusTextView.text = "Аккаунт заблокирован навсегда"
            banUntilTextView.text = "Дата разблокировки: никогда"
            timerLabelTextView.visibility = View.GONE
            timerTextView.visibility = View.GONE
        } else {
            // Временный бан
            statusTextView.text = "Аккаунт временно заблокирован"
            banUntilTextView.text = "Дата разблокировки: ${dateFormat.format(Date(banUntil))}"
            startBanTimer(banUntil)
        }
    }

    private fun startBanTimer(endTime: Long) {
        val currentTime = System.currentTimeMillis()
        if (endTime <= currentTime) {
            timerLabelTextView.text = "Срок бана истёк."
            timerTextView.visibility = View.GONE
            return
        }

        val timeLeft = endTime - currentTime
        countDownTimer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                timerTextView.text = String.format(
                    Locale.getDefault(),
                    "%d д %d ч %d м %d с",
                    days, hours, minutes, seconds
                )
            }

            override fun onFinish() {
                timerLabelTextView.text = "Срок бана истёк."
                timerTextView.visibility = View.GONE
                // Можно добавить логику для перенаправления пользователя
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}