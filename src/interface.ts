export interface SecureKeystoreModule {
  deviceSupportsHardware: () => boolean;
  generateKey: (alias: string) => void;
  generateKeyPair: (alias: string) => string;
  hasAlias: (alias: string) => boolean;
  encryptData: (alias: string, data: string) => string;
  decryptData: (alias: string, encryptedText: string) => string;
  sign: (alias: string, data: string) => string;
}
