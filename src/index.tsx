import { NativeModules, Platform } from 'react-native';
import type { SecureKeystoreModule } from './interface';

const LINKING_ERROR =
  `The package 'react-native-secure-keystore' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const SecureKeystore: SecureKeystoreModule = NativeModules.SecureKeystore
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