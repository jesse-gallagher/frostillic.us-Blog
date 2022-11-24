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
<%@tag description="Overall Page template" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
		
		<link rel="alternate" href="${uriInfoBean.requestUri.resolve('feed.json')}" type="application/json" title="${fn:escapeXml(translation.feedJson)}">
		<link rel="alternate" href="${uriInfoBean.requestUri.resolve('feed.xml')}" type="application/rss+xml" title="${fn:escapeXml(translation.feedRss)}">
		<link rel="EditURI" type="application/rsd+xml" href="${uriInfoBean.requestUri.resolve('rsd.xml')}" />
		<link rel="webmention" href="${uriInfoBean.requestUri.resolve('webmention')}" />
		
		<script type="text/javascript" src="${pageContext.request.contextPath}/webjars/hotwired__turbo/7.2.4/dist/turbo.es2017-esm.js"></script>
		
		<title>${translation.appTitle}</title>
	</head>
	<body>
		<div id="entirety">
			<header id="pageheader">
				<a href="${pageContext.request.contextPath}">${translation.appTitle}</a>
			</header>
			<nav id="pagenav">
				<input type="checkbox" id="navbar-toggle" class="mobile-nav" aria-hidden="true"/>
				<div class="sidebar-content">
					<header class="authorinfo">
						<img src="${userInfo.getImageUrl(translation.authorEmail)}" class="photo" alt="${translation.authorPhoto}"/>
					</header>
					<ul class="sitenav">
						<li><a href="${pageContext.request.contextPath}/">${translation.home}</a></li>
						<li><a href="posts">${translation.archive}</a></li>
						<c:if test="${darwinoSession.user.anonymous}">
							<li data-turbolinks="false"><a href="?login">${translation.logIn}</a></li>
						</c:if>
						<c:if test="${not darwinoSession.user.anonymous}">
							<li data-turbolinks="false"><a href="?logout">${translation.logOut}</a></li>
						</c:if>
					</ul>
					
					<form action="posts/search" method="GET" class="inline-search">
						<input name="q" id="quick-search" aria-label="${translation.quickSearch}"/>
						<button type="submit">${translation.searchButton}</button>
					</form>
					
					<c:forEach items="${links.byCategory}" var="cat">
						<ul title="${cat.key}">
							<c:forEach items="${cat.value}" var="link">
								<li><a href="${link.url}" rel="${link.rel}">${link.name}</a></li>
							</c:forEach>
						</ul>
					</c:forEach>
					
					<c:if test="${userInfo.admin}">
						<ul title="${translation.admin}">
							<li><a href="admin">${translation.adminPanel}</a></li>
							<li><a href="admin/console">${translation.adminConsole}</a></li>
							<li><a href="posts/new">${translation.newPost}</a></li>
						</ul>
					</c:if>
				</div>
			</nav>
			<main id="pagebody">
				<c:if test="${not empty redirectMessages}">
					<ul>
						<c:forEach items="${redirectMessages}" var="message">
							<li>${message}</li>
						</c:forEach>
					</ul>
				</c:if>
			
				<jsp:doBody />
			</main>
			<footer id="pagefooter">
				<p>${translation.copyright}</p>
			</footer>
		</div>
	</body>
</html>