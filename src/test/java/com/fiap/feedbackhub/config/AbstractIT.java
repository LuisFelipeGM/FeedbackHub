package com.fiap.feedbackhub.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;

public class AbstractIT {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        LocalStackContainer localstack = LocalStackContainerSingleton.getInstance();

        registry.add("aws.region", localstack::getRegion);
        registry.add("aws.endpoint-override", () -> localstack.getEndpointOverride(DYNAMODB).toString());
    }
}
