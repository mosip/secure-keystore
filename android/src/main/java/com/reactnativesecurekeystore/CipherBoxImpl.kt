package com.reactnativesecurekeystore

import com.reactnativesecurekeystore.dto.EncryptedOutput
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

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

    val spec = GCMParameterSpec(128, encryptedOutput.iv)
    cipher.init(Cipher.DECRYPT_MODE, key, spec)

    return cipher.doFinal(encryptedOutput.encryptedData);
  }
}
