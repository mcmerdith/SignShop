package org.wargamer2010.signshop.configuration.orm.typemapping.conversion;

public interface SSAttributeConverter<J, S> {
    S convertToDatabaseColumn(J javaObject);

    J convertToModelAttribute(S databaseObject);
}
