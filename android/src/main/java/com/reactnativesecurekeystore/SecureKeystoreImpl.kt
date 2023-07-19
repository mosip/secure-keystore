package com.reactnativesecurekeystore

import android.security.keystore.KeyProperties
import android.util.Log
import com.reactnativesecurekeystore.exception.KeyNotFound
import com.reactnativesecurekeystore.util.Companion.getLogTag
import java.security.GeneralSecurityException
import java.security.Key
import java.security.KeyPair
import java.security.KeyStore
import javax.crypto.SecretKey

const val CIPHER_ALGORITHM =
  "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"

class SecureKeystoreImpl(private val keyGenerator: KeyGenerator, private val cipherBox: CipherBox) : SecureKeystore {
  private var ks: KeyStore = KeyStore.getInstance(KEYSTORE_TYPE)
  private val logTag = getLogTag(javaClass.simpleName)

  init {
    ks.load(null);
  }

  /**  Generate secret key and store it in AndroidKeystore */
  override fun generateKey(alias: String): SecretKey {
    return keyGenerator.generateKey(alias)
  }

  /** Generate a new key pair */
  override fun generateKeyPair(alias: String): KeyPair {
    return keyGenerator.generateKeyPair(alias)
  }

  /** Remove key with provided name from security storage.  */
  override fun removeKey(alias: String) {
    try {
      if (ks.containsAlias(alias)) {
        ks.deleteEntry(alias)
      }
    } catch (ignored: GeneralSecurityException) {
      /* only one exception can be raised by code: 'KeyStore is not loaded' */
    }
  }

  override fun encryptData(alias: String, data: String): String {
    try {
      val key = getOrGenerateKey(ks, alias)

      val encryptedOutput = cipherBox.encryptData(key, data)

      return encryptedOutput.toString()
    } catch (e: GeneralSecurityException) {
      Log.i(logTag, "exception in encryptData: $e")
      throw  e
      /* only one exception can be raised by code: 'KeyStore is not loaded' */
    }
  }

  override fun decryptData(alias: String, encryptedText: String): String {
    try {
      if(!ks.containsAlias(alias)) {
        throw KeyNotFound("Key not found for the alias: $alias")
      }

      val key = ks.getKey(alias, null)

      val decryptedData = cipherBox.decryptData(key, encryptedText)

      return String(decryptedData)
    } catch (e: GeneralSecurityException) {
      Log.i(logTag, "exception in decryptData: $e")
      throw  e
      /* only one exception can be raised by code: 'KeyStore is not loaded' */
    }
  }

  private fun getOrGenerateKey(ks: KeyStore, alias: String): Key {
    if (ks.containsAlias(alias)) {
      return ks.getKey(alias, null)
    }

    Log.i(logTag, "Generating new key")
    return generateKey(alias)
  }
}
