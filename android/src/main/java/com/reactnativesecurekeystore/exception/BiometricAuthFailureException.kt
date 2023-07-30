package com.reactnativesecurekeystore.exception

import com.reactnativesecurekeystore.biometrics.Biometrics

class BiometricAuthFailureException(val code: Biometrics.ErrorCode, override val message: String): RuntimeException(message) {

}
