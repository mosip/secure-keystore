package com.reactnativesecurekeystore

import android.security.keystore.KeyProperties
import com.reactnativesecurekeystore.dto.EncryptedOutput
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

const val CIPHER_ALGORITHM =
  "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
const val GCM_TAG_LEN = 128

class CipherBoxImpl : CipherBox {
  override fun encryptData(key: Key, data: String): EncryptedOutput {
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, key)

    val encryptedData = cipher.doFinal(data.toByteArray());

    return EncryptedOutput(encryptedData, cipher.iv)
  }

  override fun decryptData(key: Key, encryptedText: String): ByteArray {
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
    val encryptedOutput = EncryptedOutput(encryptedText);

    val spec = GCMParameterSpec(GCM_TAG_LEN, encryptedOutput.iv)
    cipher.init(Cipher.DECRYPT_MODE, key, spec)

    return cipher.doFinal(encryptedOutput.encryptedData);
  }
}
