import React, { useState } from 'react';
import { StyleSheet, View, TextInput, Button, TouchableOpacity, Text } from 'react-native';
import SecureKeystore from '@mosip/secure-keystore';

export default function App() {
  const [inputMessage, setInputMessage] = useState('');
  const [keyType, setKeyType] = useState('RSA'); // Default to RSA
  const [signatureResult, setSignatureResult] = useState('');

  const generateAndSignKeyPair = async () => {
    try {
      // Generate the key pair based on the selected key type
      if(keyType === 'RSA') {
        await SecureKeystore.generateKeyPair(keyType, true, 0);
      } else {
        await SecureKeystore.generateKeyPairEC("ECDSA", true, 0);
      }
      
      // Perform the signing operation
      const signature = await SecureKeystore.sign('SHA256with' + keyType, keyType , inputMessage);
      setSignatureResult(`Signature using ${keyType}: ${signature}`);
    } catch (error) {
      console.error('Error during key generation or signing:', error);
      setSignatureResult(`Failed to generate or sign using ${keyType}`);
    }
  };

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.input}
        onChangeText={setInputMessage}
        value={inputMessage}
        placeholder="Enter your message here..."
        placeholderTextColor="#888"
      />
      <View style={styles.buttonContainer}>
        <TouchableOpacity style={styles.button} onPress={() => setKeyType('RSA')}>
          <Text style={styles.buttonText}>Use RSA</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.button} onPress={() => setKeyType('ECDSA')}>
          <Text style={styles.buttonText}>Use EC</Text>
        </TouchableOpacity>
      </View>
      <TouchableOpacity style={styles.buttonLarge} onPress={generateAndSignKeyPair}>
        <Text style={styles.buttonText}>Generate Key Pair and Sign</Text>
      </TouchableOpacity>
      <Text style={styles.resultText}>Result: {signatureResult}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
    backgroundColor: '#f9f9f9', // light grey background
  },
  input: {
    height: 50,
    marginVertical: 12,
    borderWidth: 1,
    borderColor: '#ccc',
    padding: 10,
    width: '100%',
    borderRadius: 5,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    width: '100%',
    marginTop: 20,
  },
  button: {
    backgroundColor: '#007AFF', // iOS blue
    paddingVertical: 12,
    paddingHorizontal: 20,
    borderRadius: 5,
  },
  buttonText: {
    color: 'white',
    textAlign: 'center',
    fontWeight: '600',
  },
  buttonLarge: {
    backgroundColor: '#28a745', // Bootstrap green
    paddingVertical: 12,
    paddingHorizontal: 20,
    borderRadius: 5,
    marginTop: 20,
    width: '100%',
  },
  resultText: {
    padding: 20,
    color: '#333',
    fontSize: 16,
    textAlign: 'center',
  },
});
