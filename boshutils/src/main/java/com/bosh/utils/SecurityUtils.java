package com.bosh.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import java.io.File;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * *Work in Progress*
 *
 * Manages encrypting and decrypting data in the form of {@link String} or {@link File} objects using
 * AES 256 encryption algorithm. This class must be initialised with a password and a salt before
 * being able to encrypt and decrypt the data.
 *
 * @author David Jones
 * @version 1.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class SecurityUtils {

	private static final int mIterationCount = 1000;

	private static final String CHARSET = "UTF-8";
	private static final int mKeyLength = 384;
	private static final int mKeyByteLength = 256 / 8;

	private static final int mIVByteLength = 128 / 8;
	private static final String KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final String SECRET_KEY_ALGORITHM = "AES";
	private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

	private final char[] mKeyCharArray;
	private final byte[] mSalt;

	public SecurityUtils(@NonNull String password, @NonNull byte[] salt) {
		mKeyCharArray = password.toCharArray();
		mSalt = salt;
	}

	public String encryptEncodeString(String string) {
		return Base64.encodeToString(encryptString(string), Base64.NO_WRAP);
	}

	public byte[] encryptString(String string) {
		try {
			return encrypt(string.getBytes(CHARSET));
		} catch (Exception e) {
			UtilLogger.e("SecurityUtils", "encryptString(" + string + ") cannot be encrypted: " + e.getLocalizedMessage());
		}
		return new byte[0];
	}

	@Nullable
	public String decryptString(byte[] encodedString) {
		try {
			byte[] plaintext = decrypt(encodedString);
			return new String(plaintext, CHARSET);
		} catch (Exception e) {
			UtilLogger.e("SecurityUtils", "decryptString() cannot be decrypted: " + e.getLocalizedMessage());
		}
		return null;
	}

	@Nullable
	public String encryptEncodeFile(@NonNull File file) {
		try {
			byte[] fileBytes = FileUtils.readFile(file);
			byte[] encryptedData = encrypt(fileBytes);
			return Base64.encodeToString(encryptedData, Base64.NO_WRAP);
		} catch (Exception e) {
			UtilLogger.e("SecurityUtils", "encryptFile(" + file.getAbsolutePath()
				+ ") cannot be encrypted: " + e.getLocalizedMessage());
		}
		return null;
	}

	@Nullable
	public File decodeDecryptFile(@NonNull String base64, @NonNull File file) {
		try {
			byte[] encryptedData = Base64.decode(base64, Base64.NO_WRAP);
			byte[] decryptedData = decrypt(encryptedData);
			return FileUtils.writeFile(decryptedData, file.getAbsolutePath());
		} catch (Exception e) {
			UtilLogger.e("SecurityUtils", "decryptFile() cannot decrypt file: " + e.getLocalizedMessage());
		}
		return null;
	}

	private Cipher getCipher(int optMode) throws Exception {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
		PBEKeySpec pbeKeySpec = new PBEKeySpec(mKeyCharArray, mSalt, mIterationCount, mKeyLength);

		Key secretKey = factory.generateSecret(pbeKeySpec);
		byte[] key = new byte[mKeyByteLength];
		byte[] iv = new byte[mIVByteLength];
		System.arraycopy(secretKey.getEncoded(), 0, key, 0, mKeyByteLength);
		System.arraycopy(secretKey.getEncoded(), mKeyByteLength, iv, 0, mIVByteLength);

		SecretKeySpec secret = new SecretKeySpec(key, SECRET_KEY_ALGORITHM);
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
		Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
		cipher.init(optMode, secret, ivSpec);
		return cipher;
	}

	private byte[] encrypt(byte[] data) throws Exception {
		Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
		return cipher.doFinal(data);
	}

	private byte[] decrypt(byte[] encryptedData) throws Exception {
		Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
		return cipher.doFinal(encryptedData);
	}
}
