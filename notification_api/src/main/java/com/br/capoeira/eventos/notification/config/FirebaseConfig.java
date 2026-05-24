package com.br.capoeira.eventos.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseConfig {

    @Value("${FIREBASE_CONFIG:}")
    private String firebaseConfig;

    @PostConstruct
    public void initializeFirebaseApp() {
        try {

            InputStream serviceAccount =
                    getFirebaseCredentials();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(serviceAccount)
                    )
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error initializing Firebase",
                    e
            );
        }
    }

    private InputStream getFirebaseCredentials()
            throws IOException {

        ClassPathResource resource =
                new ClassPathResource("firebase_config.json");

        if (resource.exists()) {
            return resource.getInputStream();
        }

        if (!firebaseConfig.isBlank()) {
            return new ByteArrayInputStream(
                    firebaseConfig.getBytes(StandardCharsets.UTF_8)
            );
        }

        throw new IOException(
                "Firebase credentials not found"
        );
    }

    @Bean
    public com.google.cloud.firestore.Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    @Bean
    public FirebaseMessaging getFirebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }
}
