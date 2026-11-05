package com.fiap.feedbackhub.domain.converter;

import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class UrgenciaConverter implements AttributeConverter<Urgencia> {

    @Override
    public AttributeValue transformFrom(Urgencia input) {
        return AttributeValue.builder().s(input.name()).build();
    }

    @Override
    public Urgencia transformTo(AttributeValue input) {
        return Urgencia.valueOf(input.s());
    }

    @Override
    public EnhancedType<Urgencia> type() {
        return EnhancedType.of(Urgencia.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
