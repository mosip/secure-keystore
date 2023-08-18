import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.CryptoObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.acos

class BiometricPromptAuthCallback(
  private val continuation: Continuation<Unit>,
  private val action: (CryptoObject?) -> Unit,
) : BiometricPrompt.AuthenticationCallback() {
  override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
    super.onAuthenticationError(errorCode, errString)

    continuation.resumeWithException(RuntimeException("User has cancelled biometric auth code: $errorCode, message: $errString"))
  }

  override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
    super.onAuthenticationSucceeded(result)
    val cryptoObject = result.cryptoObject

    action(cryptoObject)
    continuation.resume(Unit)
  }
}
