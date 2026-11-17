package com.fiap.feedbackhub.repository;

import com.fiap.feedbackhub.domain.entity.Aula;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.Optional;

@Repository
public class AulaRepository {

    private final DynamoDbTable<Aula> tabela;

    public AulaRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.tabela-aulas}")
            String tabelaAulas
    ) {
        this.tabela = enhancedClient.table(tabelaAulas, TableSchema.fromBean(Aula.class));
    }

    public Optional<Aula> buscarPorId(String id) {
        return Optional.ofNullable(tabela.getItem(Key.builder().partitionValue(id).build()));
    }

    public List<Aula> listarTodas() {
        return tabela.scan().items().stream().toList();
    }
}
