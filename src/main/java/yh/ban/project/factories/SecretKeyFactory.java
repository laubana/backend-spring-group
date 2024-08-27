package yh.ban.project.factories;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class SecretKeyFactory {
	private static SecretKey secretKey;

	private SecretKeyFactory() {
	}

	public static SecretKey getSecretKey() {
		if (secretKey == null) {
			synchronized (SecretKeyFactory.class) {
				if (secretKey == null) {
					secretKey = generateSecretKey();
				}
			}
		}
		return secretKey;
	}

	private static SecretKey generateSecretKey() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");

			keyGenerator.init(256);

			return keyGenerator.generateKey();
		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
			return null;
		}
	}
}
