package com.CommitTeam.Recover.Utils

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.CommitTeam.Recover.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ErrorDetailsFragment : BottomSheetDialogFragment() {

    private var countDownTimer: CountDownTimer? = null

    private lateinit var banInfoGroup: LinearLayout
    private lateinit var reasonTextView: TextView
    private lateinit var banUntilTextView: TextView
    private lateinit var timerLabelTextView: TextView
    private lateinit var timerTextView: TextView

    private lateinit var errorDetailsGroup: LinearLayout
    private lateinit var titleView: TextView
    private lateinit var messageView: TextView
    private lateinit var detailsJsonView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_error_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleView = view.findViewById(R.id.tv_details_title)

        errorDetailsGroup = view.findViewById(R.id.error_details_group)
        messageView = view.findViewById(R.id.tv_details_message)
        detailsJsonView = view.findViewById(R.id.tv_details_json)

        banInfoGroup = view.findViewById(R.id.ban_info_group)
        reasonTextView = view.findViewById(R.id.ban_reason_text_view)
        banUntilTextView = view.findViewById(R.id.ban_ban_until_text_view)
        timerLabelTextView = view.findViewById(R.id.timer_label_text_view)
        timerTextView = view.findViewById(R.id.timer_text_view)

        val banUntil = arguments?.getLong(ARG_BAN_UNTIL, -1L) ?: -1L

        if (banUntil != -1L) {
            val reason = arguments?.getString(ARG_BAN_REASON) ?: "Причина не указана."
            showBanInfo(reason, banUntil)
        } else {
            val message = arguments?.getString(ARG_MESSAGE) ?: "Сообщение об ошибке отсутствует."
            val details = arguments?.getString(ARG_DETAILS)
            showErrorInfo(message, details)
        }
    }

    private fun showErrorInfo(message: String, details: String?) {
        titleView.text = "Детали ошибки"
        errorDetailsGroup.visibility = View.VISIBLE
        banInfoGroup.visibility = View.GONE

        messageView.text = message

        if (details.isNullOrEmpty()) {
            detailsJsonView.visibility = View.GONE
        } else {
            detailsJsonView.visibility = View.VISIBLE
            detailsJsonView.text = details
        }
    }

    private fun showBanInfo(reason: String, banUntil: Long) {
        titleView.text = "Аккаунт заблокирован"
        errorDetailsGroup.visibility = View.GONE
        banInfoGroup.visibility = View.VISIBLE

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

        // 1. --- ИЗМЕНЕНИЕ ---
        // Добавлена метка "Причина:", как ты просил.
        reasonTextView.text = "Причина: $reason"
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---

        if (banUntil == 0L) {
            banUntilTextView.text = "Дата разблокировки: никогда"
            timerLabelTextView.visibility = View.GONE
            timerTextView.visibility = View.GONE
        } else {
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
        countDownTimer?.cancel()
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
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    companion object {
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_DETAILS = "arg_details"
        private const val ARG_BAN_REASON = "arg_ban_reason"
        private const val ARG_BAN_UNTIL = "arg_ban_until"

        fun newInstance(message: String, details: String?): ErrorDetailsFragment {
            val fragment = ErrorDetailsFragment()
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            args.putString(ARG_DETAILS, details)
            fragment.arguments = args
            return fragment
        }

        fun newInstanceForBan(reason: String, banUntil: Long): ErrorDetailsFragment {
            val fragment = ErrorDetailsFragment()
            val args = Bundle()
            args.putString(ARG_BAN_REASON, reason)
            args.putLong(ARG_BAN_UNTIL, banUntil)
            fragment.arguments = args
            return fragment
        }
    }
}