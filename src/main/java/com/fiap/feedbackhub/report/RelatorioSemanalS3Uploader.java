package com.fiap.feedbackhub.report;

import com.fiap.feedbackhub.domain.dto.RelatorioSemanalDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;

@Component
public class RelatorioSemanalS3Uploader {

    private final S3Client s3Client;
    private final String bucket;

    public RelatorioSemanalS3Uploader(S3Client s3Client, @Value("${aws.s3.bucket-relatorios}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public String salvar(RelatorioSemanalDTO relatorio, String conteudoCsv) {
        String chave = "relatorios/%s_%s.csv".formatted(relatorio.dataInicio(), relatorio.dataFim());

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(chave)
                        .contentType("text/csv")
                        .build(),
                RequestBody.fromString(conteudoCsv, StandardCharsets.UTF_8));

        return chave;
    }
}
