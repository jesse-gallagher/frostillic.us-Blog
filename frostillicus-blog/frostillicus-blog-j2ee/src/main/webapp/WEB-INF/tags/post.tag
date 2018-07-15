<%@attribute name="value" required="true" type="model.Post" %>
<%@attribute name="comments" required="false" type="java.util.List" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<article>
	<header>
		<h2><a href="posts/${pageScope.value.id}">${pageScope.value.title}</a></h2>
		<h3>${pageScope.value.posted}</h3>
	</header>
	${pageScope.value.bodyHtml}
	
	<c:if test="${not empty pageScope.comments}">
		<section class="comments">
			<c:forEach items="${pageScope.comments}" var="comment">
				<t:comment value="${comment}"/>
			</c:forEach>
		</section>
	</c:if>
</article>