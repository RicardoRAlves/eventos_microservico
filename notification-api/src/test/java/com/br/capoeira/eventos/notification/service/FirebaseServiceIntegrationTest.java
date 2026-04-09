package com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.FirestoreEmulatorContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {
        FirebaseService.class,
        FirebaseServiceIntegrationTest.FirestoreTestConfig.class
})
@Testcontainers
class FirebaseServiceIntegrationTest {

    static FirestoreEmulatorContainer firestoreEmulator =
            new FirestoreEmulatorContainer(
                    DockerImageName.parse("gcr.io/google.com/cloudsdktool/google-cloud-cli:441.0.0-emulators")
            );

    static {
        firestoreEmulator.start();
    }

    @Autowired
    private FirebaseService firebaseService;

    @Autowired
    private Firestore firestore;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

    @AfterEach
    void cleanUp() throws Exception {
        firestore.collection("events")
                .listDocuments()
                .forEach(doc -> {
                    try {
                        doc.delete().get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    void shouldAddEventOnFirestore() throws Exception {
        Event event = new Event();
        event.setTransactionId("tx-123");
        event.setTitle("Evento integração");

        firebaseService.addEvent(event);

        var snapshot = firestore.collection("events")
                .document("tx-123")
                .get()
                .get();

        assertTrue(snapshot.exists());
        assertEquals("tx-123", snapshot.getString("transactionId"));
        assertEquals("Evento integração", snapshot.getString("title"));
    }

    @TestConfiguration
    static class FirestoreTestConfig {

        @Bean
        @Primary
        Firestore firestore() {
            return FirestoreOptions.newBuilder()
                    .setProjectId("test-project")
                    .setEmulatorHost(firestoreEmulator.getEmulatorEndpoint())
                    .build()
                    .getService();
        }

        @Bean
        @Primary
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}