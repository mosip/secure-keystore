import { NativeModules, Platform } from 'react-native';
import type { RNSecureKeystoreModule } from './interface';

const LINKING_ERROR =
  `The package 'react-native-secure-keystore' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const RNSecureKeystore: RNSecureKeystoreModule = NativeModules.RNSecureKeystoreModule
  ? NativeModules.SecureKeystore
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export default SecureKeystore;
