package com.example.securekeystoreapp

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.reactnativesecurekeystore.CipherBoxImpl
import com.reactnativesecurekeystore.KeyGeneratorImpl
import com.reactnativesecurekeystore.SecureKeystoreImpl
import com.reactnativesecurekeystore.biometrics.Biometrics
import kotlinx.coroutines.*
import android.content.Context

class MainActivity : AppCompatActivity() {

    private lateinit var alias: String
    private lateinit var signALgorithm: String
    private lateinit var secureKeystore: SecureKeystoreImpl
    private lateinit var biometrics: Biometrics
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the key generator, cipher box, and biometrics
        val keyGenerator = KeyGeneratorImpl()
        val cipherBox = CipherBoxImpl()
        biometrics = Biometrics()
        secureKeystore = SecureKeystoreImpl(keyGenerator, cipherBox, biometrics)

        // Initialize the ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCancelable(false)

        // Find UI components
        val generateKeyPairButton: Button = findViewById(R.id.generateKeyPairButton)
        val generateECKeyPairButton: Button = findViewById(R.id.generateECKeyPairButton)
        val dataToSignEditText: EditText = findViewById(R.id.dataToSignEditText)
        val signDataButton: Button = findViewById(R.id.signDataButton)
        val resultTextView: TextView = findViewById(R.id.resultTextView)

        // Set onClickListeners for buttons
        generateKeyPairButton.setOnClickListener {
             alias = "myKeyAlias"
            secureKeystore.generateKeyPair(alias, false, null)
            signALgorithm = "SHA256withRSA"
            resultTextView.text = "Generated Key Pair for alias: $alias"
        }

        generateECKeyPairButton.setOnClickListener {
             alias = "myECKeyAlias"
            secureKeystore.generateKeyPairEC(alias, true, 10)
            resultTextView.text = "Generated EC Key Pair for alias: $alias"
            signALgorithm = "SHA256withECDSA"
        }

        signDataButton.setOnClickListener {
            val data = dataToSignEditText.text.toString()
            authenticateAndSignData(alias, data, resultTextView,this)
        }
    }

    private fun authenticateAndSignData(alias: String, data: String, resultTextView: TextView,context:Context) {
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                secureKeystore.sign(
                    signALgorithm,
                    alias,
                    data,
                    onSuccess = { signature ->
                        runOnUiThread {
                            handleSuccess(signature)
                        }
                    },
                    onFailure = { code, message ->
                        runOnUiThread {
                            handleFailure(code, message)
                        }
                    },
                    context
                )
            } catch (e: Exception) {
                Log.d("error", "error")
                runOnUiThread {
                    handleFailure(-1, "An error occurred")
                }
            } finally {
                runOnUiThread {
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun handleSuccess(signature: String) {
        findViewById<TextView>(R.id.resultTextView).text = "Signature: $signature"
    }

    private fun handleFailure(code: Int, message: String) {
        findViewById<TextView>(R.id.resultTextView).text = "Error: $message"
    }
}
