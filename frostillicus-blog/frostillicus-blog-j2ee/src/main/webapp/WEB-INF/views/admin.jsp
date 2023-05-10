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
<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" session="false" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<t:layout>
	<div class="tab-container">
		<input type="radio" id="tab2" name="tab-group" checked="checked" />
		<label for="tab2"><c:out value="${translation.links}"/></label>
		<input type="radio" id="tab1" name="tab-group" />
		<label for="tab1"><c:out value="${translation.accessTokens}"/></label>
		<div class="tabs">
			<div class="tab">
				<div class="links crud-list">
					<div class="header">
						<label><c:out value="${translation.visible}"/></label>
						<label><c:out value="${translation.category}"/></label>
						<label><c:out value="${translation.name}"/></label>
						<label><c:out value="${translation.url}"/></label>
						<label><c:out value="${translation.linkRel}"/></label>
						<label></label>
					</div>
					<c:forEach items="${links.all}" var="link">
						<form method="POST" action="admin/links/${link.id}" accept-charset="UTF-8" enctype="multipart/form-data">
							<!-- TODO see if there's a way to ditch the "span"s. They're there to act as table cells -->
							<span>
								<input type="checkbox" name="visible" value="Y" ${link.visible ? 'checked="checked"' : ''} />
							</span>
							<span>
								<input type="text" name="category" value="${fn:escapeXml(link.category)}"/>
							</span>
							<span>
								<input type="text" name="name" value="${fn:escapeXml(link.name)}"/>
							</span>
							<span>
								<input type="text" name="url" value="${fn:escapeXml(link.url)}"/>
							</span>
							<span>
								<input type="text" name="rel" value="${fn:escapeXml(link.rel)}"/>
							</span>
							<div class="actions">
								<input type="submit" name="submit" value="${fn:escapeXml(translation.saveButton)}" onclick="return confirm('${fn:escapeXml(translation.linkSaveConfirm)}')"/>
								<button type="submit" name="_method" value="DELETE" onclick="return confirm('${fn:escapeXml(translation.linkDeleteConfirm)}')"><c:out value="${translation.deleteButton}"/></button>
							</div>
						</form>
					</c:forEach>
					<div class="footer">
						<form method="POST" action="admin/links/new">
							<button type="submit"><c:out value="${translation.addButton}"/></button>
						</form>
					</div>
				</div>
			</div>
			<div class="tab">
				<div class="tokens crud-list">
					<div class="header">
						<label><c:out value="${translation.userName}"/></label>
						<label><c:out value="${translation.name}"/></label>
						<label><c:out value="${translation.token}"/></label>
						<label></label>
					</div>
					<c:forEach items="${accessTokens.all}" var="token">
						<form method="POST" action="admin/tokens/${token.id}" accept-charset="UTF-8" enctype="multipart/form-data">
							<!-- TODO see if there's a way to ditch the "span"s. They're there to act as table cells -->
							<span>
								<input type="text" name="userName" value="${fn:escapeXml(token.userName)}"/>
							</span>
							<span>
								<input type="text" name="name" value="${fn:escapeXml(token.name)}"/>
							</span>
							<span>
								<input type="text" name="token" value="${fn:escapeXml(token.token)}"/>
							</span>
							<div class="actions">
								<input type="submit" name="submit" value="${fn:escapeXml(translation.saveButton)}" onclick="return confirm('${fn:escapeXml(translation.tokenSaveConfirm)}')"/>
								<button type="submit" name="_method" value="DELETE" onclick="return confirm('${fn:escapeXml(translation.tokenDeleteConfirm)}')">${fn:escapeXml(translation.deleteButton)}</button>
							</div>
						</form>
					</c:forEach>
					<div class="footer">
						<form method="POST" action="admin/tokens/new">
							<button type="submit"><c:out value="${translation.addButton}"/></button>
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
</t:layout>