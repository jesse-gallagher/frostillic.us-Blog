/**
 * Copyright © 2012-2020 Jesse Gallagher
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
package jaxrs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import com.darwino.commons.util.StringUtil;
import com.darwino.commons.util.io.StreamUtil;

/**
 * This class hooks into incoming requests for a method-override form parameter.
 */
@Provider
@PreMatching
public class MethodOverrideFilter implements ContainerRequestFilter {
	@Context
	private Providers providers;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if(isReadable(requestContext)) {
			String overrideMethod = null;

			Form formData = getFormData(requestContext);
			if(formData != null) {
				List<String> formVal = formData.asMap().get("_httpmethod"); //$NON-NLS-1$
				if(formVal != null && !formVal.isEmpty()) {
					overrideMethod = formVal.get(0);
				}
			}

			if(StringUtil.isNotEmpty(overrideMethod)) {
				requestContext.setMethod(overrideMethod);
			}
		}
	}

	private boolean isReadable(ContainerRequestContext requestContext) {
		if(!"POST".equals(requestContext.getMethod())) { //$NON-NLS-1$
			return false;
		}

		// requestContext.hasEntity() is oddly unreliable - Liberty says false with a URL-encoded form post
		if(!(requestContext.hasEntity() || requestContext.getLength() > 0)) {
			return false;
		}

		MediaType mediaType = requestContext.getMediaType();
		return MediaType.APPLICATION_FORM_URLENCODED_TYPE.isCompatible(mediaType) || MediaType.MULTIPART_FORM_DATA_TYPE.isCompatible(mediaType);
	}

	private Form getFormData(ContainerRequestContext requestContext) throws IOException {
		InputStream is;
		MediaType mediaType = requestContext.getMediaType();
		if(MediaType.MULTIPART_FORM_DATA_TYPE.isCompatible(mediaType)) {
			is = requestContext.getEntityStream();
		} else {
			// handle this a bit differently to avoid downstream trouble with this type
			is = copy(requestContext.getEntityStream());
		}
		Form form = providers.getMessageBodyReader(Form.class, Form.class, new Annotation[0], mediaType)
				.readFrom(Form.class, Form.class, new Annotation[0], mediaType, null, is);
		return form;
	}

	private ByteArrayInputStream copy(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StreamUtil.copyStream(is, baos);
		byte[] data = baos.toByteArray();
		return new ByteArrayInputStream(data);
	}
}
