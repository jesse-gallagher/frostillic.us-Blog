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
<%@tag description="Displays a model.Post in an editable form" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@attribute name="value" required="true" type="model.Post" %>
<%@attribute name="edit" required="true" type="java.lang.Boolean" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<form name="form" action="posts/${pageScope.edit ? post.id : ''}" method="post" class="crud" enctype="multipart/form-data">
	<label for="title">${translation.titleLabel}</label>
	<input type="text" name="title" id="title" required="required" autofocus="autofocus" value="${fn:escapeXml(pageScope.value.title)}" />
	
	<label for="tags">${translation.tagsLabel}</label>
	<input type="text" name="tags" id="tags" value="${fn:escapeXml(fn:join(pageScope.value.tags.toArray(),', '))}"/>

	<label for="thread">${translation.threadLabel}</label>
	<input type="text" name="thread" id="thread" value="${fn:escapeXml(pageScope.value.thread)}"/>
	
	<label for="status">${translation.statusLabel}</label>
	<div class='radio-group'>
		<input type="radio" name="status" ${pageScope.value.status == 'Draft' or empty pageScope.value.status ? 'checked="checked"' : ''} value="Draft" /> ${translation.draft}
		<input type="radio" name="status" ${pageScope.value.status == 'Posted' ? 'checked="checked"' : ''} value="Posted" /> ${translation.posted}
	</div>

	<label for="bodyMarkdown">${translation.bodyLabel}</label>
	<textarea name="bodyMarkdown" id="bodyMarkdown">${fn:escapeXml(empty pageScope.value.bodyMarkdown ? pageScope.value.bodyHtml : pageScope.value.bodyMarkdown)}</textarea>
		
	<input type="submit" value="${translation.savePost}"/>
	<c:if test="${pageScope.edit}">
		<input type="hidden" name="_method" value="PUT" />
	</c:if>
</form>