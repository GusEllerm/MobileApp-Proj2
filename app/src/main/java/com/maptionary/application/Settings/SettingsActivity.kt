package com.maptionary.application.Settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()


        val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val darkModeBool: Boolean = sharedPrefs.getBoolean(DARK_MODE_SWITCH, false)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        Toast.makeText(this, darkModeBool.toString(), Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        val DARK_MODE_SWITCH = "darkMode"
    }
}
