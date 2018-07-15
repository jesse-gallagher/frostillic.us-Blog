package bean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import util.MD5Util;

@ApplicationScoped
@Named("userInfo")
public class UserInfoBean {
	public String getGravatarUrl(String emailAddress, int size) {
		return "https://secure.gravatar.com/avatar/" + MD5Util.md5Hex(emailAddress) + "?s=" + size;
	}
}
