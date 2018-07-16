package bean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.darwino.platform.DarwinoContext;

import util.MD5Util;

@RequestScoped
@Named("userInfo")
public class UserInfoBean {
	@Inject @Named("darwinoContext")
	DarwinoContext context;
	
	public String getGravatarUrl(String emailAddress, int size) {
		return "https://secure.gravatar.com/avatar/" + MD5Util.md5Hex(emailAddress) + "?s=" + size;
	}
	
	public boolean isAdmin() {
		return context.getUser().hasRole("admin");
	}
}
