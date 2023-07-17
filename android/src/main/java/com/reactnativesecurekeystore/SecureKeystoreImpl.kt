package com.reactnativesecurekeystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SecureKeystoreImpl: SecureKeystore {
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

  private fun getKeyGenSpecBuilder(alias: String): KeyGenParameterSpec.Builder  {
    val purposes = KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT

    return KeyGenParameterSpec.Builder(alias, purposes)
      .setKeySize(ENCRYPTION_KEY_SIZE)
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
  }

  /** Remove key with provided name from security storage.  */
  override fun removeKey(alias: String) {
    val ks = KeyStore.getInstance("AndroidKeyStore")
    try {
      if (ks.containsAlias(alias)) {
        ks.deleteEntry(alias)
      }
    } catch (ignored: GeneralSecurityException) {
      /* only one exception can be raised by code: 'KeyStore is not loaded' */
    }
  }
}
