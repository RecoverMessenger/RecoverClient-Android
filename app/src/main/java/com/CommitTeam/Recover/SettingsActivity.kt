package com.CommitTeam.Recover

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.CommitTeam.Recover.settings.ConfidentialityActivity
import com.CommitTeam.Recover.settings.EditProfileActivity
import com.CommitTeam.Recover.settings.PrivacyActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var editProfile: TextView
    private lateinit var privacy: TextView
    private lateinit var confidentiality: TextView
    private lateinit var sourceCode: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_new)

        toolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editProfile = findViewById(R.id.settings_edit_profile)
        privacy = findViewById(R.id.settings_privacy)
        confidentiality = findViewById(R.id.settings_confidentiality)
        sourceCode = findViewById(R.id.settings_source_code)

        editProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        privacy.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        confidentiality.setOnClickListener {
            startActivity(Intent(this, ConfidentialityActivity::class.java))
        }

        sourceCode.setOnClickListener {
            val url = "https://github.com/RecoverMessenger/RecoverClient-Android"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}