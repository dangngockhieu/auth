package com.example.auth;

import io.github.cdimascio.dotenv.Dotenv;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EnvLoader {
    public static void loadEnv() {
        Dotenv dotenv = loadDotenv();
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }

    private static Dotenv loadDotenv() {
        Path envFile = findEnvFile();
        if (envFile == null) {
            return Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        }

        return Dotenv.configure()
                .directory(envFile.getParent().toString())
                .filename(envFile.getFileName().toString())
                .ignoreIfMissing()
                .load();
    }

    private static Path findEnvFile() {
        Path workingDirectoryEnv = Path.of("").toAbsolutePath().resolve(".env");
        if (Files.exists(workingDirectoryEnv)) {
            return workingDirectoryEnv;
        }

        try {
            Path location = Path.of(EnvLoader.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());

            for (Path current = location; current != null; current = current.getParent()) {
                Path candidate = current.resolve(".env");
                if (Files.exists(candidate)) {
                    return candidate;
                }
            }
        } catch (URISyntaxException ignored) {
            return null;
        }

        return null;
    }
}
