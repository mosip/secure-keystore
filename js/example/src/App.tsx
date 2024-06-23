import { useEffect, useState } from 'react';
import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import SecureStore from 'react-native-secure-keystore';

export default function App() {
  const [supportsHardware, setSupportsHardware] = useState<
    boolean | undefined
  >();

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    const supHardware = SecureStore.deviceSupportsHardware();
    setSupportsHardware(supHardware);
    console.log('is hardware supported: ' + supHardware);
    SecureStore.encryptData('key-alias', 'Hi I am a Developer').then(
      (encryptData) => {
        console.log('encryptedText: ' + encryptData);
        console.log(
          'decryptedText: ' + SecureStore.decryptData('key-alias', encryptData)
        );
      }
    );
  });

  return (
    <View style={styles.container}>
      <Text>Has HardWare Storage: {supportsHardware}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
