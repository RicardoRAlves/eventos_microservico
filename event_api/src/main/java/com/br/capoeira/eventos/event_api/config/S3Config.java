package com.br.capoeira.eventos.event_api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class S3Config {
    @Value("${aws.access_key_id}")
    private String awsId;
    @Value("${aws.secret_access_key}")
    private String awsKey;
    @Value("${s3.region}")
    private String region;
    @Bean
    public S3Client s3client(){
        AwsBasicCredentials credentials = AwsBasicCredentials.create(awsId, awsKey);
        return S3Client.builder().region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
