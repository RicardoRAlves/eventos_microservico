package com.br.capoeira.eventos.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class FirebaseConfig implements EnvironmentAware {

    static final String FIREBASE_APP_NAME = "notification-api";

    private final FirebaseCredentialResolver credentialResolver;
    private Environment environment;

    public FirebaseConfig(FirebaseCredentialResolver credentialResolver) {
        this.credentialResolver = credentialResolver;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Bean(destroyMethod = "delete")
    public FirebaseApp firebaseApp() {
        boolean environmentCredentialsPresent = environment.containsProperty("FIREBASE_CONFIG");
        String environmentCredentials = environment.getProperty("FIREBASE_CONFIG");
        boolean classpathFallbackAllowed = environment.getProperty(
                "firebase.allow-classpath-credentials",
                Boolean.class,
                false
        );
        String expectedProjectId = environment.getProperty("firebase.expected-project-id");

        FirebaseCredentialResolver.ResolvedCredentials resolvedCredentials = credentialResolver.resolve(
                environmentCredentials,
                environmentCredentialsPresent,
                new ClassPathResource("firebase_config.json"),
                classpathFallbackAllowed,
                expectedProjectId
        );

        ensureAppDoesNotAlreadyExist();

        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(resolvedCredentials.json())
            );
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setProjectId(resolvedCredentials.projectId())
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options, FIREBASE_APP_NAME);
            String effectiveProjectId = app.getOptions().getProjectId();
            if (!resolvedCredentials.projectId().equals(effectiveProjectId)) {
                app.delete();
                throw new IllegalStateException(
                        "Firebase Admin SDK initialized with unexpected project ID. expected="
                                + resolvedCredentials.projectId() + ", effective=" + effectiveProjectId
                );
            }

            log.info(
                    "Firebase initialized successfully. profiles={}, credentialsSource={}, projectId={}, expectedProjectId={}",
                    activeProfiles(),
                    resolvedCredentials.source(),
                    effectiveProjectId,
                    normalizedExpectedProjectId(expectedProjectId)
            );
            return app;
        } catch (Exception exception) {
            log.error(
                    "Firebase initialization failed. profiles={}, credentialsSource={}, projectId={}, expectedProjectId={}",
                    activeProfiles(),
                    resolvedCredentials.source(),
                    resolvedCredentials.projectId(),
                    normalizedExpectedProjectId(expectedProjectId)
            );
            throw new IllegalStateException("Error initializing Firebase", exception);
        }
    }

    @Bean
    public Firestore getFirestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
    public FirebaseMessaging getFirebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private void ensureAppDoesNotAlreadyExist() {
        boolean appAlreadyExists = FirebaseApp.getApps().stream()
                .anyMatch(app -> FIREBASE_APP_NAME.equals(app.getName()));
        if (appAlreadyExists) {
            throw new IllegalStateException(
                    "FirebaseApp '" + FIREBASE_APP_NAME + "' is already initialized; refusing to reuse it"
            );
        }
    }

    private String activeProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length == 0 ? "default" : Arrays.toString(activeProfiles);
    }

    private String normalizedExpectedProjectId(String expectedProjectId) {
        return expectedProjectId == null || expectedProjectId.isBlank()
                ? "not-configured"
                : expectedProjectId.trim();
    }
}
