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
		<link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" />
	</head>
	<body>
		<div id="pageheader">
			<jsp:invoke fragment="header" />
		</div>
		<div id="body">
			<jsp:doBody />
		</div>
		<div id="pagefooter">
			<jsp:invoke fragment="footer" />
		</div>
	</body>
</html>