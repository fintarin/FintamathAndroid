package com.fintamath.storage

import android.content.SharedPreferences
import android.content.res.Resources
import com.fintamath.R

object SettingsStorage {

    private const val defPrecision = 10

    private var sharedPref: SharedPreferences? = null
    private var resources: Resources? = null

    fun init(sharedPreferences: SharedPreferences, resources: Resources) {
        this.sharedPref = sharedPreferences
        this.resources = resources
    }

    fun getPrecision(): Int {
        return getInt(R.string.precision, defPrecision)
    }

    fun setPrecision(precision: Int) {
        setInt(R.string.precision, precision)
    }

    private fun getInt(keyId: Int, defValue: Int): Int {
        sharedPref?.apply {
            return getInt(resources?.getString(keyId), defValue)
        }

        return defValue
    }

    private fun setInt(keyId: Int, value: Int) {
        sharedPref?.apply {
            with(edit()) {
                putInt(resources?.getString(keyId), value)
                apply()
            }
        }
    }
}
