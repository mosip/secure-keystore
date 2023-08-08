package com.reactnativesecurekeystore

import android.os.Build
import android.security.keystore.KeyInfo
import android.util.Log
import java.security.GeneralSecurityException
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory


class DeviceCapability(private val secureKeystore: SecureKeystore, private val KeyGenerator: KeyGeneratorImpl) {
  private val mutex = Object()

  @Transient
  var isSupportsSecureHardware: AtomicBoolean? = null

  fun supportsHardwareKeyStore(): Boolean {
    if (isSupportsSecureHardware != null) return isSupportsSecureHardware!!.get()

    synchronized(mutex) {
      /** double check if it has value in sync block */
      if (isSupportsSecureHardware != null) return isSupportsSecureHardware!!.get()
      // Check the key stored in secure hardware using temporary key alias, and removed after checked
      val key = KeyGenerator.generateKey(CHECK_HARDWARE_SUPPORT_KEY_ALIAS,false, null)
      isSupportsSecureHardware =
        AtomicBoolean(getSecurityLevel(key) == DeviceSecurityLevel.SECURE_HARDWARE)
      Log.i("SecureStorage", "Device Supports Hardware $isSupportsSecureHardware")
      secureKeystore.removeKey(CHECK_HARDWARE_SUPPORT_KEY_ALIAS)
      return isSupportsSecureHardware!!.get()
    }
  }

  /** Get the supported level of security for provided Key instance.  */
  @Throws(GeneralSecurityException::class)
  fun getSecurityLevel(key: SecretKey): DeviceSecurityLevel {
    val keyInfo: KeyInfo = getKeyInfo(key)

    val insideSecureHardware: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      /** SecurityLevel 1 indicates SECURITY_LEVEL_TRUSTED_ENVIRONMENT
       *  SecurityLevel 2 indicates SECURITY_LEVEL_STRONGBOX */
      (keyInfo.securityLevel == 1) or (keyInfo.securityLevel == 2)
    } else {
      keyInfo.isInsideSecureHardware()
    }
    Log.i("keystore", "Device has secure Hardware $insideSecureHardware")
    if (insideSecureHardware) {
      return DeviceSecurityLevel.SECURE_HARDWARE
    }
    return DeviceSecurityLevel.SECURE_SOFTWARE
  }

  /** Get information about provided key.  */
  @Throws(GeneralSecurityException::class)
  fun getKeyInfo(key: SecretKey): KeyInfo {
    Log.i("keystore", "KeyInfo Details$key -> ${key.algorithm}")
    val secretKeyFactory = SecretKeyFactory.getInstance(key.algorithm, KEYSTORE_TYPE)
    return secretKeyFactory.getKeySpec(
      key as SecretKey?,
      KeyInfo::class.java
    ) as KeyInfo
  }
}
