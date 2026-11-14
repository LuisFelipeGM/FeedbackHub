#!/bin/bash
set -e

echo "Criando recursos no LocalStack..."

awslocal dynamodb create-table \
  --table-name Avaliacoes \
  --attribute-definitions \
      AttributeName=data,AttributeType=S \
      AttributeName=id,AttributeType=S \
  --key-schema \
      AttributeName=data,KeyType=HASH \
      AttributeName=id,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST

awslocal sqs create-queue --queue-name dlq-notificacoes-criticas

DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url http://localhost:4566/000000000000/dlq-notificacoes-criticas \
  --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

awslocal sqs create-queue \
  --queue-name sqs-notificacoes-criticas \
  --attributes "{\"RedrivePolicy\":\"{\\\"deadLetterTargetArn\\\":\\\"${DLQ_ARN}\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}\"}"

awslocal s3 mb s3://feedbackhub-relatorios

awslocal ses verify-email-identity --email-address admin@feedbackhub.local

echo "Recursos criados: tabela Avaliacoes, filas de notificacao, bucket de relatorios, identidade SES."
