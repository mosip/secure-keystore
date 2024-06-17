package com.reactnativesecurekeystore

import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.reactnativesecurekeystore.dto.EncryptedOutput
import java.security.Key
import java.security.PrivateKey
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.ASN1InputStream
import java.io.ByteArrayInputStream


const val CIPHER_ALGORITHM =
  "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
const val GCM_TAG_LEN = 128
const val HMAC_ALGORITHM = "HmacSHA256"

class CipherBoxImpl : CipherBox {

  override fun initEncryptCipher(key: Key): Cipher {
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
    cipher.init(Cipher.ENCRYPT_MODE, key)

    return cipher
  }

  override fun encryptData(cipher: Cipher, data: String): EncryptedOutput {
    val decodedData = Base64.decode(data, Base64.DEFAULT)
    val encryptedData = cipher.doFinal(decodedData, 0, decodedData.size)

    return EncryptedOutput(encryptedData, cipher.iv)
  }

  override fun initDecryptCipher(key: Key, encryptedOutput: EncryptedOutput): Cipher {
    val cipher = Cipher.getInstance(CIPHER_ALGORITHM)

    val spec = GCMParameterSpec(GCM_TAG_LEN, encryptedOutput.iv)
    cipher.init(Cipher.DECRYPT_MODE, key, spec)

    return cipher
  }

  override fun decryptData(cipher: Cipher, encryptedOutput: EncryptedOutput): ByteArray {
    try {
      return cipher.doFinal(encryptedOutput.encryptedData, 0, encryptedOutput.encryptedData.size)
    } catch (e: Exception) {
      Log.e("Secure","Exception in Decryption",e)
      throw e
    }
  }

  override fun createSignature(key: PrivateKey,signAlgorithm: String): Signature {
    val signature = Signature.getInstance(signAlgorithm).run {
      initSign(key)
      this
    }

    return signature
  }

  override fun sign(signature: Signature, data: String, signAlgorithm: String): String {
    try {
      val bytes = data.toByteArray(charset("UTF8"))
      val sign = signature.run {
        update(bytes)
        sign()
      }
      if(signAlgorithm.equals("SHA256withECDSA"))
      {
        val rsFormatSignature = convertDerToRsFormat(sign)
        return Base64.encodeToString(rsFormatSignature, Base64.DEFAULT)
      }
      return Base64.encodeToString(sign, Base64.DEFAULT)
    } catch (e: Exception) {
      Log.e("CipherBox", "Exception in sign creation", e)
      throw e
    }
  }

  private fun convertDerToRsFormat(derSignature: ByteArray): ByteArray {
    val asn1InputStream = ASN1InputStream(ByteArrayInputStream(derSignature))
    val seq = asn1InputStream.readObject() as ASN1Sequence
    val r = (seq.getObjectAt(0) as ASN1Integer).value
    val s = (seq.getObjectAt(1) as ASN1Integer).value

    val rBytes = r.toByteArray()
    val sBytes = s.toByteArray()

    val rPadded = ByteArray(32)
    val sPadded = ByteArray(32)

    val rTrimmed = if (rBytes.size > 32) rBytes.copyOfRange(rBytes.size - 32, rBytes.size) else rBytes
    val sTrimmed = if (sBytes.size > 32) sBytes.copyOfRange(sBytes.size - 32, sBytes.size) else sBytes

    System.arraycopy(rTrimmed, 0, rPadded, 32 - rTrimmed.size, rTrimmed.size)
    System.arraycopy(sTrimmed, 0, sPadded, 32 - sTrimmed.size, sTrimmed.size)

    return rPadded + sPadded
  }


  override fun generateHmacSha(key: SecretKey, data: String): ByteArray {
    val mac = Mac.getInstance(HMAC_ALGORITHM)

    try {
      mac.init(key)
    } catch (e: Exception) {
      Log.e("debugging", "Exception in generatehmac", e)
      throw e
    }

    return mac.doFinal(data.toByteArray())
  }
}
