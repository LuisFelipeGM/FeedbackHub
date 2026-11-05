package com.fiap.feedbackhub.repository;

import com.fiap.feedbackhub.config.AbstractIT;
import com.fiap.feedbackhub.domain.entity.Avaliacao;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@SpringBootTest
public abstract class BaseRepositoryIT extends AbstractIT {

    @Autowired
    protected AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${aws.dynamodb.tabela-avaliacoes}")
    private String tabelaAvaliacoes;

    @BeforeEach
    void limparTabelaAvaliacoes() {
        DynamoDbTable<Avaliacao> tabela = enhancedClient.table(tabelaAvaliacoes, TableSchema.fromBean(Avaliacao.class));
        tabela.scan().items().forEach(tabela::deleteItem);
    }
}
