package com.reactnativesecurekeystore

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class SecureKeystoreModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val keyGenerator = KeyGeneratorImpl()
  private val cipherBox = CipherBoxImpl()
  private val keystore = SecureKeystoreImpl(keyGenerator, cipherBox)
  private val deviceCapability = DeviceCapability(keystore, keyGenerator)
  private val logTag = util.getLogTag(javaClass.simpleName)

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
  fun generateKey(alias: String) {
    Log.d(logTag, "Generating a key for $alias")

    keystore.generateKey(alias)
  }

  // Generates KeyPair and returns Public key
  @ReactMethod(isBlockingSynchronousMethod = true)
  fun generateKeyPair(alias: String): String {
    Log.d(logTag, "Generating a keyPair for $alias")
    val publicKey = keystore.generateKeyPair(alias)
    Log.d(logTag, "Publickey$publicKey")
    return publicKey
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun encryptData(alias: String, data: String): String {
    Log.d(logTag, "Encrypting data: $data")
    return keystore.encryptData(alias, data)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun decryptData(alias: String, encryptedText: String): String {
    Log.d(logTag, "decrypting data: $encryptedText")
    return keystore.decryptData(alias, encryptedText)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun generateHmacSha(data: String): String {
    Log.d(logTag, "generating HACH Sha for data: $data")
    return keystore.generateHmacSha(data)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun sign(alias: String, data: String): String {
    Log.d(logTag, "signing data: $data")
    return keystore.sign(alias, data)
  }
}
