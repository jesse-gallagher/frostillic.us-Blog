package bean;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import util.MD5Util;

@ApplicationScoped
@Named("userInfo")
public class UserInfoBean {
	public String getGravatarUrl(String emailAddress, int size) throws NoSuchAlgorithmException {
		return "https://secure.gravatar.com/avatar/" + MD5Util.md5Hex(emailAddress) + "?s=" + size;
	}
}
