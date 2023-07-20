package com.reactnativesecurekeystore

import android.util.Base64
import android.util.Log
import com.reactnativesecurekeystore.exception.KeyNotFound
import com.reactnativesecurekeystore.util.Companion.getLogTag
import java.security.Key
import java.security.KeyStore
import java.security.PrivateKey


class SecureKeystoreImpl(private val keyGenerator: KeyGenerator, private val cipherBox: CipherBox) : SecureKeystore {
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

    return PemWriter.toPemString(keyPair.public)
  }

  /** Remove key with provided name from security storage.  */
  override fun removeKey(alias: String) {
    if (ks.containsAlias(alias)) {
      ks.deleteEntry(alias)
    }
  }

  override fun encryptData(alias: String, data: String): String {
    Log.i(logTag, ks.aliases().toList().toString())
    val key = getKeyOrThrow(alias)

    val encryptedOutput = cipherBox.encryptData(key, data)

    return encryptedOutput.toString()
  }

  override fun decryptData(alias: String, encryptedText: String): String {
    val key = getKeyOrThrow(alias)

    val decryptedData = cipherBox.decryptData(key, encryptedText)

    return String(decryptedData)
  }

  override fun sign(alias: String, data: String): String {
    val key = getKeyOrThrow(alias) as PrivateKey

    val signature = cipherBox.sign(key, data)

    return Base64.encodeToString(signature, Base64.DEFAULT)
  }

  private fun getKeyOrThrow(alias: String): Key {
    if (!ks.containsAlias(alias)) {
      throw KeyNotFound("Key not found for the alias: $alias")
    }

    //TODO: Check the type of key to be secret key not key pair
    return ks.getKey(alias, null)
  }
}
