package com.fiap.feedbackhub.repository;

import com.fiap.feedbackhub.domain.entity.Avaliacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.LocalDate;
import java.util.List;

@Repository
public class AvaliacaoRepository {

    private final DynamoDbTable<Avaliacao> tabela;

    public AvaliacaoRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.tabela-avaliacoes}")
            String tabelaAvaliacoes
    ) {
        this.tabela = enhancedClient.table(tabelaAvaliacoes, TableSchema.fromBean(Avaliacao.class));
    }

    public void salvar(Avaliacao avaliacao) {
        tabela.putItem(avaliacao);
    }

    public List<Avaliacao> buscarPorData(LocalDate data) {
        QueryConditional condicao = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(data.toString()).build());

        return tabela.query(condicao).items().stream().toList();
    }
}
