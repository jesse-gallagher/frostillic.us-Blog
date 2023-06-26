/*
 * Copyright (c) 2012-2023 Jesse Gallagher
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
package darwino;

import java.util.List;

import com.darwino.commons.platform.beans.ManagedBeansExtension;
import com.darwino.commons.platform.properties.PropertiesExtension;
import com.darwino.commons.security.acl.UserProvider;
import com.darwino.j2ee.platform.DefaultWebBeanExtension;
import com.darwino.j2ee.platform.DefaultWebPropertiesExtension;
import com.darwino.social.gravatar.GravatarUserProvider;


/**
 * J2EE Plugin for registering the services.
 */
public class AppPlugin extends AppBasePlugin {

	public AppPlugin() {
		super("frostillic.us Jakarta EE Application"); //$NON-NLS-1$
	}

	@Override
	public void findExtensions(final Class<?> serviceClass, final List<Object> extensions) {
		if(serviceClass==ManagedBeansExtension.class) {
			extensions.add(new DefaultWebBeanExtension());
		} else if(serviceClass==PropertiesExtension.class) {
			extensions.add(new DefaultWebPropertiesExtension());
		} else if(serviceClass==UserProvider.class) {
			var g = new GravatarUserProvider();
			g.setImageSize(256);
			extensions.add(g);
		}
	}
}
