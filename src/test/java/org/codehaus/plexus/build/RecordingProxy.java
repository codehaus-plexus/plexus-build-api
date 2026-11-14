package org.codehaus.plexus.build;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A dynamic proxy that records each call and answers with a configured value, or with the zero value of the return
 * type.
 */
public final class RecordingProxy<T> {

    /** One recorded call. */
    public static final class Call {
        public final String method;
        public final List<Object> args;

        Call(String method, Object[] args) {
            this.method = method;
            this.args = args == null ? new ArrayList<>() : Arrays.asList(args);
        }

        @Override
        public String toString() {
            return method + args;
        }
    }

    private final List<Call> calls = new ArrayList<>();
    private final Map<String, Object> answers = new HashMap<>();
    private final T instance;

    private RecordingProxy(Class<T> type) {
        instance = type.cast(Proxy.newProxyInstance(
                RecordingProxy.class.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return objectMethod(proxy, method, args);
                    }
                    calls.add(new Call(method.getName(), args));
                    if (answers.containsKey(method.getName())) {
                        return answers.get(method.getName());
                    }
                    return zero(method.getReturnType());
                }));
    }

    public static <T> RecordingProxy<T> of(Class<T> type) {
        return new RecordingProxy<>(type);
    }

    public RecordingProxy<T> answer(String method, Object value) {
        answers.put(method, value);
        return this;
    }

    public T get() {
        return instance;
    }

    public List<Call> calls() {
        return calls;
    }

    public List<String> methods() {
        return calls.stream().map(call -> call.method).collect(Collectors.toList());
    }

    private static Object objectMethod(Object proxy, Method method, Object[] args) {
        switch (method.getName()) {
            case "equals":
                return proxy == args[0];
            case "hashCode":
                return System.identityHashCode(proxy);
            default:
                return "RecordingProxy@" + Integer.toHexString(System.identityHashCode(proxy));
        }
    }

    private static Object zero(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        return null;
    }
}
