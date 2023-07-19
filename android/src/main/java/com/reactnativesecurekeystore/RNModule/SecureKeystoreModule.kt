package com.reactnativesecurekeystore
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class SecureKeystoreModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val keystore = SecureKeystoreImpl()
    private val deviceCapability = DeviceCapability(keystore)
    private val logTag = util.getLogTag(javaClass.simpleName)

    override fun getName(): String {
        return "SecureKeystore"
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun deviceSupportsHardware(): Boolean {
      val supportsHardware = deviceCapability.supportsHardwareKeyStore()
      Log.d(logTag,"Device supports Hardware $supportsHardware")
      return supportsHardware
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun encryptData(alias: String, data: String): String {
      Log.d(logTag,"Encrypting data: $data")
      return keystore.encryptData(alias, data)
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun decryptData(alias: String, encryptedText: String): String {
      Log.d(logTag,"decrypting data: $encryptedText")
      return keystore.decryptData(alias, encryptedText)
    }
}
