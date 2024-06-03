# react-native-secure-keystore
A React Native module to create and store keys in android hardware keystore and helps to do encryption, decryption, and hmac calculation.

`note: This library only supported for android.`

## Installation

```sh
npm install @mosip/secure-keystore
```

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

`generateKey(alias: string) => string`

generates a asymmetric RSA key Pair for signing

### encryptData

`encryptData(alias: string, data: string) => string`

Encrypts the given data(encoded in base64) using the key that is assigned to the alias. Returns back encrypted data as a string

### decryptData

`decryptData(alias: string, encryptionText: string) => string`

Decrypts the given encryptionText using the key that is assigned to the alias. Returns back the data as a string

### sign

`sign(alias: string, data: string) => string`

Create a signature for the given data using the key that is assigned to the alias. Returns back the signature as a string

### hasAlias

`hasAlias(alias: string) => boolean`

Check if the given alias is present in the key store


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MPL-2.0

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
