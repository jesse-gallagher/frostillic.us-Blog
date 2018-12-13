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
<%@tag description="Displays an individual model.Comment object in a list" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@attribute name="value" required="true" type="model.Comment" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<article class="comment">
	<img class="photo" src="${userInfo.getImageUrl(pageScope.value.postedByEmail)}" alt="${translation.commenterPhoto}"/>
	<h3>
		${pageScope.value.postedBy}
		-
		<fmt:formatDate value="${pageScope.value.posted}" type="both" dateStyle="medium" timeStyle="short" />
	</h3>
	<c:if test="${userInfo.admin}">
		<div class="admin">
			<form method="POST" action="posts/${pageScope.value.postId}/comments/${pageScope.value.commentId}">
				<input type="submit" class="delete" value="${translation.deleteButton}" onclick="return confirm('${translation.commentDeleteConfirm}')" />
				<input type="hidden" name="_method" value="DELETE" />
			</form>
		</div>
	</c:if>
	<div class="comment-body">
		${pageScope.value.bodyHtml}
	</div>
</article>