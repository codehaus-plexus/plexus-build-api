package org.codehaus.plexus.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every component in the Sisu index ends up on every realm that carries this jar, and Guice rejects the whole injector
 * when one of them has more than one scope annotation (#127). Guice counts an annotation as a scope when its type is
 * annotated with {@code javax.inject.Scope} or {@code com.google.inject.ScopeAnnotation}; this test applies the same
 * rule.
 */
class ComponentScopeTest {

    private static final String INDEX = "META-INF/sisu/javax.inject.Named";

    private static final List<String> SCOPE_MARKERS =
            Arrays.asList("javax.inject.Scope", "jakarta.inject.Scope", "com.google.inject.ScopeAnnotation");

    @Test
    void indexListsTheComponents() throws Exception {
        List<String> components = indexedComponents();

        assertTrue(components.contains(DefaultBuildContext.class.getName()), components::toString);
        assertTrue(components.contains("org.codehaus.plexus.build.progress.DefaultProgress"), components::toString);
    }

    @Test
    void everyIndexedComponentHasAtMostOneScope() throws Exception {
        List<String> components = indexedComponents();
        assertFalse(components.isEmpty());

        for (String name : components) {
            Class<?> type = Class.forName(name);
            List<String> scopes = Arrays.stream(type.getAnnotations())
                    .map(Annotation::annotationType)
                    .filter(ComponentScopeTest::isScope)
                    .map(Class::getName)
                    .collect(Collectors.toList());
            assertTrue(scopes.size() <= 1, () -> name + " has more than one scope annotation: " + scopes);
        }
    }

    @Test
    void defaultProgressIsScopedToTheMojoExecution() throws Exception {
        Class<?> type = Class.forName("org.codehaus.plexus.build.progress.DefaultProgress");
        List<String> scopes = Arrays.stream(type.getAnnotations())
                .map(Annotation::annotationType)
                .filter(ComponentScopeTest::isScope)
                .map(Class::getName)
                .collect(Collectors.toList());

        assertEquals(Arrays.asList("org.apache.maven.execution.scope.MojoExecutionScoped"), scopes);
    }

    private static boolean isScope(Class<? extends Annotation> annotationType) {
        return Arrays.stream(annotationType.getAnnotations())
                .anyMatch(meta -> SCOPE_MARKERS.contains(meta.annotationType().getName()));
    }

    /** Reads the index from this module's own output, not from a jar that happens to be on the test class path. */
    private static List<String> indexedComponents() throws IOException {
        List<String> components = new ArrayList<>();
        String ownLocation = DefaultBuildContext.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toString();
        Enumeration<URL> indexes = ComponentScopeTest.class.getClassLoader().getResources(INDEX);
        while (indexes.hasMoreElements()) {
            URL index = indexes.nextElement();
            if (!index.toString().startsWith(ownLocation)) {
                continue;
            }
            try (InputStream in = index.openStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .forEach(components::add);
            }
        }
        return components;
    }
}
