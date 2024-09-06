package com.reactnativesecurekeystore.common

import android.security.keystore.KeyInfo
import com.reactnativesecurekeystore.KEYSTORE_TYPE
import com.reactnativesecurekeystore.PUBLIC_KEY_STORING_ID
import com.reactnativesecurekeystore.PRIVATE_KEY_STORING_ID
import java.security.Key
import java.security.KeyFactory
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

class Util {
    companion object {
        fun getKeyInfo(key: Key): KeyInfo {
            if (key is SecretKey) {
                val keyFactory = SecretKeyFactory.getInstance(key.algorithm, KEYSTORE_TYPE)
                return keyFactory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
            }

            val keyFactory = KeyFactory.getInstance(key.algorithm, KEYSTORE_TYPE)
            return keyFactory.getKeySpec(key, KeyInfo::class.java)
        }

        fun getPublicKeyId(account:String): String{
            return account+PUBLIC_KEY_STORING_ID;
        }

        fun getPrivateKeyId(account:String): String{
            return account+PRIVATE_KEY_STORING_ID;
        }

        fun getLogTag(moduleName: String): String {
            return moduleName
        }
    }
}
