package com.fiap.feedbackhub.service;

import com.fiap.feedbackhub.config.AbstractIT;
import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.repository.AvaliacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RelatorioSemanalServiceIT extends AbstractIT {

    @Autowired
    private RelatorioSemanalService relatorioSemanalService;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.dynamodb.tabela-avaliacoes}")
    private String tabelaAvaliacoes;

    @Value("${aws.s3.bucket-relatorios}")
    private String bucketRelatorios;

    @BeforeEach
    void limparRecursos() {
        DynamoDbTable<Avaliacao> tabela = enhancedClient.table(tabelaAvaliacoes, TableSchema.fromBean(Avaliacao.class));
        tabela.scan().items().forEach(tabela::deleteItem);

        s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketRelatorios).build())
                .contents()
                .forEach(objeto -> s3Client.deleteObject(builder -> builder.bucket(bucketRelatorios).key(objeto.key())));
    }

    @Test
    void deveGerarRelatorioComAsAvaliacoesDoPeriodoESalvarNoS3() {
        LocalDate hoje = LocalDate.now();

        avaliacaoRepository.salvar(Avaliacao.novo("Aula dentro do período", 7,
                LocalDateTime.of(hoje.minusDays(3), LocalTime.of(10, 0))));
        avaliacaoRepository.salvar(Avaliacao.novo("Aula fora do período", 7,
                LocalDateTime.of(hoje, LocalTime.of(10, 0))));

        relatorioSemanalService.gerar();

        LocalDate dataFim = hoje.minusDays(1);
        LocalDate dataInicio = dataFim.minusDays(6);
        String chaveEsperada = "relatorios/%s_%s.csv".formatted(dataInicio, dataFim);

        S3Object objetoSalvo = s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketRelatorios).build())
                .contents().stream()
                .filter(objeto -> objeto.key().equals(chaveEsperada))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Relatório não encontrado no S3: " + chaveEsperada));

        ResponseBytes<GetObjectResponse> conteudo = s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucketRelatorios).key(objetoSalvo.key()).build());

        String csv = conteudo.asUtf8String();
        assertThat(csv)
                .contains("Aula dentro do período")
                .doesNotContain("Aula fora do período");
    }
}
