# secure-keystore
A module to create and store keys in android hardware keystore and helps to do encryption, decryption, and hmac calculation.

`note: This library only supported for android.`

## Usage as a Kotlin library (for native android)
The secure-keystore kotlin artifact (.aar) has been published to Maven.
### Adding as a Maven dependency.
- In settings.gradle.kts of your app modify the following:
  ```
       dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
         google()
         mavenCentral()
         maven("https://oss.sonatype.org/content/repositories/snapshots/")
       }
     }
  ```
- In your app's build.gradle.kts add the following:
  ```kotlin
     dependencies {
           implementation("io.mosip:secure-keystore:1.0-SNAPSHOT")
     }
  ```
 The kotlin library has been added to your project.

## Usage as a React-Native wrapper

### Installation

```sh
npm install @mosip/secure-keystore
```

## API Documentation

1. for RSA based Key Pair

```js
import SecureKeyStore  from "@mosip/secure-keystore";

// ...

if(!SecureKeyStore.deviceSupportsHardware) {
  return
}

const alias = "1234ab";
const data = "any data";

const publicKey = await SecureKeyStore.generateKeyPair(alias);

const signature = await SecureKeyStore.sign(alias, data)

```


2. for symmetric key

```js
import SecureKeyStore  from "@mosip/secure-keystore";

// ...

if(!SecureKeyStore.deviceSupportsHardware) {
  return
}

const alias = "1234ab";
const base64EncodedData = encodeToBase64("any data");

await SecureKeyStore.generateKey(alias);

const encryptedData = await SecureKeyStore.encryptData(alias, base64EncodedData)
const decryptedData = await SecureKeyStore.decryptData(alias, encryptedData)

```


## API documentation

### deviceSupportsHardware

`deviceSupportsHardware() => boolean`

Check if the device supports hardware key store


### generateKey

`generateKey(alias: string) => void`

generates a symmetric key for encryption and decryption

### generateKeyPair

`generateKeyPair(alias: String, isAuthRequired: Boolean, authTimeout: Int?): String`

generates a asymmetric RSA key Pair for signing

### generateKeyPairEC

`generateKeyPairEC(alias: String, isAuthRequired: Boolean, authTimeout: Int?): String`

generates a asymmetric EC(P-256) key Pair for signing.

### encryptData

`encryptData(alias: string, data: string) => string`

Encrypts the given data(encoded in base64) using the key that is assigned to the alias. Returns back encrypted data as a string

### decryptData

`decryptData(alias: string, encryptionText: string) => string`

Decrypts the given encryptionText using the key that is assigned to the alias. Returns back the data as a string

### sign

`sign(signature: Signature, data: String, signAlgorithm: String): String`

Create a signature for the given data, and signing algorithm using the key that is assigned to the alias. Returns back the signature as a string
For `SHA256withECDSA` as `signAlgorithm` the output is in standard ASN1 format.In case of certain verifiers like jwt.io conversion to RS format is necessary.

```kotlin
 private fun convertDerToRsFormat(derSignature: ByteArray): ByteArray {
     val asn1InputStream = ASN1InputStream(ByteArrayInputStream(derSignature))
     val seq = asn1InputStream.readObject() as ASN1Sequence
     val r = (seq.getObjectAt(0) as ASN1Integer).value
     val s = (seq.getObjectAt(1) as ASN1Integer).value

     val rBytes = r.toByteArray()
     val sBytes = s.toByteArray()

     val rPadded = ByteArray(32)
     val sPadded = ByteArray(32)

     val rTrimmed = if (rBytes.size > 32) rBytes.copyOfRange(rBytes.size - 32, rBytes.size) else rBytes
     val sTrimmed = if (sBytes.size > 32) sBytes.copyOfRange(sBytes.size - 32, sBytes.size) else sBytes

     System.arraycopy(rTrimmed, 0, rPadded, 32 - rTrimmed.size, rTrimmed.size)
     System.arraycopy(sTrimmed, 0, sPadded, 32 - sTrimmed.size, sTrimmed.size)

     return rPadded + sPadded
   }
```
### hasAlias

`hasAlias(alias: string) => boolean`

Check if the given alias is present in the key store


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MPL-2.0

---

