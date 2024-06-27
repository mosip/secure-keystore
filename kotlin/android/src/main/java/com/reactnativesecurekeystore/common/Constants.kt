package com.reactnativesecurekeystore


/** Android Key store helps to store keys specific to app */
const val KEYSTORE_TYPE = "AndroidKeyStore"

/** Test alias to check the hardware supports TEE or strongbox */
const val CHECK_HARDWARE_SUPPORT_KEY_ALIAS = "$KEYSTORE_TYPE#checkHardwareSupport"

/** Key size. */
const val ENCRYPTION_KEY_SIZE = 256
