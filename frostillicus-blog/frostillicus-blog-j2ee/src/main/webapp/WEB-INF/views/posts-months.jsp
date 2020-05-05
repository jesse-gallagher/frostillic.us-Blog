<%--

    Copyright © 2012-2020 Jesse Gallagher

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
<t:layout>
	<ul>
		<c:forEach items="${months}" var="entry">
			<h3>${entry.key}</h3>
			<c:forEach items="${entry.value}" var="m">
				<li><a href="posts/${entry.key}/${m}">${messages.getMonth(m-1)}</a></li>
			</c:forEach>
		</c:forEach>
	</ul>
</t:layout>