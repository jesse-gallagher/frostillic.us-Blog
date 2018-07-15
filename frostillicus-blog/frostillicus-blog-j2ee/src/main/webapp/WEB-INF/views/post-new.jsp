<html>
	<head>
	</head>
	<body>
		<form name="form" action="/blog/posts" method="post">
			<div>
				<label for="title">Title:</label>
				<input type="text" name="title" id="title"/>
			</div>
			<div>
				<label for="bodyMarkdown">Body:</label>
				<textarea name="bodyMarkdown" id="bodyMarkdown"></textarea>
			</div>
			<input type="submit" value="Submit"/>
		</form>
	</body>
</html>