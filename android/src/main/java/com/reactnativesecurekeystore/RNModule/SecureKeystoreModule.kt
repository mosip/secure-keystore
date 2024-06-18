package com.reactnativesecurekeystore

import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.reactnativesecurekeystore.biometrics.Biometrics
import com.reactnativesecurekeystore.common.Util
import kotlin.math.log

class SecureKeystoreModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val keyGenerator = KeyGeneratorImpl()
  private val cipherBox = CipherBoxImpl()
  private val biometrics = Biometrics(reactContext)
  private val keystore = SecureKeystoreImpl(keyGenerator, cipherBox, biometrics)
  private val deviceCapability = DeviceCapability(keystore, keyGenerator, biometrics)
  private val logTag = Util.getLogTag(javaClass.simpleName)

  override fun getName(): String {
    return "SecureKeystore"
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun deviceSupportsHardware(): Boolean {
    val supportsHardware = deviceCapability.supportsHardwareKeyStore()
    Log.d(logTag, "Device supports Hardware $supportsHardware")
    return supportsHardware
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun hasAlias(alias: String): Boolean {
    val exists = keystore.hasAlias(alias)
    Log.d(logTag, "Alias Exist $exists")
    return exists
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun updatePopup(title: String, description: String) {
    Biometrics.updatePopupDetails(title, description)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun generateKey(alias: String, isAuthRequired: Boolean, authTimeout: Int?) {
    Log.d(logTag, "Generating a key for $alias")

    keystore.generateKey(alias, isAuthRequired, authTimeout)
  }

  // Generates KeyPair and returns Public key
  @ReactMethod(isBlockingSynchronousMethod = true)
  fun generateKeyPair(alias: String, isAuthRequired: Boolean, authTimeout: Int?): String {
    Log.d(logTag, "Generating a keyPair")
    return keystore.generateKeyPair(alias, isAuthRequired, authTimeout)
  }

  // Generates hmacsha256 key
  @ReactMethod(isBlockingSynchronousMethod = true)
  fun generateHmacshaKey(alias: String) {
    Log.d(logTag, "Generating a generateHmacsha256 Key")
    keystore.generateHmacSha256Key(alias)
  }

  @ReactMethod
  fun encryptData(alias: String, data: String, promise: Promise) {
    Log.d(logTag, "Encrypting data")

    keystore.encryptData(
      alias,
      data,
      onSuccess = { encryptedText -> run { promise.resolve(encryptedText) } },
      onFailure = { code, message -> run { promise.reject(code.toString(), message) } })
  }

  @ReactMethod
  fun decryptData(alias: String, encryptedText: String, promise: Promise) {
    Log.d(logTag, "decrypting data with $alias")

    keystore.decryptData(
      alias,
      encryptedText,
      onSuccess = { data -> run { promise.resolve(data) } },
      onFailure = { code, message -> run { promise.reject(code.toString(), message) } })
  }

  @ReactMethod
  fun generateHmacSha(alias: String, data: String, promise: Promise) {
    Log.d(logTag, "generating HMAC Sha for data")

    keystore.generateHmacSha(alias, data,
      onSuccess = { sha -> run { promise.resolve(sha) } },
      onFailure = { code, message -> run { promise.reject(code.toString(), message) } })
  }

  @ReactMethod
  fun sign(signAlgoritm: String, alias: String, data: String, promise: Promise) {
    Log.d(logTag, "signing data")

    keystore.sign(
      signAlgoritm,
      alias,
      data,
      onSuccess = { signature -> run { promise.resolve(signature) } },
      onFailure = { code, message -> run { promise.reject(code.toString(), message) } })
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun clearKeys() {
    keystore.removeAllKeys()
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun hasBiometricsEnabled(): Boolean {
    val isEnabled = deviceCapability.hasBiometricsEnabled()
    Log.d(logTag, "Device biometrics enabled -> $isEnabled")
    return isEnabled
  }
}
