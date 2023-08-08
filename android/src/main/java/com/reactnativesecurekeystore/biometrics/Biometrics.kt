package com.reactnativesecurekeystore.biometrics

import BiometricPromptAuthCallback
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.UiThreadUtil
import com.reactnativesecurekeystore.common.util
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.crypto.IllegalBlockSizeException

class Biometrics(
  private val context: ReactApplicationContext
) {
  private val logTag = util.getLogTag(javaClass.simpleName)

  enum class ErrorCode {
    INTERNAL_ERROR,
    CANCELLED_BY_USER,
  }

  fun authenticateAndPerform(
    preAuthCryptoObject: BiometricPrompt.CryptoObject,
    action: (BiometricPrompt.CryptoObject) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  ) {
    try {
      Log.d(logTag, "Calling action for auth")
      action(preAuthCryptoObject)
    } catch (e: IllegalBlockSizeException) {
      Log.e(logTag, "Calling action failed due to auth exception", e)

      authenticate(preAuthCryptoObject,
        onSuccess = { cryptoObject -> onBiometricAuthSuccess(cryptoObject, action, onFailure) },
        onFailure = { errorCode, errString -> onBiometricAuthFailure(errorCode, errString, onFailure) })
    } catch (e: Exception) {
      Log.e(logTag, "Calling action failed due to other exception", e)

      onFailure(ErrorCode.INTERNAL_ERROR.ordinal, e.message.toString())
    }
  }


  private fun onBiometricAuthSuccess(
    cryptoObject: BiometricPrompt.CryptoObject?,
    action: (BiometricPrompt.CryptoObject) -> Unit,
    onFailure: (code: Int, message: String) -> Unit
  ) {
    Log.d(logTag, "auth success")
    try {
      action(cryptoObject!!)
    } catch (e: Exception) {
      Log.e(logTag, "auth success but invalid crypto object or action failed", e)
      Log.e(logTag, "cause: ", e.cause)
      onFailure(ErrorCode.INTERNAL_ERROR.ordinal, e.message.toString())
    }
  }

  private fun onBiometricAuthFailure(
    errorCode: ErrorCode, errorString: String, onFailure: (code: Int, message: String) -> Unit
  ) {
    Log.e(logTag, "error in biometric auth: errorCode : $errorCode, errorString: $errorString")
    onFailure(errorCode.ordinal, errorString)
  }

  private fun authenticate(
    cryptoObject: BiometricPrompt.CryptoObject,
    onSuccess: (cryptoObject: BiometricPrompt.CryptoObject?) -> Unit,
    onFailure: (errorCode: ErrorCode, errString: String) -> Unit
  ) {
    Log.e(logTag, "Running on UI thread to show popup: auth called")

    UiThreadUtil.runOnUiThread {
      try {
        Log.e(logTag, "Running on UI thread to show popup")
        val fragmentActivity = context.currentActivity ?: throw NullPointerException("Not assigned current activity")
        val authCallback: BiometricPrompt.AuthenticationCallback = BiometricPromptAuthCallback(onSuccess, onFailure)
        val executor: Executor = Executors.newSingleThreadExecutor()

        val promptInfo = createPromptInfo()
        val biometricPrompt = BiometricPrompt(fragmentActivity as FragmentActivity, executor, authCallback)

        Log.e(logTag, "showing prompt")
        biometricPrompt.authenticate(promptInfo, cryptoObject)

      } catch (e: Exception) {
        Log.e(logTag, "exception in creating auth prompt: ", e)
        onFailure(ErrorCode.INTERNAL_ERROR, "Failed to display auth prompt")
      }
    }
  }

  private fun createPromptInfo(): PromptInfo {
    return PromptInfo.Builder()
      .setTitle("Unlock App")
      .setDescription("Enter phone screen lock pattern, PIN, password or fingerprint")
      .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
      .build()
  }
}
