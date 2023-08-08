import androidx.biometric.BiometricPrompt
import com.reactnativesecurekeystore.biometrics.Biometrics

class BiometricPromptAuthCallback (
  val onSuccess: (cryptoObject: BiometricPrompt.CryptoObject?) -> Unit,
  val onFailure: (errorCode: Biometrics.ErrorCode, errString: String) -> Unit
) : BiometricPrompt.AuthenticationCallback() {
  override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
    super.onAuthenticationError(errorCode, errString)

    onFailure(Biometrics.ErrorCode.INTERNAL_ERROR, "$errString, Internal Error Code:  $errorCode")
  }

  override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
    super.onAuthenticationSucceeded(result)

    onSuccess(result.cryptoObject)
  }
}
