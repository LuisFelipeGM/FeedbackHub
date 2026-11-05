package com.fiap.feedbackhub.domain.entity;

import com.fiap.feedbackhub.domain.converter.UrgenciaConverter;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@DynamoDbBean
public class Avaliacao {

    private UUID id;
    private Integer nota;
    private Urgencia urgencia;
    private String descricao;
    private String data;
    private String dataHoraEnvio;

    public static Avaliacao novo(String descricao, int nota, LocalDateTime momento) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(UUID.randomUUID());
        avaliacao.setNota(nota);
        avaliacao.setUrgencia(Urgencia.apartirDaNota(nota));
        avaliacao.setDescricao(descricao);
        avaliacao.setData(momento.toLocalDate().toString());
        avaliacao.setDataHoraEnvio(momento.toString());
        return avaliacao;
    }

    @DynamoDbPartitionKey
    public String getData() {
        return data;
    }

    @DynamoDbSortKey
    public UUID getId() {
        return id;
    }

    @DynamoDbConvertedBy(UrgenciaConverter.class)
    public Urgencia getUrgencia() {
        return urgencia;
    }
}
