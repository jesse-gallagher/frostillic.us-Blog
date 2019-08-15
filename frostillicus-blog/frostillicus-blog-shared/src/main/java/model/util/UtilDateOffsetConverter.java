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
