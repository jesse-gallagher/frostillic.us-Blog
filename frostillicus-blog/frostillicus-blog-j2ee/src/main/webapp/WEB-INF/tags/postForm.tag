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
<%@tag description="Displays a model.Post in an editable form" trimDirectiveWhitespaces="true" %>
<%@attribute name="value" required="true" type="model.Post" %>
<%@attribute name="edit" required="true" type="java.lang.Boolean" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<form name="form" action="posts/${pageScope.edit ? post.id : ''}" method="post" class="crud" enctype="multipart/form-data">
	<label for="title"><c:out value="${translation.titleLabel}"/></label>
	<input type="text" name="title" id="title" required="required" autofocus="autofocus" value="${fn:escapeXml(pageScope.value.title)}" />
	
	<label for="tags"><c:out value="${translation.tagsLabel}"/></label>
	<input type="text" name="tags" id="tags" value="${fn:escapeXml(fn:join(pageScope.value.tags.toArray(),', '))}"/>

	<label for="thread"><c:out value="${translation.threadLabel}"/></label>
	<input type="text" name="thread" id="thread" value="${fn:escapeXml(pageScope.value.thread)}"/>
	
	<label for="status"><c:out value="${translation.statusLabel}"/></label>
	<div class='radio-group'>
		<input type="radio" name="status" ${pageScope.value.status == 'Draft' or empty pageScope.value.status ? 'checked="checked"' : ''} value="Draft" /> <c:out value="${translation.draft}"/>
		<input type="radio" name="status" ${pageScope.value.status == 'Posted' ? 'checked="checked"' : ''} value="Posted" /><c:out value="${translation.posted}"/>
	</div>

	<label for="bodyMarkdown"><c:out value="${translation.bodyLabel}"/></label>
	<textarea name="bodyMarkdown" id="bodyMarkdown"><c:out value="${empty pageScope.value.bodyMarkdown ? pageScope.value.bodyHtml : pageScope.value.bodyMarkdown)}"/></textarea>
		
	<input type="submit" value="${fn:escapeXml(translation.savePost)}"/>
	<c:if test="${pageScope.edit}">
		<input type="hidden" name="_method" value="PUT" />
	</c:if>
</form>