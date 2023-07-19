export interface SecureKeystoreModule {
  deviceSupportsHardware: () => boolean;
  encryptData: (alias: String, data: String) => String;
  decryptData: (alias: String, encryptedText: String) => String;
}
