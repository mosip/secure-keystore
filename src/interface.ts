export interface SecureKeystoreModule {
  deviceSupportsHardware: () => boolean;
  generateKey: (alias: String) => void;
  generateKeyPair: (alias: String) => string;
  encryptData: (alias: String, data: String) => String;
  decryptData: (alias: String, encryptedText: String) => String;
}
