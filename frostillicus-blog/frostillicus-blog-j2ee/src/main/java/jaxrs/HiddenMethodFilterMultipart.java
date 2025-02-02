/*
 * Copyright (c) 2012-2025 Jesse Gallagher
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
import java.io.IOException;
import java.util.Arrays;

import com.darwino.commons.util.StringUtil;
import com.darwino.commons.util.io.StreamUtil;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;

/**
 * This class hooks into incoming requests for a method-override form parameter
 * when the incoming type is {@value MediaType#MULTIPART_FORM_DATA}.
 */
@Provider
@PreMatching
public class HiddenMethodFilterMultipart implements ContainerRequestFilter {
	@Context
	private Providers providers;

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {
		if(isReadable(requestContext)) {
			String overrideMethod = getHttpMethod(requestContext);

			if(StringUtil.isNotEmpty(overrideMethod)) {
				requestContext.setMethod(overrideMethod);
			}
		}
	}

	private boolean isReadable(final ContainerRequestContext requestContext) {
		if(!"POST".equals(requestContext.getMethod())) { //$NON-NLS-1$
			return false;
		}

		// requestContext.hasEntity() is oddly unreliable - Liberty says false with a URL-encoded form post
		if(!(requestContext.hasEntity() || requestContext.getLength() > 0)) {
			return false;
		}

		return MediaType.MULTIPART_FORM_DATA_TYPE.isCompatible(requestContext.getMediaType());
	}

	private String getHttpMethod(final ContainerRequestContext requestContext) throws IOException {
		// This copies the entire body ahead of time to deal with how various JAX-RS implementations
		//   may or may not reset the stream
		byte[] entity = requestContext.getEntityStream().readAllBytes();
		requestContext.setEntityStream(new ByteArrayInputStream(entity));

		// Read this here as it seems RestEasy doesn't provide a default reader
		try {
			MimeMultipart body = new MimeMultipart(new ByteArrayDataSource(entity, MediaType.MULTIPART_FORM_DATA));
			for(int i = 0; i < body.getCount(); i++) {
				BodyPart part = body.getBodyPart(i);
				if(Arrays.stream(part.getHeader(HttpHeaders.CONTENT_DISPOSITION)).anyMatch(h -> h.contains("; name=\"_method\"")) ) { //$NON-NLS-1$
					return StreamUtil.readString(part.getInputStream());
				}
			}
			return null;
		} catch (MessagingException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
