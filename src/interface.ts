export interface SecureKeystoreModule {
  deviceSupportsHardware: () => boolean;
  generateKey: (alias: string, isAuthRequired: boolean, authTimeout?: number) => void;
  generateKeyPair: (alias: string, isAuthRequired: boolean, authTimeout?: number) => string;
  hasAlias: (alias: string) => boolean;
  encryptData: (alias: string, data: string) => Promise<string>;
  decryptData: (alias: string, encryptedText: string) => Promise<string>;
  generateHmacSha: (alias: string, data: string) => Promise<string>;
  sign: (alias: string, data: string) => Promise<string>;
  generateHmacshaKey: (alias: string) => void;
  clearKeys: () => void;
  hasBiometricsEnabled: () => boolean;
}
