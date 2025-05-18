package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.util.PrefsUtil
import android.widget.Button
import com.example.financetracker.MainActivity


class OnboardingActivity : AppCompatActivity() {

    private lateinit var prefsUtil: PrefsUtil

    companion object {
        private const val PREF_KEY_FIRST_TIME = "first_time_launch"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)  // Use setContentView directly

        prefsUtil = PrefsUtil(this)

        // Reset first-time launch flag (for testing)
        resetFirstTimeLaunch()

        // Set button click listener manually
        val getStartedButton: Button = findViewById(R.id.btn_get_started)
        getStartedButton.setOnClickListener {
            setFirstTimeLaunchToFalse()
            navigateToMainActivity()
            finish()
        }
    }

    private fun resetFirstTimeLaunch() {
        val sharedPreferences = getSharedPreferences("FinanceTrackerPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(PREF_KEY_FIRST_TIME, true).apply()
    }

    private fun setFirstTimeLaunchToFalse() {
        val sharedPreferences = getSharedPreferences("FinanceTrackerPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean(PREF_KEY_FIRST_TIME, false).apply()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
