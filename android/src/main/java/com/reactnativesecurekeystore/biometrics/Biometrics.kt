 package com.reactnativesecurekeystore.biometrics

  import android.content.Context
  import android.os.Build
  import android.security.keystore.KeyPermanentlyInvalidatedException
  import android.security.keystore.UserNotAuthenticatedException
  import android.util.Log
  import androidx.biometric.BiometricManager
  import androidx.biometric.BiometricPrompt
  import androidx.fragment.app.FragmentActivity
  import com.reactnativesecurekeystore.exception.ErrorCode
  import com.reactnativesecurekeystore.exception.KeyInvalidatedException
  import java.security.SignatureException
  import java.util.concurrent.Executor
  import java.util.concurrent.Executors
  import javax.crypto.IllegalBlockSizeException
  import kotlin.coroutines.resumeWithException
  import kotlin.coroutines.suspendCoroutine

  class Biometrics(
    private val context: Context
  ) {
    private val logTag = "sec"

    companion object {
      var POPUP_TITLE = "Unlock App"
      var POPUP_DESCRIPTION = "Please use fingerprint to unlock the app"

      fun updatePopupDetails(title: String, description: String) {
        POPUP_TITLE = title
        POPUP_DESCRIPTION = description
      }
    }

    suspend fun authenticateAndPerform(
      createCryptoObject: () -> BiometricPrompt.CryptoObject,
      action: (BiometricPrompt.CryptoObject) -> Unit,
      onFailure: (code: Int, message: String) -> Unit
    ) {
      try {
        Log.d(logTag, "Creating CryptoObject")
        val preAuthCryptoObject = createCryptoObject()
        Log.d(logTag, "CryptoObject created, calling action")
        action(preAuthCryptoObject)
      } catch (e: UserNotAuthenticatedException) {
        Log.e(logTag, "User not authenticated exception", e)
        authenticate(createCryptoObject, action, false)
      } catch (e: IllegalBlockSizeException) {
        Log.e(logTag, "Illegal block size exception", e)
        authenticate(createCryptoObject, action, true)
      } catch (e: SignatureException) {
        Log.e(logTag, "Signature exception", e)
        authenticate(createCryptoObject, action, true)
      } catch (e: KeyPermanentlyInvalidatedException) {
        Log.e(logTag, "Key permanently invalidated exception", e)
        throw KeyInvalidatedException()
      } catch (e: Exception) {
        Log.e(logTag, "Other exception", e)
        onFailure(ErrorCode.INTERNAL_ERROR.ordinal, e.message.toString())
      }
    }

    fun isBiometricEnabled(): Boolean {
      val biometricManager = BiometricManager.from(context)
      return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private suspend fun authenticate(
      createCryptoObject: () -> BiometricPrompt.CryptoObject,
      action: (BiometricPrompt.CryptoObject) -> Unit,
      createCryptoObjectSuccess: Boolean
    ) = suspendCoroutine<Unit> { continuation ->
      try {
        val fragmentActivity = context as? FragmentActivity
          ?: throw NullPointerException("Context is not a FragmentActivity")

        val onAuthSuccess: (BiometricPrompt.CryptoObject?) -> Unit =
          { cryptoObject -> action(cryptoObject ?: createCryptoObject()) }

        val authCallback = BiometricPromptAuthCallback(continuation, onAuthSuccess)
        val executor: Executor = Executors.newSingleThreadExecutor()

        val promptInfo = createPromptInfo()
        val biometricPrompt = BiometricPrompt(fragmentActivity, executor, authCallback)

        if (createCryptoObjectSuccess) {
          biometricPrompt.authenticate(promptInfo, createCryptoObject())
        } else {
          biometricPrompt.authenticate(promptInfo)
        }
      } catch (e: Exception) {
        Log.e(logTag, "Exception in creating auth prompt", e)
        continuation.resumeWithException(RuntimeException("Exception in creating auth prompt"))
      }
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
      val builder = BiometricPrompt.PromptInfo.Builder()
        .setTitle(POPUP_TITLE)
        .setDescription(POPUP_DESCRIPTION)
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        builder.setNegativeButtonText("Cancel")
      }

      return builder.build()
    }
  }
