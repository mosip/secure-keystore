# secure-keystore - React native module library
A React Native module to create and store keys in android [hardware keystore](https://source.android.com/docs/security/features/keystore) and helps to do encryption, decryption, hmac calculation.

This library contains a react-native wrapper to store the secure data in android keystore and helps to do encryption, decryption, hmac calculation for android devices which supports hardware backed keystore.

This library allows user to store the Verifiable Credential (VC) securely using hardware backed keystore. As the library allows HMAC calculation we can use the same library to verify that if the stored data has been tampered.

It also helps to sign with aliases, created as part of key pair generation.

This contains the source code for the android modules as well as a sample app under example/ folder. The sample app can be used for testing the modules being worked on in case it is needed.

`note: This library only supported for android.`

## Installing this library as a dependency

```sh
npm install @mosip/secure-keystore

# or

# Install specific version
npm install @mosip/secure-keystore@v0.1.6
```

## API documentation

### deviceSupportsHardware
To check if device supports hardware keystore

`deviceSupportsHardware() => boolean`

### generateKey
It generates a symmetric key for encryption and decryption

`generateKey(alias: string) => void`

### generateKeyPair
It generates an asymmetric RSA key Pair for signing

`generateKey(alias: string) => string`

### encryptData
It encrypts the given data using the key that is assigned to the alias. Returns back encrypted data as a string

`encryptData(alias: string, data: string) => string`

### decryptData
It decrypts the given encryptionText using the key that is assigned to the alias. Returns back the data as a string

`decryptData(alias: string, encryptionText: string) => string`

### sign
It creates a signature for the given data using the key that is assigned to the alias. Returns back the signature as a string

`sign(alias: string, data: string) => string`

### hasAlias
It checks if the given alias is present in the key store

`hasAlias(alias: string) => boolean`

## Usage

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
const data = "any data";

await SecureKeyStore.generateKey(alias);

const encryptedData = await SecureKeyStore.encryptData(alias, data)
const decryptedData = await SecureKeyStore.decryptData(alias, encryptedData)

```


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MPL2.0

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)

