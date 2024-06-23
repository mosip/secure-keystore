export interface SecureKeystoreModule {
    deviceSupportsHardware: () => boolean;
    generateKey: (alias: string, isAuthRequired: boolean, authTimeout: number) => void;
    generateKeyPair: (alias: string, isAuthRequired: boolean, authTimeout: number) => string;
    generateHmacshaKey: (alias: string) => void;
    hasAlias: (alias: string) => boolean;
    updatePopup: (title: string, description: String) => void;
    encryptData: (alias: string, data: string) => Promise<string>;
    decryptData: (alias: string, encryptedText: string) => Promise<string>;
    generateHmacSha: (alias: string, data: string) => Promise<string>;
    sign: (alias: string, data: string) => Promise<string>;
    clearKeys: () => void;
    hasBiometricsEnabled: () => boolean;
}
//# sourceMappingURL=interface.d.ts.map