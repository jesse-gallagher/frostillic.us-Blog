/*
 * Copyright © 2012-2022 Jesse Gallagher
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
package api.atompub;

import org.w3c.dom.Document;

import com.darwino.commons.xml.DomUtil;

enum AtomPubUtil {
    ;

    public Document createResponse(final String tagName) {
        var result = DomUtil.createDocument();
        var root = DomUtil.createRootElement(result, tagName);
        root.setAttribute("xmlns", "http://purl.org/atom/app#"); //$NON-NLS-1$ //$NON-NLS-2$
        root.setAttribute("xmlns:atom", "http://www.w3.org/2005/Atom"); //$NON-NLS-1$ //$NON-NLS-2$
        root.setAttribute("xmlns:app", "http://www.w3.org/2007/app"); //$NON-NLS-1$ //$NON-NLS-2$

        return result;
    }
}
