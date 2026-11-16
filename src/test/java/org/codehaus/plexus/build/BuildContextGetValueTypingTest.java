package org.codehaus.plexus.build;

/**
 * Compile-time check that {@link BuildContext#getValue(String)} infers its result type from the assignment target, and
 * that an implementation declaring the pre-generic {@code Object getValue(String)} still compiles. It has no test
 * methods: it fails the build by not compiling.
 */
final class BuildContextGetValueTypingTest {

    private BuildContextGetValueTypingTest() {}

    static String inferredFromAssignment(BuildContext context) {
        return context.getValue("key");
    }

    static Object plainObjectStillWorks(BuildContext context) {
        return context.getValue("key");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    abstract static class PreGenericImplementation implements BuildContext {
        @Override
        public Object getValue(String key) {
            return null;
        }
    }
}
