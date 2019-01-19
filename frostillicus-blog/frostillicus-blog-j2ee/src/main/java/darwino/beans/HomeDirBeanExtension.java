/**
 * Copyright © 2016-2019 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package darwino.beans;

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
			String h = System.getProperty("user.dir") + File.separatorChar + ".darwinosecret" + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$
			if(StringUtil.isNotEmpty(h)) {
				File f = new File(h,"darwino-beans.xml"); //$NON-NLS-1$
				addFactoriesFromFile(f);
			}
		} catch(Exception ex) {
			Platform.log(ex);
		}
	}
}
