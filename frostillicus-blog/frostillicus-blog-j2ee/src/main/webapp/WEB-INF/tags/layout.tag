<%--

    Copyright (c) 2012-2023 Jesse Gallagher

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
<%@tag description="Overall Page template" trimDirectiveWhitespaces="true" %>
<%@attribute name="ogTitle" required="false" type="java.lang.String" %>
<%@attribute name="ogDescription" required="false" type="java.lang.String" %>
<%@attribute name="ogImage" required="false" type="java.lang.String" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="${translation._lang}">
	<head>
		<meta http-equiv="x-ua-compatible" content="ie=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=no" />
		<meta name="turbolinks-root" content="${pageContext.request.contextPath}" />
		
		<base href="${pageContext.request.contextPath}/" />
		
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/img/icon.png" />
		<link rel="apple-touch-icon" href="${pageContext.request.contextPath}/img/icon.png" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/forms.css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/tabs.css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/tooltips.css" />
		
		<link rel="alternate" href="${urlBean.requestUri.resolve('feed.json')}" type="application/json" title="${fn:escapeXml(translation.feedJson)}">
		<link rel="alternate" href="${urlBean.requestUri.resolve('feed.xml')}" type="application/rss+xml" title="${fn:escapeXml(translation.feedRss)}">
		<link rel="EditURI" type="application/rsd+xml" href="${urlBean.requestUri.resolve('rsd.xml')}" />
		<link rel="webmention" href="${urlBean.requestUri.resolve('webmention')}" />
		
		<script type="module" src="${pageContext.request.contextPath}/webjars/hotwired__turbo/7.3.0/dist/turbo.es2017-esm.js"></script>
		
		<c:forEach items="${metaTags.all}" var="metaTag">
		<meta name="${fn:escapeXml(metaTag.name)}" content="${fn:escapeXml(metaTag.content)}"/>
		</c:forEach>
<c:if test="${not empty pageScope.ogTitle}">
		<meta property="og:title" content="${fn:escapeXml(pageScope.ogTitle)}" />
</c:if>
<c:if test="${not empty pageScope.ogDescription}">
		<meta property="og:description" content="${fn:escapeXml(pageScope.ogDescription)}" />
</c:if>
<c:if test="${not empty pageScope.ogImage}">
		<meta property="og:image" content="${fn:escapeXml(pageScope.ogImage)}" />
</c:if>
		
		<title><c:out value="${translation.appTitle}"/></title>
	</head>
	<body>
		<div id="entirety">
			<header id="pageheader">
				<a href="${pageContext.request.contextPath}"><c:out value="${translation.appTitle}"/></a>
			</header>
			<nav id="pagenav">
				<input type="checkbox" id="navbar-toggle" class="mobile-nav" aria-hidden="true"/>
				<div class="sidebar-content">
					<header class="authorinfo">
						<img src="${userInfo.getImageUrl(translation.authorEmail)}" class="photo" alt="${fn:escapeXml(translation.authorPhoto)}"/>
					</header>
					<ul class="sitenav">
						<li><a href="${pageContext.request.contextPath}/"><c:out value="${translation.home}"/></a></li>
						<li><a href="posts"><c:out value="${translation.archive}"/></a></li>
						<c:if test="${userInfo.anonymous}">
							<li data-turbolinks="false"><a href="?login"><c:out value="${translation.logIn}"/></a></li>
						</c:if>
						<c:if test="${not userInfo.anonymous}">
							<li data-turbolinks="false"><a href="?logout"><c:out value="${translation.logOut}"/></a></li>
						</c:if>
					</ul>
					
					<form action="posts/search" method="GET" class="inline-search">
						<input name="q" id="quick-search" aria-label="${fn:escapeXml(translation.quickSearch)}"/>
						<button type="submit"><c:out value="${translation.searchButton}"/></button>
					</form>
					
					<c:forEach items="${links.byCategory}" var="cat">
						<ul title="${cat.key}">
							<c:forEach items="${cat.value}" var="link">
								<li><a href="${link.url}" rel="${link.rel}"><c:out value="${link.name}"/></a></li>
							</c:forEach>
						</ul>
					</c:forEach>
					
					<c:if test="${userInfo.admin}">
						<ul title="${fn:escapeXml(translation.admin)}">
							<li><a href="admin"><c:out value="${translation.adminPanel}"/></a></li>
							<li><a href="admin/console"><c:out value="${translation.adminConsole}"/></a></li>
							<li><a href="posts/new"><c:out value="${translation.newPost}"/></a></li>
						</ul>
					</c:if>
				</div>
			</nav>
			<main id="pagebody">
				<c:if test="${not empty redirectMessages}">
					<ul>
						<c:forEach items="${redirectMessages}" var="message">
							<li><c:out value="${message}"/></li>
						</c:forEach>
					</ul>
				</c:if>
			
				<jsp:doBody />
			</main>
			<footer id="pagefooter">
				<p><c:out value="${translation.copyright}" escapeXml="false"/></p>
			</footer>
		</div>
	</body>
</html>