package com.reactnativesecurekeystore
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class SecureKeystoreModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val keystore = SecureKeystoreImpl()
    private val deviceCapability = DeviceCapability(keystore)

    override fun getName(): String {
        return "SecureKeystore"
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    fun deviceSupportsHardware(): Boolean {
      val supportsHardware = deviceCapability.supportsHardwareKeyStore()
      Log.i("keystore--","Device supports Hardware $supportsHardware")
      return supportsHardware
    }
}
