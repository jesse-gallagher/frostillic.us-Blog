<%--

    Copyright © 2012-2019 Jesse Gallagher

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
<%@tag description="Displays an individual model.Post object and its comments, if present" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@attribute name="value" required="true" type="model.Post" %>
<%@attribute name="comments" required="false" type="java.util.List" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<article class="post post-${pageScope.value.status}">
	<header>
		<h2><a href="posts/${pageScope.value.postedYear}/${pageScope.value.postedMonth}/${pageScope.value.postedDay}/${pageScope.value.slug}">${pageScope.value.title}</a></h2>
		<h3><fmt:formatDate value="${pageScope.value.postedDate}" type="BOTH" dateStyle="MEDIUM" timeStyle="SHORT" /></h3>
		<c:if test="${not empty pageScope.value.tags}">
			<div class="meta">
				${translation.tagsLabel}
				<c:forEach items="${pageScope.value.tags}" var="tag">
					<a href="posts/tag/${tag}">${tag}</a>
				</c:forEach>	
			</div>
		</c:if>
		
		<c:if test="${userInfo.admin}">
			<div class="admin">
				<a class="edit" href="posts/${pageScope.value.postedYear}/${pageScope.value.postedMonth}/${pageScope.value.postedDay}/${pageScope.value.slug}/edit">${translation.editButton}</a>
				<form method="POST" action="posts/${pageScope.value.slug}">
					<input type="submit" class="delete" value="${translation.deleteButton}" onclick="return confirm('${translation.postDeleteConfirm}')" />
					<input type="hidden" name="_method" value="DELETE" />
				</form>
			</div>
		</c:if>
	</header>
	<c:if test="${not empty pageScope.value.thread}">
		<ol class="thread" title="${pageScope.value.thread}">
			<c:forEach items="${pageScope.value.threadInfo}" var="threadEntry">
				<c:if test="${pageScope.value.id == threadEntry.id}">
					<li>${fn:escapeXml(threadEntry.title)}</li>
				</c:if>
				<c:if test="${pageScope.value.id != threadEntry.id}">
					<li><a href="posts/${threadEntry.postedYear}/${threadEntry.postedMonth}/${threadEntry.postedDay}/${threadEntry.slug}">${fn:escapeXml(threadEntry.title)}</a></li>
				</c:if>
			</c:forEach>
		</ol>
	</c:if>
	<div class='body'>
		${pageScope.value.bodyHtml}
	</div>
	
	<c:if test="${pageScope.comments != null}">
		<section class="comments" id="comments">
			<c:forEach items="${pageScope.comments}" var="comment">
				<t:comment value="${comment}"/>
			</c:forEach>
			
			<fieldset>
				<legend>${translation.newComment}</legend>
					
				<form action="posts/${pageScope.value.postId}/comments" method="POST" class="new-comment crud">
					<label for="postedBy">${translation.authorLabel}</label>
					<input type="text" name="postedBy" id="postedBy" required="required"
						value="${userInfo.anonymous ? '' : userInfo.cn}"/>
					
					<label for="postedByEmail">
						<span class="tooltip">
							${translation.emailLabel}
							<span class="tooltip-text">${translation.emailLegal}</span>
						</span>
					</label>
					<input type="email" name="postedByEmail" id="postedByEmail" required="required"
						value="${userInfo.anonymous ? '' : userInfo.emailAddress}"/>
					
					<label for="bodyMarkdown">${translation.bodyLabel}</label>
					<textarea name="bodyMarkdown" id="bodyMarkdown" required="required"></textarea>
					
					<input type="submit" value="${translation.postComment}"/>
					<div class="legal">
						<span class="tooltip">
							${translation.commentLegalLabel}
							<span class="tooltip-text">${translation.commentLegal}</span>
						</span>
					</div>
				</form>
			</fieldset>
		</section>
	</c:if>
	<c:if test="${pageScope.comments == null}">
		<div class="meta">
			<a href="posts/${pageScope.value.postedYear}/${pageScope.value.postedMonth}/${pageScope.value.postedDay}/${pageScope.value.slug}#comments">
				${pageScope.value.commentCount} Comment${pageScope.value.commentCount == 1 ? '' : 's'}
			</a>
		</div>
	</c:if>
</article>