package util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
	public static String hex(final byte[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i]
			                                     & 0xFF) | 0x100).substring(1,3));
		}
		return sb.toString();
	}
	public static String md5Hex (final String message) {
		try {
			MessageDigest md =
				MessageDigest.getInstance("MD5"); //$NON-NLS-1$
			return hex (md.digest(message.getBytes("CP1252"))); //$NON-NLS-1$
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
}