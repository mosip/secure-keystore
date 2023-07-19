package com.reactnativesecurekeystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.reactnativesecurekeystore.dto.EncryptedOutput
import com.reactnativesecurekeystore.util.Companion.getLogTag
import java.security.GeneralSecurityException
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"

class SecureKeystoreImpl : SecureKeystore {
  private var ks: KeyStore = KeyStore.getInstance(KEYSTORE_TYPE)
  private val logTag = getLogTag(javaClass.simpleName)

  init {
    ks.load(null);
  }

  /**  Generate secret key and store it in AndroidKeystore */
  override fun generateKey(alias: String): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(
      KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_TYPE
    )
    keyGenerator.init(
      getKeyGenSpecBuilder(alias).build()
    )
    return keyGenerator.generateKey()
  }

  /** Generate a new key pair */
  override fun generateKeyPair(alias: String) {
    val keyPairGenerator = KeyPairGenerator.getInstance(
      KeyProperties.KEY_ALGORITHM_EC, KEYSTORE_TYPE
    )
    keyPairGenerator.initialize(
      getKeyGenSpecBuilder(alias).build()
    )
    keyPairGenerator.generateKeyPair()
  }

  private fun getKeyGenSpecBuilder(alias: String): KeyGenParameterSpec.Builder {
    val purposes = KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT

    return KeyGenParameterSpec.Builder(alias, purposes)
      .setKeySize(ENCRYPTION_KEY_SIZE)
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
  }

  override fun encryptData(alias: String, data: String): String {
    try {
      val key = getOrGenerateKey(ks, alias)

      val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
      cipher.init(Cipher.ENCRYPT_MODE, key)

      val encryptedData = cipher.doFinal(data.toByteArray());
      val encryptedOutput = EncryptedOutput(encryptedData, cipher.iv)

      return encryptedOutput.toString()
    } catch (e: GeneralSecurityException) {
      Log.i(logTag, "exception in encryptData: $e")
      throw  e
      /* only one exception can be raised by code: 'KeyStore is not loaded' */
    }
  }

  override fun decryptData(alias: String, encryptedText: String): String {
    val ks = KeyStore.getInstance(KEYSTORE_TYPE)
    ks.load(null);

    try {
      val key = ks.getKey(alias, null)
      val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
      val encryptedOutput = EncryptedOutput(encryptedText);

      val spec = GCMParameterSpec(128, encryptedOutput.iv)
      cipher.init(Cipher.DECRYPT_MODE, key, spec)

      val decryptedData = cipher.doFinal(encryptedOutput.encryptedData);

      return String(decryptedData)
    } catch (e: GeneralSecurityException) {
      Log.i(logTag, "exception in decryptData: $e")
      throw  e
      /* only one exception can be raised by code: 'KeyStore is not loaded' */
    }
  }

  private fun getOrGenerateKey(ks: KeyStore, alias: String): Key? {
    if(ks.containsAlias(alias)) {
      return ks.getKey(alias, null)
    }

    Log.i(logTag, "Generating new key")
    return generateKey(alias)
  }

  /** Remove key with provided name from security storage.  */
  override fun removeKey(alias: String) {
    val ks = KeyStore.getInstance(KEYSTORE_TYPE)
    try {
      if (ks.containsAlias(alias)) {
        ks.deleteEntry(alias)
      }
    } catch (ignored: GeneralSecurityException) {
      /* only one exception can be raised by code: 'KeyStore is not loaded' */
    }
  }
}
