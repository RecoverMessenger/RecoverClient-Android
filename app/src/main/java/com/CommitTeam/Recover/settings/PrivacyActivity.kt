package com.CommitTeam.Recover.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.CommitTeam.Recover.R

class PrivacyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        val toolbar: Toolbar = findViewById(R.id.privacy_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Приватность"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}