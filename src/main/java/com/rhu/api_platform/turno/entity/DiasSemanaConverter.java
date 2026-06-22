package com.rhu.api_platform.turno.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convierte un Set<DiaSemana> a una cadena separada por comas para almacenar en BD.
 * Preserva el orden de inserción.
 */
@Converter(autoApply = true)
public class DiasSemanaConverter implements AttributeConverter<Set<DiaSemana>, String> {

    private static final String SEPARADOR = ",";

    @Override
    public String convertToDatabaseColumn(Set<DiaSemana> dias) {
        if (dias == null || dias.isEmpty()) {
            return null;
        }
        return dias.stream()
                .map(Enum::name)
                .collect(Collectors.joining(SEPARADOR));
    }

    @Override
    public Set<DiaSemana> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(dbData.split(SEPARADOR))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(DiaSemana::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
