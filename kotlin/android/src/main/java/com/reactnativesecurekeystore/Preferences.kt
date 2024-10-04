package com.reactnativesecurekeystore;

interface Preferences {

    fun getPreference(key: String, defaultValue: String):String
    fun savePreference(key: String, value: String)
    fun clearPreferences()

    fun hasAlias(alias: String):Boolean

}
