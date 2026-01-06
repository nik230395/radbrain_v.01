package org.nikolic.programm.entities;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        if (attribute == null) {
            return UserRole.USER.name();
        }
        return attribute.name(); // Returns "ADMIN" or "USER"
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        System.out.println("DEBUG: Converting DB value to enum:  '" + dbData + "'");

        if (dbData == null || dbData.trim().isEmpty()) {
            return UserRole.USER;
        }

        // Handle both uppercase and lowercase
        String normalized = dbData.trim().toUpperCase();
        try {
            return UserRole.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            System.out.println("WARNING: Invalid role '" + dbData + "', defaulting to USER");
            return UserRole.USER;
        }
    }
}