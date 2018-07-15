<%@attribute name="value" required="true" type="model.Comment" %>
<article class="comment">
	<header>
		<h3>${pageScope.value.postedBy}</h3>
	</header>
	${pageScope.value.bodyHtml}
</article>