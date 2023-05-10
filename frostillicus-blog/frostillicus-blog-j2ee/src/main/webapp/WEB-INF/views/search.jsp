<%--

    Copyright © 2012-2023 Jesse Gallagher

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
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<t:layout>
	<form action="posts/search" method="GET" class="search">
		<input class="search" name="q" value="${fn:escapeXml(uriInfoBean.getParam('q'))}"/>
		<button type="submit"><c:out value="${translation.searchButton}"/></button>
	</form>

	<c:forEach items="${posts}" var="post">
		<t:post value="${post}"/>
	</c:forEach>
</t:layout>