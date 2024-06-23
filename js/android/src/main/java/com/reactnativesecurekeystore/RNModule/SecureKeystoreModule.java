package com.reactnativesecurekeystore;

import com.reactnativesecurekeystore.SecureKeystoreImpl;
import com.reactnativesecurekeystore.KeyGeneratorImpl;
import com.reactnativesecurekeystore.CipherBoxImpl;
import android.app.Activity;
import com.reactnativesecurekeystore.DeviceCapability;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.reactnativesecurekeystore.biometrics.Biometrics;
import com.reactnativesecurekeystore.common.Util;
import kotlin.Unit;
import android.content.Context;
import org.jetbrains.annotations.Nullable;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import androidx.fragment.app.FragmentActivity;
import com.facebook.react.bridge.ActivityEventListener;

public class SecureKeystoreModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private final KeyGeneratorImpl keyGenerator = new KeyGeneratorImpl();
    private final CipherBoxImpl cipherBox = new CipherBoxImpl();
    private final Biometrics biometrics;
    private final SecureKeystoreImpl keystore;
    private final DeviceCapability deviceCapability;
    private final String logTag;

    private ReactApplicationContext context;

    public SecureKeystoreModule(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
        Context context = getReactApplicationContext();
        this.biometrics = new Biometrics();
        this.keystore = new SecureKeystoreImpl(keyGenerator, cipherBox, biometrics);
        this.deviceCapability = new DeviceCapability(keystore, keyGenerator, biometrics);
        this.logTag = Util.Companion.getLogTag(getClass().getSimpleName());
    }

    @Override
    public String getName() {
        return "SecureKeystoreModule";
    }

    @Override
    public void onNewIntent(Intent intent) {
        // Handle new intent here if needed
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data) {
        // Handle activity result here if needed
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public boolean deviceSupportsHardware() {
        return deviceCapability.supportsHardwareKeyStore();
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public boolean hasAlias(String alias) {
        return keystore.hasAlias(alias);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public void updatePopup(String title, String description) {
        Biometrics.Companion.updatePopupDetails(title, description);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public void generateKey(String alias, boolean isAuthRequired, Integer authTimeout) {
        keystore.generateKey(alias, isAuthRequired, authTimeout);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String generateKeyPair(String alias, boolean isAuthRequired, Integer authTimeout) {
        return keystore.generateKeyPair(alias, isAuthRequired, authTimeout);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public String generateKeyPairEC(String alias, boolean isAuthRequired, Integer authTimeout) {
        return keystore.generateKeyPairEC(alias, isAuthRequired, authTimeout);
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public void generateHmacshaKey(String alias) {
        keystore.generateHmacSha256Key(alias);
    }

    @ReactMethod
    public void encryptData(String alias, String data, Promise promise) {
        Function1<String, Unit> successLambda = new Function1<String, Unit>() {
            @Override
            public Unit invoke(String encryptedText) {
                promise.resolve(encryptedText);
                return Unit.INSTANCE;
            }
        };

        Function2<Integer, String, Unit> failureLambda = new Function2<Integer, String, Unit>() {
            @Override
            public Unit invoke(Integer code, String message) {
                promise.reject(code.toString(), message);
                return Unit.INSTANCE;
            }
        };
        keystore.encryptData(
                alias,
                data,
                successLambda,
                failureLambda,
                context.getCurrentActivity()
        );
    }

    @ReactMethod
    public void decryptData(String alias, String encryptedText, Promise promise) {
        Function1<String, Unit> successLambda = new Function1<String, Unit>() {
            @Override
            public Unit invoke(String data) {
                promise.resolve(data);
                return Unit.INSTANCE;
            }
        };

        Function2<Integer, String, Unit> failureLambda = new Function2<Integer, String, Unit>() {
            @Override
            public Unit invoke(Integer code, String message) {
                promise.reject(code.toString(), message);
                return Unit.INSTANCE;
            }
        };

        keystore.decryptData(alias, encryptedText, successLambda, failureLambda, context.getCurrentActivity());
    }

    @ReactMethod
    public void generateHmacSha(String alias, String data, Promise promise) {
        Function1<String, Unit> successLambda = new Function1<String, Unit>() {
            @Override
            public Unit invoke(String sha) {
                promise.resolve(sha);
                return Unit.INSTANCE;
            }
        };

        Function2<Integer, String, Unit> failureLambda = new Function2<Integer, String, Unit>() {
            @Override
            public Unit invoke(Integer code, String message) {
                promise.reject(code.toString(), message);
                return Unit.INSTANCE;
            }
        };

        keystore.generateHmacSha(
                alias,
                data,
                successLambda,
                failureLambda
        );
    }

    @ReactMethod
    public void sign(String signAlgorithm, String alias, String data, Promise promise) {
        Function1<String, Unit> successLambda = new Function1<String, Unit>() {
            @Override
            public Unit invoke(String signature) {
                promise.resolve(signature);
                return Unit.INSTANCE;
            }
        };

        Function2<Integer, String, Unit> failureLambda = new Function2<Integer, String, Unit>() {
            @Override
            public Unit invoke(Integer code, String message) {
                promise.reject(code.toString(), message);
                return Unit.INSTANCE;
            }
        };

        keystore.sign(
                signAlgorithm,
                alias,
                data,
                successLambda,
                failureLambda,
                context.getCurrentActivity()
        );
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public void clearKeys() {
        keystore.removeAllKeys();
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    public boolean hasBiometricsEnabled() {
        return deviceCapability.hasBiometricsEnabled(context.getCurrentActivity());
    }
}
