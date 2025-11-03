package com.CommitTeam.Recover.Utils

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.CommitTeam.Recover.R
import com.google.android.material.snackbar.Snackbar

object ErrorUtils {

    fun showCustomError(
        view: View,
        fragmentManager: FragmentManager,
        message: String,
        detailsJson: String? = null
    ) {
        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)
        // 1. ИСПРАВЛЕНО: Сразу приводим к ViewGroup
        val snackbarLayout = snackbar.view as ViewGroup
        snackbarLayout.setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))

        val layoutParams = snackbarLayout.layoutParams as ViewGroup.MarginLayoutParams
        val margin = (12 * view.resources.displayMetrics.density).toInt()
        layoutParams.setMargins(margin, 0, margin, margin)
        snackbarLayout.layoutParams = layoutParams

        snackbarLayout.setPadding(0, 0, 0, 0)

        // 2. ИСПРАВЛЕНО: Передаем 'snackbarLayout' как родителя, attachToRoot = false
        val customView = LayoutInflater.from(view.context)
            .inflate(R.layout.custom_error_view, snackbarLayout, false)

        val errorMessageTextView = customView.findViewById<TextView>(R.id.tv_error_message)
        errorMessageTextView.text = message

        val customErrorRoot = customView.findViewById<LinearLayout>(R.id.custom_error_root)

        val detailsPrompt = customView.findViewById<TextView>(R.id.tv_error_details_prompt)
        if (detailsJson.isNullOrEmpty()) {
            detailsPrompt.visibility = View.GONE
        } else {
            detailsPrompt.visibility = View.VISIBLE
        }

        customErrorRoot.setOnClickListener {
            ErrorDetailsFragment.newInstance(message, detailsJson)
                .show(fragmentManager, "ErrorDetailsFragment")

            snackbar.dismiss()
        }

        // 3. ИСПРАВЛЕНО: 'if' больше не нужен
        snackbarLayout.removeAllViews()
        snackbarLayout.addView(customView)

        snackbar.show()
    }

    fun showBanSnackbar(
        view: View,
        fragmentManager: FragmentManager,
        reason: String,
        banUntil: Long
    ) {
        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)
        // 1. ИСПРАВЛЕНО: Сразу приводим к ViewGroup
        val snackbarLayout = snackbar.view as ViewGroup
        snackbarLayout.setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))

        val layoutParams = snackbarLayout.layoutParams as ViewGroup.MarginLayoutParams
        val margin = (12 * view.resources.displayMetrics.density).toInt()
        layoutParams.setMargins(margin, 0, margin, margin)
        snackbarLayout.layoutParams = layoutParams

        snackbarLayout.setPadding(0, 0, 0, 0)

        // 2. ИСПРАВЛЕНО: Передаем 'snackbarLayout' как родителя, attachToRoot = false
        val customView = LayoutInflater.from(view.context)
            .inflate(R.layout.custom_error_view, snackbarLayout, false)

        val errorMessageTextView = customView.findViewById<TextView>(R.id.tv_error_message)
        errorMessageTextView.text = "Аккаунт заблокирован"

        val detailsPrompt = customView.findViewById<TextView>(R.id.tv_error_details_prompt)
        detailsPrompt.visibility = View.VISIBLE
        detailsPrompt.text = "Нажмите для просмотра деталей"

        val customErrorRoot = customView.findViewById<LinearLayout>(R.id.custom_error_root)

        customErrorRoot.setOnClickListener {
            ErrorDetailsFragment.newInstanceForBan(reason, banUntil)
                .show(fragmentManager, "BanDetailsFragment")

            snackbar.dismiss()
        }

        // 3. ИСПРАВЛЕНО: 'if' больше не нужен
        snackbarLayout.removeAllViews()
        snackbarLayout.addView(customView)

        snackbar.show()
    }
}