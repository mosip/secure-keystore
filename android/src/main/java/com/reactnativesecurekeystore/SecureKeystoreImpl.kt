package com.reactnativesecurekeystore

import android.util.Log
import androidx.biometric.BiometricPrompt.CryptoObject
import com.reactnativesecurekeystore.biometrics.Biometrics
import com.reactnativesecurekeystore.common.PemConverter
import com.reactnativesecurekeystore.common.util.Companion.getLogTag
import com.reactnativesecurekeystore.dto.EncryptedOutput
import com.reactnativesecurekeystore.exception.InvalidEncryptionText
import com.reactnativesecurekeystore.exception.KeyNotFound
import kotlinx.coroutines.runBlocking
import java.security.Key
import java.security.KeyStore
import java.security.PrivateKey
import javax.crypto.SecretKey

class SecureKeystoreImpl(
  private val keyGenerator: KeyGenerator, private val cipherBox: CipherBox, private val biometrics: Biometrics
) : SecureKeystore {
  private var ks: KeyStore = KeyStore.getInstance(KEYSTORE_TYPE)
  private val logTag = getLogTag(javaClass.simpleName)
  private val mutex = Object()

  init {
    ks.load(null);
  }

  /**  Generate secret key and store it in AndroidKeystore */
  override fun generateKey(alias: String, isAuthRequired: Boolean, authTimeout: Int?) {
    keyGenerator.generateKey(alias, isAuthRequired, authTimeout)
  }

  /** Generate a new key pair */
  override fun generateKeyPair(alias: String, isAuthRequired: Boolean, authTimeout: Int?): String {
    val keyPair = keyGenerator.generateKeyPair(alias, isAuthRequired, authTimeout)

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

  override fun encryptData(
    alias: String,
    data: String,
    onSuccess: (encryptedText: String) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  ) {
    val key = getKeyOrThrow(alias)

    runBlocking {
      val preAction = {
        CryptoObject(cipherBox.initEncryptCipher(key))
      }

      val action = { cryptoObject: CryptoObject ->
        val encryptedText = cipherBox.encryptData(cryptoObject.cipher!!, data).toString()
        onSuccess(encryptedText)
      }

      biometrics.authenticateAndPerform(preAction, action, onFailure)
    }
  }

  override fun decryptData(
    alias: String, encryptedText: String,
    onSuccess: (data: String) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  ) {
    val key = getKeyOrThrow(alias)

    if (!EncryptedOutput.validate(encryptedText)) {
      throw InvalidEncryptionText()
    }

    runBlocking {
      val encryptedOutput = EncryptedOutput(encryptedText)

      val preAction = { CryptoObject(cipherBox.initDecryptCipher(key, encryptedOutput)) }

      val action = { cryptoObject: CryptoObject ->
        val data = cipherBox.decryptData(cryptoObject.cipher!!, encryptedOutput)
        onSuccess(String(data))
      }

      biometrics.authenticateAndPerform(
        preAction,
        action,
        onFailure
      )
    }
  }

  override fun sign(
    alias: String, data: String,
    onSuccess: (signature: String) -> Unit, onFailure: (code: Int, message: String) -> Unit
  ) {
    val key = getKeyOrThrow(alias) as PrivateKey

    runBlocking {
      val preAction = { CryptoObject(cipherBox.createSignature(key, data)) }

      val action = { cryptoObject: CryptoObject ->
        val signatureText = cipherBox.sign(cryptoObject.signature!!)
        onSuccess(signatureText)
      }

      biometrics.authenticateAndPerform(
        preAction,
        action,
        onFailure
      )
    }
  }

  override fun generateHmacSha(
    alias: String, data: String,
    onSuccess: (sha: String) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  ): String {
    val key = getKeyOrThrow(alias) as SecretKey

    val hmacSha: ByteArray

    try {
      hmacSha = cipherBox.generateHmacSha(key, data)

      return String(hmacSha)
    } catch (e: RuntimeException) {
      Log.e(logTag, "Exception in Hmac generation: ", e)
      throw e
    }
  }

  private fun getKeyOrThrow(alias: String): Key {
    if (!ks.containsAlias(alias)) {
      throw KeyNotFound("Key not found for the alias: $alias")
    }

    //TODO: Check the type of key to be secret key not key pair
    return ks.getKey(alias, null)
  }
}
