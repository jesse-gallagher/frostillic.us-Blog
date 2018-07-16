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
<%@attribute name="header" fragment="true"%>
<%@attribute name="footer" fragment="true"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="x-ua-compatible" content="ie=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=no" />
		
		<base href="${pageContext.request.contextPath}/" />
		
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/icon.png" />
		<link rel="apple-touch-icon" href="icon.png" />
		<link rel="alternate" href="${pageContext.request.contextPath}/feed.xml" type="application/rss+xml" title="frostillic.us &gt; Feed">
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
	</head>
	<body>
		<div id="entirety">
			<header id="pageheader">
				<a href="${pageContext.request.contextPath}">frostillic.us</a>
			</header>
			<nav id="pagenav">
				<header class="authorinfo">
					<img src="https://secure.gravatar.com/avatar/5aada48ea6558e53a94955db8ffe91b8?s=128" class="photo"/>
				</header>
				<ul class="sitenav">
					<li><a href="${pageContext.request.contextPath}/">Home</a></li>
					<li><a href="posts">Archives</a></li>
					<c:if test="${darwinoSession.user.anonymous}">
						<li><a href="?login">Log In</a></li>
					</c:if>
					<c:if test="${not darwinoSession.user.anonymous}">
						<li><a href="?logout">Log Out</a></li>
					</c:if>
				</ul>
			</nav>
			<div id="pagebody">
				<jsp:doBody />
			</div>
			<footer id="pagefooter">
				<p>
					Except as otherwise noted, the content of this page is licensed under the
					<a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 License</a>
					and code samples are licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a>.
				</p>
			</footer>
		</div>
	</body>
</html>