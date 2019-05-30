package com.maptionary.application.Settings


import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.maptionary.application.R


class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
