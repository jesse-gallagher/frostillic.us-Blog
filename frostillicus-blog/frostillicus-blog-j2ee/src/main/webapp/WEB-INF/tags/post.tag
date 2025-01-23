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
<%@tag description="Displays an individual model.Post object and its comments, if present" trimDirectiveWhitespaces="true" %>
<%@attribute name="value" required="true" type="model.Post" %>
<%@attribute name="comments" required="false" type="java.util.List" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<article class="post post-${pageScope.value.status}">
	<header>
		<c:if test="${userInfo.admin}">
			<div class="admin">
				<a class="edit" href="posts/${pageScope.value.postedYear}/${pageScope.value.postedMonth}/${pageScope.value.postedDay}/${pageScope.value.slug}/edit"><c:out value="${translation.editButton}"/></a>
				<form method="POST" action="posts/${encoder.urlEncode(pageScope.value.slug)}" enctype="multipart/form-data">
					<input type="submit" class="delete" value="${fn:escapeXml(translation.deleteButton)}" onclick="return confirm('${fn:escapeXml(translation.postDeleteConfirm)}')" />
					<input type="hidden" name="_method" value="DELETE" />
				</form>
			</div>
		</c:if>
		
		<h2><a href="posts/${pageScope.value.postedYear}/${pageScope.value.postedMonth}/${pageScope.value.postedDay}/${pageScope.value.slug}"><c:out value="${pageScope.value.title}"/></a></h2>
		<h3><fmt:formatDate value="${pageScope.value.postedDate}" type="BOTH" dateStyle="MEDIUM" timeStyle="SHORT" /></h3>
		<c:if test="${not empty pageScope.value.tags}">
			<div class="meta">
				<c:out value="${translation.tagsLabel}"/>
				<c:forEach items="${pageScope.value.tags}" var="tag">
					<a href="posts/tag/${encoder.urlEncode(tag)}">${fn:escapeXml(tag)}</a>
				</c:forEach>	
			</div>
		</c:if>
	</header>
	<c:if test="${not empty pageScope.value.thread}">
		<ol class="thread" title="${fn:escapeXml(pageScope.value.thread)}">
			<c:forEach items="${pageScope.value.threadInfo}" var="threadEntry">
				<c:if test="${pageScope.value.id == threadEntry.id}">
					<li><c:out value="${messages.format('postDateAndTitle', messages.getFriendlyDate(threadEntry.posted), threadEntry.title)}"/></li>
				</c:if>
				<c:if test="${pageScope.value.id != threadEntry.id}">
					<li>
						<a href="posts/${threadEntry.postedYear}/${threadEntry.postedMonth}/${threadEntry.postedDay}/${threadEntry.slug}">
							<c:out value="${messages.format('postDateAndTitle', messages.getFriendlyDate(threadEntry.posted), threadEntry.title)}"/>
						</a>
					</li>
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
				<legend><c:out value="${translation.newComment}"/></legend>
					
				<form action="posts/${pageScope.value.postId}/comments" method="POST" class="new-comment crud" enctype="application/x-www-form-urlencoded"
					onsubmit="this.querySelector('input[name=\'shim\']').name = '${fn:escapeXml(mvc.csrf.name)}'">
					<label for="postedBy"><c:out value="${translation.authorLabel}"/></label>
					<input type="text" name="postedBy" id="postedBy" required="required"
						value="${userInfo.anonymous ? '' : userInfo.cn}"/>
					
					<label for="postedByEmail">
						<span class="tooltip">
							<c:out value="${translation.emailLabel}"/>
							<span class="tooltip-text"><c:out value="${translation.emailLegal}"/></span>
						</span>
					</label>
					<input type="email" name="postedByEmail" id="postedByEmail" required="required"
						value="${userInfo.anonymous ? '' : userInfo.emailAddress}"/>
					
					<label for="bodyMarkdown"><c:out value="${translation.bodyLabel}"/></label>
					<textarea name="bodyMarkdown" id="bodyMarkdown" required="required"></textarea>
					
					<input type="submit" value="${fn:escapeXml(translation.postComment)}"/>
					<div class="legal">
						<span class="tooltip">
							<c:out value="${translation.commentLegalLabel}"/>
							<span class="tooltip-text"><c:out value="${translation.commentLegal}" escapeXml="false"/></span>
						</span>
					</div>
					<input type="hidden" name="shim" value="${mvc.csrf.token}"/>
				</form>
			</fieldset>
		</section>
	</c:if>
	<c:if test="${pageScope.comments == null}">
		<div class="meta">
			<a href="posts/${pageScope.value.postedYear}/${pageScope.value.postedMonth}/${pageScope.value.postedDay}/${pageScope.value.slug}#comments">
				<c:out value="${messages.format('commentCount', pageScope.value.commentCount)}"/>
			</a>
		</div>
	</c:if>
</article>