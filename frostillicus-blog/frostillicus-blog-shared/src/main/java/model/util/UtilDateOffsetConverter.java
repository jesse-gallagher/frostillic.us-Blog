/**
 * Copyright © 2012-2020 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package model.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

import jakarta.nosql.mapping.AttributeConverter;

/**
 * This converter converts between {@link OffsetDateTime} values and {@link Date} objects.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class UtilDateOffsetConverter implements AttributeConverter<OffsetDateTime, Date> {
	@Override
	public Date convertToDatabaseColumn(OffsetDateTime attribute) {
		if(attribute == null) {
			return null;
		} else {
			return Date.from(attribute.toInstant());
		}
	}

	/**
	 * @throws IllegalArgumentException if the provided string cannot be parsed to a date
	 */
	@Override
	public OffsetDateTime convertToEntityAttribute(Date dbData) {
		if(dbData == null) {
			return null;
		} else {
			return OffsetDateTime.ofInstant(dbData.toInstant(), ZoneId.systemDefault());
		}
	}
}
