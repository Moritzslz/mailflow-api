package de.flowsuite.mailflowapi.mail;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

class MailUtil {

    public static String readFile(ResourceLoader resourceLoader, String classpath) {
        Resource resource = resourceLoader.getResource(classpath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + classpath, e);
        }
    }
}
