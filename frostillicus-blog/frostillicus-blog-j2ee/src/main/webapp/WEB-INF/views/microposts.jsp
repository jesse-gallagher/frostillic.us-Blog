<%--

    Copyright (c) 2012-2025 Jesse Gallagher

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@page contentType="text/html" trimDirectiveWhitespaces="true" session="false" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<t:layout>
	<fieldset>
		<legend><c:out value="${translation.newMicroPost}"/></legend>
			
		<form action="micropub" method="POST" class="crud" enctype="multipart/form-data">
			<label for="name"><c:out value="${translation.name}"/></label>
			<input type="text" name="name" id="name"/>
			
			<label for="content"><c:out value="${translation.bodyLabel}"/></label>
			<textarea name="content" id="content" required="required"></textarea>
			
			<input type="hidden" name="h" value="entry" />
			<input type="submit" value="${fn:escapeXml(translation.post)}"/>
		</form>
	</fieldset>
	
	<table>
		<tbody>
<c:forEach items="${posts}" var="post">
    		<tr>
    			<td>
    				<fmt:formatDate value="${post.postedDate}" type="both" dateStyle="medium" timeStyle="short" />
    			</td>
    			<td><c:out value="${fn:escapeXml(post.name)}"/></td>
    			<td><c:out value="${fn:escapeXml(post.content)}"/></td>
    		</tr>
</c:forEach>
		</tbody>
	</table>
</t:layout>