package com.example.secure_keystore_example_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.secure_keystore_example_app.ui.theme.SecurekeystoreexampleappTheme
import com.reactnativesecurekeystore.SecureKeystoreImpl
import com.reactnativesecurekeystore.biometrics.Biometrics
import com.facebook.react.bridge.ReactApplicationContext
import com.reactnativesecurekeystore.CipherBoxImpl
import com.reactnativesecurekeystore.KeyGeneratorImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var keyGen = KeyGeneratorImpl()
        var cipBox = CipherBoxImpl()
        var biometrics = Biometrics(ReactApplicationContext(this))
        var secureKeystore = SecureKeystoreImpl(keyGen, cipBox, biometrics)
        var alias = "1234ab"
        var publicKey = keyGen.generateKeyPair(alias, false, 30)
        System.out.println("Our secure keystore has the alias - "+secureKeystore.hasAlias(alias))
        setContent {
            SecurekeystoreexampleappTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SecurekeystoreexampleappTheme {
        Greeting("Android")
    }
}