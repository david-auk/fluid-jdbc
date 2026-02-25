package io.github.david.auk.fluid.jdbc.dbtests.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class FetchResource {

    private FetchResource() {
        // utility class
    }

    public static String loadTrimmedResource(String resourcePath) {
        try (InputStream is = FetchResource.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Resource " + resourcePath + " not found");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource " + resourcePath, e);
        }
    }
}
