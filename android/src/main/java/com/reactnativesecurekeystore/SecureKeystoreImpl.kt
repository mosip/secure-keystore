package com.reactnativesecurekeystore

import Biometrics
import android.hardware.biometrics.BiometricPrompt.CryptoObject
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.reactnativesecurekeystore.dto.EncryptedOutput
import com.reactnativesecurekeystore.exception.InvalidEncryptionText
import com.reactnativesecurekeystore.exception.KeyNotFound
import com.reactnativesecurekeystore.util.Companion.getLogTag
import java.security.Key
import java.security.KeyStore
import java.security.PrivateKey

@RequiresApi(Build.VERSION_CODES.P)
class SecureKeystoreImpl(
  private val keyGenerator: KeyGenerator,
  private val cipherBox: CipherBox,
  private val biometrics: Biometrics
) : SecureKeystore {
  private var ks: KeyStore = KeyStore.getInstance(KEYSTORE_TYPE)
  private val logTag = getLogTag(javaClass.simpleName)

  init {
    ks.load(null);
  }

  /**  Generate secret key and store it in AndroidKeystore */
  override fun generateKey(alias: String) {
    keyGenerator.generateKey(alias)
  }

  /** Generate a new key pair */
  override fun generateKeyPair(alias: String): String {
    val keyPair = keyGenerator.generateKeyPair(alias)

    return PemConverter(keyPair.public).toPem()
  }

  /** Remove key with provided name from security storage.  */
  override fun removeKey(alias: String) {
    if (ks.containsAlias(alias)) {
      ks.deleteEntry(alias)
    }
  }

  override fun hasAlias(alias: String): Boolean {
    if (ks.containsAlias(alias)) {
      return true
    }
    return false
  }

  override fun encryptData(alias: String, data: String): String {
    Log.i(logTag, ks.aliases().toList().toString())
    val key = getKeyOrThrow(alias)

    val encryptedOutput = cipherBox.encryptData(key, data)

    return encryptedOutput.toString()
  }

  override fun decryptData(alias: String, encryptedText: String): String {
    val key = getKeyOrThrow(alias)
    if (!EncryptedOutput.validate(encryptedText)) {
      throw InvalidEncryptionText()
    }

    val encryptedOutput = EncryptedOutput(encryptedText)
    val decryptedData = cipherBox.decryptData(key, encryptedOutput)

    return String(decryptedData)
  }

  override fun generateHmacSha(data: String): String {
    val hmacSha = cipherBox.generateHmacSha(data)

    return String(hmacSha)
  }

  override fun sign(alias: String, data: String, promise: Promise) {
    val key = getKeyOrThrow(alias) as PrivateKey
    val signature = cipherBox.createSignature(key, data)

    biometrics.authenticate(
      signature,
      onSuccess = { cryptoObject -> onAuthSuccess(cryptoObject, promise) },
      onFailure = { errorCode, errString -> onAuthFailure(errorCode, errString, promise) }
    )
  }

  private fun onAuthSuccess(cryptoObject: CryptoObject, promise: Promise) {
    val sign = cryptoObject.signature.sign()
    promise.resolve(Base64.encodeToString(sign, Base64.DEFAULT) )
  }


  private fun onAuthFailure(errorCode: Int, errorString: String, promise: Promise) {
    Log.e(logTag, "errorCode : $errorCode, errorString: $errorString")
    promise.reject(errorCode.toString(), errorString)
  }

  private fun getKeyOrThrow(alias: String): Key {
    if (!ks.containsAlias(alias)) {
      throw KeyNotFound("Key not found for the alias: $alias")
    }

    //TODO: Check the type of key to be secret key not key pair
    return ks.getKey(alias, null)
  }
}
