package com.reactnativesecurekeystore

import android.security.keystore.KeyProperties
import android.util.Log
import com.reactnativesecurekeystore.dto.EncryptedOutput
import java.security.Key
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec

const val CIPHER_ALGORITHM =
  "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
const val GCM_TAG_LEN = 128
const val SIGN_ALGORITHM = "SHA256with${KeyProperties.KEY_ALGORITHM_RSA}"
const val HMAC_ALGORITHM = "HmacSHA256"

class CipherBoxImpl : CipherBox {
  override fun encryptData(key: Key, data: String): EncryptedOutput {
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, key)

    val encryptedData = cipher.doFinal(data.toByteArray());

    return EncryptedOutput(encryptedData, cipher.iv)
  }

  override fun createSignature(key: PrivateKey, data: String): Signature {
    val hash = data.toByteArray(charset("UTF8"))
    val signature = Signature.getInstance(SIGN_ALGORITHM).run {
      initSign(key)
      update(hash)
      this
    }

    return signature
  }

  override fun generateHmacSha(key: Key, data: String): ByteArray {
    val mac = Mac.getInstance(HMAC_ALGORITHM);

    try{
      mac.init(key)
    } catch (e: Exception) {
      Log.e("debugging", "Exception in generatehmac", e)
    }

    return mac.doFinal()
  }

  override fun decryptData(key: Key, encryptedOutput: EncryptedOutput): ByteArray {
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)

    val spec = GCMParameterSpec(GCM_TAG_LEN, encryptedOutput.iv)
    cipher.init(Cipher.DECRYPT_MODE, key, spec)

    return cipher.doFinal(encryptedOutput.encryptedData);
  }
}
