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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<t:layout>
	<div class="tab-container">
		<input type="radio" id="tab2" name="tab-group" checked="checked" />
		<label for="tab2">${translation.links}</label>
		<input type="radio" id="tab1" name="tab-group" />
		<label for="tab1">${translation.generalSettings}</label>
		<div class="tabs">
			<div class="tab">
				<div class="links crud-list">
					<div class="header">
						<label>${translation.visible}</label>
						<label>${translation.category}</label>
						<label>${translation.name}</label>
						<label>${translation.url}</label>
						<label></label>
					</div>
					<c:forEach items="${links.all}" var="link">
						<form method="POST" action="admin/links/${link.id}" accept-charset="UTF-8">
							<!-- TODO figure out a good way to submit the delete -->
							<!-- TODO see if there's a way to ditch the "span"s. They're there to act as table cells -->
							<span>
								<input type="checkbox" name="visible" value="Y" ${link.visible ? 'checked="checked"' : ''} />
							</span>
							<span>
								<input type="text" name="category" value="${link.category}"/>
							</span>
							<span>
								<input type="text" name="name" value="${link.name}"/>
							</span>
							<span>
								<input type="text" name="url" value="${link.url}"/>
							</span>
							<div class="actions">
								<input type="submit" name="submit" value="${translation.saveButton}" onclick="return confirm('${translation.linkSaveConfirm}')"/>
								<button type="submit" name="_method" value="DELETE" onclick="return confirm('${translation.linkDeleteConfirm}')">${translation.deleteButton}</button>
							</div>
						</form>
					</c:forEach>
					<div class="footer">
						<form method="POST" action="admin/links/new">
							<button type="submit">${translation.addButton}</button>
						</form>
					</div>
				</div>
			</div>
			<div class="tab">
				General Settings
			</div>
		</div>
	</div>
</t:layout>