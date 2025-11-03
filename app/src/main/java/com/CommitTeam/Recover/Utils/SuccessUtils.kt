package com.CommitTeam.Recover.Utils

// import android.util.Log // 1. ИСПРАВЛЕНО: Ненужный импорт удален
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.CommitTeam.Recover.R
import com.google.android.material.snackbar.Snackbar


object SuccessUtils {

    /**
     *
     * @param view Любая View на текущем экране
     * @param message Сообщение, которое нужно показать.
     */
    fun showSuccessSnackbar(view: View, message: String) {

        // Длительность LENGTH_SHORT, т.к. успех не нужно долго читать
        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
        val snackbarLayout = snackbar.view as ViewGroup // Приводим к ViewGroup
        snackbarLayout.setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))

        val layoutParams = snackbarLayout.layoutParams as ViewGroup.MarginLayoutParams
        val margin = (12 * view.resources.displayMetrics.density).toInt()
        layoutParams.setMargins(margin, 0, margin, margin)
        snackbarLayout.layoutParams = layoutParams

        snackbarLayout.setPadding(0, 0, 0, 0)

        // Надуваем наш зеленый layout (исправлено с 'null' на 'snackbarLayout')
        val customView = LayoutInflater.from(view.context)
            .inflate(R.layout.custom_success_view, snackbarLayout, false)

        val successMessageTextView = customView.findViewById<TextView>(R.id.tv_success_message)
        successMessageTextView.text = message

        snackbarLayout.removeAllViews()
        snackbarLayout.addView(customView)

        snackbar.show()
    }
}