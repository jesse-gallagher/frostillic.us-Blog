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
<%@attribute name="comments" required="false" type="java.util.List" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<article class="post">
	<header>
		<h2><a href="posts/${pageScope.value.postedYear}/${pageScope.value.postedMonth+1}/${pageScope.value.postedDay}/${pageScope.value.postId}">${pageScope.value.title}</a></h2>
		<h3><fmt:formatDate value="${pageScope.value.posted}" type="BOTH" dateStyle="MEDIUM" timeStyle="SHORT" /></h3>
		
		<c:if test="${userInfo.admin}">
			<div class="admin">
				<form method="POST" action="posts/${pageScope.value.postId}">
					<input type="submit" class="delete" value="${translation.deleteButton}" onclick="return confirm('${translation.postDeleteConfirm}')" />
					<input type="hidden" name="_method" value="DELETE" />
				</form>
			</div>
		</c:if>
	</header>
	<div class='body'>
		${pageScope.value.bodyHtml}
	</div>
	
	<c:if test="${pageScope.comments != null}">
		<section class="comments">
			<c:forEach items="${pageScope.comments}" var="comment">
				<t:comment value="${comment}"/>
			</c:forEach>
			
			<form action="posts/${pageScope.value.postId}/comments" method="POST">
				<fieldset class="new-comment">
					<legend>
					</legend>
					<div>
						<label for="postedBy">${translation.authorLabel}</label>
						<input type="text" name="postedBy" id="postedBy" required="required"
							value="${userInfo.anonymous ? '' : userInfo.cn}"/>
					</div>
					<div>
						<label for="bodyMarkdown">${translation.bodyLabel}</label>
						<textarea name="bodyMarkdown" id="bodyMarkdown" required="required"></textarea>
					</div>
					<input type="submit" value="${translation.postComment}"/>
				</fieldset>
			</form>
		</section>
	</c:if>
	<c:if test="${pageScope.comments == null}">
		<div class="meta">${pageScope.value.commentCount} Comment${pageScope.value.commentCount == 1 ? '' : 's'}</div>
	</c:if>
</article>