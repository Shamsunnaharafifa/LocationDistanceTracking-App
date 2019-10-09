package com.bs23.distancetrackingapp.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

object SharedPreferencesManager {

    private val APP_SETTINGS = "APP_SETTINGS"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
    }

    fun getString(context: Context, key: String): String{
        return getSharedPreferences(context).getString(key, null)
    }

    fun putString(context: Context, key: String, newValue: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(key, newValue)
        editor.commit()
    }

    fun getBoolean(context: Context, key: String): Boolean{
        return getSharedPreferences(context).getBoolean(key, false)
    }

    fun putBoolean(context: Context, key: String, newValue: Boolean) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(key, newValue)
        editor.commit()
    }

    fun getInt(context: Context, key: String): Int{
        return getSharedPreferences(context).getInt(key, 0)
    }

    fun putInt(context: Context, key: String, newValue: Int) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt(key, newValue)
        editor.commit()
    }

    fun getFloat(context: Context, key: String): Float{
        return getSharedPreferences(context).getFloat(key, 0f)
    }

    fun putFloat(context: Context, key: String, newValue: Float) {
        val editor = getSharedPreferences(context).edit()
        editor.putFloat(key, newValue)
        editor.commit()
    }

    fun getDouble(context: Context, key: String): Double{
        return java.lang.Double.longBitsToDouble(getSharedPreferences(context).getLong(key, java.lang.Double.doubleToLongBits(0.0)))
    }

    fun putDouble(context: Context, key: String, newValue: Double) {
        val editor = getSharedPreferences(context).edit()
        editor.putLong(key, java.lang.Double.doubleToRawLongBits(newValue))
    }

    fun clearAllData(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.clear()
        editor.commit()
    }

}