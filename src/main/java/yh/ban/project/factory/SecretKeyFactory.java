package yh.ban.project.factory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecretKeyFactory {
	private static String SECRET;

	private static SecretKey secretKey;

	private SecretKeyFactory() {
	}

	@Value("${secret}")
	public void setSecret(String secret) {
		SECRET = secret;
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

//	private static SecretKey generateSecretKey() {
//		try {
//			KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
//
//			keyGenerator.init(256);
//
//			return keyGenerator.generateKey();
//		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {
//			return null;
//		}
//	}

	private static SecretKey generateSecretKey() {
		return new SecretKeySpec(SECRET.getBytes(), "HmacSHA256");
	}
}
