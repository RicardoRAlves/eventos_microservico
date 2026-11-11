package com.br.capoeira.eventos.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.env.MockEnvironment;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebaseConfigTest {

    @Test
    void shouldInitializeNamedAppWithValidatedProject() throws Exception {
        String json = "{\"type\":\"service_account\",\"project_id\":\"events-capoeira-homologacao\"}";
        var resolver = mock(FirebaseCredentialResolver.class);
        when(resolver.resolve(
                eq(json),
                eq(true),
                any(),
                eq(false),
                eq("events-capoeira-homologacao")
        )).thenReturn(new FirebaseCredentialResolver.ResolvedCredentials(
                json.getBytes(StandardCharsets.UTF_8),
                "events-capoeira-homologacao",
                FirebaseCredentialResolver.CredentialSource.ENVIRONMENT
        ));

        var environment = new MockEnvironment()
                .withProperty("FIREBASE_CONFIG", json)
                .withProperty("firebase.expected-project-id", "events-capoeira-homologacao")
                .withProperty("firebase.allow-classpath-credentials", "false");
        var config = new FirebaseConfig(resolver);
        config.setEnvironment(environment);

        GoogleCredentials credentials = mock(GoogleCredentials.class);
        FirebaseApp app = mock(FirebaseApp.class);
        try (MockedStatic<GoogleCredentials> googleCredentials = mockStatic(GoogleCredentials.class);
             MockedStatic<FirebaseApp> firebaseApps = mockStatic(FirebaseApp.class)) {
            googleCredentials.when(() -> GoogleCredentials.fromStream(any(InputStream.class)))
                    .thenReturn(credentials);
            firebaseApps.when(FirebaseApp::getApps).thenReturn(List.of());
            firebaseApps.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class), eq("notification-api")))
                    .thenAnswer(invocation -> {
                        FirebaseOptions options = invocation.getArgument(0);
                        when(app.getOptions()).thenReturn(options);
                        return app;
                    });

            FirebaseApp initializedApp = config.firebaseApp();

            assertSame(app, initializedApp);
            assertEquals("events-capoeira-homologacao", initializedApp.getOptions().getProjectId());
            firebaseApps.verify(() -> FirebaseApp.initializeApp(
                    any(FirebaseOptions.class),
                    eq("notification-api")
            ));
        }
    }

    @Test
    void shouldCreateFirestoreFromValidatedFirebaseApp() {
        var resolver = mock(FirebaseCredentialResolver.class);
        var config = new FirebaseConfig(resolver);
        FirebaseApp firebaseApp = mock(FirebaseApp.class);
        Firestore firestore = mock(Firestore.class);

        try (MockedStatic<FirestoreClient> firestoreClient = mockStatic(FirestoreClient.class)) {
            firestoreClient.when(() -> FirestoreClient.getFirestore(firebaseApp)).thenReturn(firestore);

            assertSame(firestore, config.getFirestore(firebaseApp));

            firestoreClient.verify(() -> FirestoreClient.getFirestore(firebaseApp));
            verify(firebaseApp, org.mockito.Mockito.never()).getName();
        }
    }
}
