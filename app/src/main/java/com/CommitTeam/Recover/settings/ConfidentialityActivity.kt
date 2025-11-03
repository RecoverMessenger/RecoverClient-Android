package com.CommitTeam.Recover.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.CommitTeam.Recover.R

class ConfidentialityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confidentiality)

        val toolbar: Toolbar = findViewById(R.id.confidentiality_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Конфиденциальность"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}