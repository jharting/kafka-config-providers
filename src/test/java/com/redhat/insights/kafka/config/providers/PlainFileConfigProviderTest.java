package com.redhat.insights.kafka.config.providers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.common.config.ConfigData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PlainFileConfigProviderTest {

    private PlainFileConfigProvider instance;

    @Before
    public void init() {
        instance = new PlainFileConfigProvider();
        instance.configure(Collections.emptyMap());
    }

    @After
    public void close() throws IOException {
        instance.close();
    }

    @Test
    public void testReadingSingleFile() throws IOException {
        final String value = "secret";
        final Path path = Files.createTempFile("kafka-config-providers", ".txt");
        Files.write(path, value.getBytes());

        final ConfigData result = instance.get("", Collections.singleton(path.toAbsolutePath().toString()));
        assertEquals(value, result.data().get(path.toAbsolutePath().toString()));
    }

    @Test
    public void testReadingMultipleFiles() throws IOException {
        final Map<String, String> data = Stream.of("a", "b", "c").collect(Collectors.toMap(value -> {
            try {
                final Path path = Files.createTempFile("kafka-config-providers", ".txt");
                Files.write(path, value.getBytes());
                return path.toAbsolutePath().toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, value -> value));

        final ConfigData result = instance.get("", data.keySet());
        for (Entry<String, String> entry: data.entrySet()) {
            assertEquals(entry.getValue(), result.data().get(entry.getKey()));
        }
    }

    @Test
    public void testExceptionThrownOnPathPlusKey() throws IOException {
        assertThrows(IllegalArgumentException.class, () -> instance.get("path", Collections.singleton("key")));
    }
}
