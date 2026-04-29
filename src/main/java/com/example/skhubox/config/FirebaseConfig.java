package com.example.skhubox.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
public class FirebaseConfig {

    private static final String FIREBASE_CREDENTIALS_PATH = "FIREBASE_CREDENTIALS_PATH";

    @PostConstruct
    public void init() {
        String credentialsPath = System.getenv(FIREBASE_CREDENTIALS_PATH);
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.warn("Firebase initialization skipped: {} is not set", FIREBASE_CREDENTIALS_PATH);
            return;
        }

        try {
            Path path = Path.of(credentialsPath);
            try (InputStream inputStream = Files.newInputStream(path)) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(inputStream))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase application has been initialized");
                }
            }
        } catch (IOException e) {
            log.error("Firebase initialization error: {}", e.getMessage());
        }
    }
}
