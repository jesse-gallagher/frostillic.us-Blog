<%--

    Copyright © 2016-2019 Jesse Gallagher

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
<%@tag description="Displays post-history navigation links" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@attribute name="start" required="true" type="java.lang.Integer"%>
<%@attribute name="pageSize" required="true" type="java.lang.Integer"%>
<%@attribute name="endOfLine" required="true" type="java.lang.Boolean"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="posts-older-newer">
    <c:if test="${pageScope.start gt 0}">
        <a class="newer" href="posts/?start=${pageScope.start - pageScope.pageSize gt 0 ? pageScope.start - pageScope.pageSize : 0}">${translation.newerPosts}</a>
    </c:if>

    <c:if test="${not pageScope.endOfLine}">
        <a class="older" href="posts/?start=${pageScope.start + pageScope.pageSize}">${translation.olderPosts}</a>
    </c:if>
    <c:if test="${pageScope.endOfLine}">
        <span class="older">${translation.olderPosts}</span>
    </c:if>
</nav>
