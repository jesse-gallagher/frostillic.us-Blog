<%@tag description="Overall Page template" pageEncoding="UTF-8"%>
<%@attribute name="header" fragment="true"%>
<%@attribute name="footer" fragment="true"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="x-ua-compatible" content="ie=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
		
		<base href="${pageContext.request.contextPath}/" />
		
		<link rel="shortcut icon" href="${pageContext.request.contextPath}/icon.png" />
		<link rel="apple-touch-icon" href="icon.png" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/normalize.css" />
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
	</head>
	<body>
		<header id="pageheader">
			<a href="${pageContext.request.contextPath}">frostillic.us</a>
		</header>
		<nav id="pagenav">
			I'm nav
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
	</body>
</html>