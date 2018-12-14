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
<%@tag description="Displays List<model.Post>" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@attribute name="value" required="true" type="java.util.List" %>
<%@attribute name="start" required="false" type="java.lang.Integer"%>
<%@attribute name="pageSize" required="false" type="java.lang.Integer"%>
<%@attribute name="endOfLine" required="false" type="java.lang.Boolean"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${pageScope.start != null}">
    <t:postsOlderNewer start="${pageScope.start}" pageSize="${pageScope.pageSize}" endOfLine="${pageScope.endOfLine}"/>
</c:if>
<c:forEach items="${pageScope.value}" var="post">
    <t:post value="${post}"/>
</c:forEach>
<c:if test="${pageScope.start != null}">
    <t:postsOlderNewer start="${pageScope.start}" pageSize="${pageScope.pageSize}" endOfLine="${pageScope.endOfLine}"/>
</c:if>