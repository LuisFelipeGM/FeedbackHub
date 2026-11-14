FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew shadowJar --no-daemon

FROM public.ecr.aws/lambda/java:21
COPY --from=build /app/build/libs/*-aws.jar ${LAMBDA_TASK_ROOT}/lib/
CMD ["org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest"]
