<%--

    Copyright © 2012-2023 Jesse Gallagher

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
<%@page contentType="text/html" trimDirectiveWhitespaces="true" session="false" %>
<!DOCTYPE html>
<html lang="en-us">
	<head>
		<meta http-equiv="x-ua-compatible" content="ie=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=no" />
		
		<link rel="shortcut icon" href="${CONTEXT_PATH}/img/icon.png" />
		<link rel="apple-touch-icon" href="${CONTEXT_PATH}/img/icon.png" />
		<link rel="stylesheet" href="${CONTEXT_PATH}/css/style.css" />
		<link rel="stylesheet" href="${CONTEXT_PATH}/css/forms.css" />
		
		<title>frostillic.us Error</title>
	</head>
	<body>
		<div id="entirety">
			<header id="pageheader">
			</header>
			<nav id="pagenav">
				
			</nav>
			<div id="pagebody">
				<h2 class="title">Error</h2>
				<p class="error" style="color: red">
					${ERROR_MESSAGE}
				</p>
				<pre style="white-space: pre-wrap">${ERROR_STACK_TRACE}</pre>
			</div>
			<footer id="pagefooter">
				
			</footer>
		</div>
	</body>
</html>
