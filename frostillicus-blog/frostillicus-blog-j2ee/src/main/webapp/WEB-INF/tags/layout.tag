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
<%@tag description="Overall Page template" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="${translation._lang}">
	<head>
		<meta http-equiv="x-ua-compatible" content="ie=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=no" />
		<meta name="turbolinks-root" content="${pageContext.request.contextPath}" />
		
		<base href="${pageContext.request.contextPath}/" />
		
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/icon.png" />
		<link rel="apple-touch-icon" href="icon.png" />
		<link rel="alternate" href="${pageContext.request.contextPath}/feed.xml" type="application/rss+xml" title="frostillic.us &gt; Feed">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/forms.css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/tabs.css" />
		
		<script type="text/javascript" src="${pageContext.request.contextPath}/js/turbolinks.js"></script>
		
		<title>frostillic.us</title>
	</head>
	<body>
		<div id="entirety">
			<header id="pageheader">
				<a href="${pageContext.request.contextPath}">${translation.appTitle}</a>
			</header>
			<nav id="pagenav">
				<header class="authorinfo">
					<img src="$darwino-social/users/users/cn%3Djesse%2Co%3Ddarwino/content/photo" class="photo" alt="${translation.authorPhoto}"/>
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
				
				<c:forEach items="${links.byCategory}" var="cat">
					<ul title="${cat.key}">
						<c:forEach items="${cat.value}" var="link">
							<li><a href="${link.url}">${link.name}</a></li>
						</c:forEach>
					</ul>
				</c:forEach>
				
				<c:if test="${userInfo.admin}">
					<ul title="${translation.admin}">
						<li><a href="admin">${translation.adminPanel}</a></li>
						<li><a href="posts/new">${translation.newPost}</a></li>
					</ul>
				</c:if>
			</nav>
			<div id="pagebody">
				<c:if test="${not empty redirectMessages}">
					<ul>
						<c:forEach items="${redirectMessages}" var="message">
							<li>${message}</li>
						</c:forEach>
					</ul>
				</c:if>
			
				<jsp:doBody />
			</div>
			<footer id="pagefooter">
				<p>${translation.copyright}</p>
			</footer>
		</div>
	</body>
</html>