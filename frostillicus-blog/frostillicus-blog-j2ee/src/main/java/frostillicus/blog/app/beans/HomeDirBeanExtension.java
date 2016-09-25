package frostillicus.blog.app.beans;

import java.io.File;

import com.darwino.commons.Platform;
import com.darwino.commons.util.StringUtil;
import com.darwino.j2ee.platform.DefaultWebBeanExtension;

public class HomeDirBeanExtension extends DefaultWebBeanExtension {

	@Override
	protected void initUserDir() {
		super.initUserDir();
		
		// Current user dir directory
		try {
			String h = System.getProperty("user.dir") + File.separatorChar + ".darwinosecret" + File.separatorChar;
			if(StringUtil.isNotEmpty(h)) {
				File f = new File(h,"darwino-beans.xml");
				addFactoriesFromFile(f);
			}
		} catch(Exception ex) {
			Platform.log(ex);
		}
	}
}
