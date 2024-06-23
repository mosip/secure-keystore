package com.reactnativesecurekeystore

/** Minimal required level of the security implementation.  */
enum class DeviceSecurityLevel {
  /** Requires for the key to be stored in the Android Keystore, separate from the encrypted data.  */
  SECURE_SOFTWARE,

  /** Requires for the key to be stored on a secure hardware (Trusted Execution Environment or Secure Environment).  */
  SECURE_HARDWARE;
}
