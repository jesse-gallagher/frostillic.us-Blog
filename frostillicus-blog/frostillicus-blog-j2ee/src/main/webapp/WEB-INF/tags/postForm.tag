<%--

    Copyright © 2016-2018 Jesse Gallagher

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
<%@attribute name="value" required="true" type="model.Post" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<form name="form" action="posts" method="post" class="crud">
	<label for="title">${translation.titleLabel}</label>
	<input type="text" name="title" id="title" required="required" autofocus="autofocus" value="${pageScope.value.title}" />
	
	<label for="tags">${translation.tagsLabel}</label>
	<input type="text" name="tags" id="tags" value="${fn:join(pageScope.value.tags.toArray(),', ')}"/>

	<label for="bodyMarkdown">${translation.bodyLabel}</label>
	<textarea name="bodyMarkdown" id="bodyMarkdown"><c:out value="${empty pageScope.value.bodyMarkdown ? pageScope.value.bodyHtml : pageScope.value.bodyMarkdown}"/></textarea>
		
	<input type="submit" value="${translation.savePost}"/>
</form>