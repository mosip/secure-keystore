package com.reactnativesecurekeystore

import android.os.Build
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Log
import com.reactnativesecurekeystore.biometrics.Biometrics
import com.reactnativesecurekeystore.common.Util.Companion.getKeyInfo
import java.security.GeneralSecurityException
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import android.content.Context


class DeviceCapability(
    private val secureKeystore: SecureKeystoreImpl,
    private val KeyGenerator: KeyGeneratorImpl,
    private val biometrics: Biometrics,
) {
    private val mutex = Object()

    @Transient
    var isSupportsSecureHardware: AtomicBoolean? = null

    fun supportsHardwareKeyStore(): Boolean {
        if (isSupportsSecureHardware != null) return isSupportsSecureHardware!!.get()

        synchronized(mutex) {
            /** double check if it has value in sync block */
            if (isSupportsSecureHardware != null) return isSupportsSecureHardware!!.get()
            // Check the key stored in secure hardware using temporary key alias, and removed after checked
            val key = KeyGenerator.generateKey(CHECK_HARDWARE_SUPPORT_KEY_ALIAS, false, null)
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

        val insideSecureHardware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            /** SecurityLevel 1 indicates SECURITY_LEVEL_TRUSTED_ENVIRONMENT
             *  SecurityLevel 2 indicates SECURITY_LEVEL_STRONGBOX */
            (keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT) or
                    (keyInfo.securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX)
        } else {
            keyInfo.isInsideSecureHardware()
        }

        return if (insideSecureHardware) {
            DeviceSecurityLevel.SECURE_HARDWARE
        } else {
            DeviceSecurityLevel.SECURE_SOFTWARE
        }
    }

    /** Get information about Device biometrics enrollment.  */
    fun hasBiometricsEnabled(context: Context): Boolean {
        return biometrics.isBiometricEnabled(context)
    }
}
