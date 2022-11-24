<%--

    Copyright © 2012-2022 Jesse Gallagher

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
<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" session="false" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<t:layout>
	<fieldset>
		<legend>${translation.newMicroPost}</legend>
			
		<form action="micropub" method="POST" class="crud" enctype="multipart/form-data">
			<label for="name">${translation.name}</label>
			<input type="text" name="name" id="name"/>
			
			<label for="content">${translation.bodyLabel}</label>
			<textarea name="content" id="content" required="required"></textarea>
			
			<input type="hidden" name="h" value="entry" />
			<input type="submit" value="${translation.post}"/>
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