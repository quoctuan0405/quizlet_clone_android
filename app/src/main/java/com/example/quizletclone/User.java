package com.example.quizletclone;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;

public class User {
    static final private String KEY_TOKEN = "TOKEN";
    static final private String KEY_USER_ID = "KEY_USER_ID";

    // https://devmainapps.blogspot.com/2020/06/android-masterkeys-deprecated-how-to.html
    // https://medium.com/@ali.muzaffar/securing-sharedpreferences-in-android-a21883a9cbf8
    static private SharedPreferences preferences(Context context) {
        SharedPreferences sharedPreferences;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build();

                MasterKey masterKey = new MasterKey.Builder(context.getApplicationContext())
                        .setKeyGenParameterSpec(spec)
                        .build();

                sharedPreferences = EncryptedSharedPreferences.create(
                        context.getApplicationContext(),
                        "secret_shared_prefs",
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                return sharedPreferences;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                Calendar start = new GregorianCalendar();
                Calendar end = new GregorianCalendar();
                end.add(Calendar.YEAR, 30);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        // You'll use the alias later to retrieve the key. It's a key
                        // for the key!
                        .setAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                        .setSubject(new X500Principal("CN=" + MasterKey.DEFAULT_MASTER_KEY_ALIAS))
                        .setSerialNumber(BigInteger.valueOf(Math.abs(MasterKey.DEFAULT_MASTER_KEY_ALIAS.hashCode())))
                        // Date range of validity for the generated pair.
                        .setStartDate(start.getTime()).setEndDate(end.getTime())
                        .build();

                KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(
                        "RSA",
                        "AndroidKeyStore");
                kpGenerator.initialize(spec);
                KeyPair kp = kpGenerator.generateKeyPair();

                sharedPreferences = EncryptedSharedPreferences.create(
                        "secret_shared_prefs",
                        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                        context,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );

                return sharedPreferences;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    static public String getToken(Context context) {
        SharedPreferences sharedPreferences = preferences(context);

        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_TOKEN, null);
        } else {
            return "";
        }
    }

    static public void setToken(Context context, String token) {
        SharedPreferences sharedPreferences = preferences(context);

        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_TOKEN, token).apply();
        }
    }

    static public void removeToken(Context context) {
        SharedPreferences sharedPreferences = preferences(context);

        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(KEY_TOKEN).apply();
        }
    }

    static public String getUserId(Context context) {
        SharedPreferences sharedPreferences = preferences(context);

        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_USER_ID, null);
        } else {
            return "";
        }
    }

    static public void setUserId(Context context, String token) {
        SharedPreferences sharedPreferences = preferences(context);

        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_USER_ID, token).apply();
        }
    }

    static public void removeUserId(Context context) {
        SharedPreferences sharedPreferences = preferences(context);

        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(KEY_USER_ID).apply();
        }
    }
}
