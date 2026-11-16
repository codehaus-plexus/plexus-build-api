package org.codehaus.plexus.build;

/**
 * Compile-time check that {@link BuildContext#getValue(String)} infers its result type from the assignment target, and
 * that an implementation declaring the pre-generic {@code Object getValue(String)} still compiles. It has no test
 * methods: it fails the build by not compiling.
 */
final class BuildContextGetValueTypingTest {

    private BuildContextGetValueTypingTest() {}

    static String inferredFromAssignment(BuildContext context) {
        String value = context.getValue("key");
        return value;
    }

    static Object plainObjectStillWorks(BuildContext context) {
        Object value = context.getValue("key");
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    abstract static class PreGenericImplementation implements BuildContext {
        @Override
        public Object getValue(String key) {
            return null;
        }
    }
}
