/**
 * Copyright © 2016-2018 Jesse Gallagher
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
package frostillicus.blog.app;

import java.util.List;

import com.darwino.commons.platform.beans.ManagedBeansExtension;
import com.darwino.commons.platform.properties.PropertiesExtension;
import com.darwino.commons.security.acl.UserService;
import com.darwino.j2ee.platform.DefaultWebPropertiesExtension;
import com.darwino.j2ee.servlet.authentication.AuthenticationService;

import frostillicus.blog.app.beans.HomeDirBeanExtension;



/**
 * J2EE Plugin for registering the services.
 */
public class AppPlugin extends AppBasePlugin {
	
	public AppPlugin() {
		super("J2EE Application"); //$NON-NLS-1$
	}

	@Override
	public void findExtensions(Class<?> serviceClass, List<Object> extensions) {
		if(serviceClass==AuthenticationService.class) {
			// User authentication service
			// This service grabs the currently authenticated user from the server context
			// By default it uses the J2EE Principal as provided by the application server, but
			// this can be changed to use alternate authentication methods
			//extensions.add(new AuthenticationService() {
			//	@Override
			//	protected boolean forceHttpSession() {
			//		// We have to set this property to ensure that the basic authentication is kept even
			//		// when the client hits a non protected resource.
			//		return true;
			//	}
			//});
		} else if(serviceClass==UserService.class) {
			// User service
			// This service can point to an LDAP server or a static directory
			// The default uses a bean like bellow
			//extensions.add(new UserServiceBeanDelegate());
		} else if(serviceClass==ManagedBeansExtension.class) {
			// Ok, default web entries - can be replaced by something more accurate
			extensions.add(new HomeDirBeanExtension());
			
		} else if(serviceClass==PropertiesExtension.class) {
			// Ok, default web entries - can be replaced by something more accurate
			extensions.add(new DefaultWebPropertiesExtension());
		}
	}
}
