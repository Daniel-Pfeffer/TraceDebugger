package $$jwdebug;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class $$$TraceRecorder {
    private static final Stack<String> stack = new Stack<>();

    private static final StringBuilder builder = new StringBuilder();

    private static final String OUT = "trace.txt";

    private $$$TraceRecorder() {
        throw new IllegalStateException("Utility class");
    }

    private static Map<String, Object> localVariables = new HashMap<>();

    // https://shorturl.at/RHSl4
    private static final LocalsTable universe = new LocalsTable("<universe>", new HashMap<>(), null);

    private static LocalsTable currentLocals = universe;


    public static void enterMethod(String className, String methodName, Object... args) {
        var methodIdentifier = methodName + "(" + Arrays.stream(args).map(o -> o.getClass().getName()).collect(Collectors.joining(",")) + ")";
        var identifier = className + "." + methodIdentifier;
        builder.append("Entering ").append(identifier).append("\n");
        currentLocals = new LocalsTable(identifier, localVariables, currentLocals);
        localVariables = new HashMap<>();
        stack.push(identifier);

        for (int i = 0; i < args.length; i++) {
            var arg = args[i];
            var argHash = System.identityHashCode(arg);
            var argName = "arg" + i;
            localVariables.put(argName, arg);
            builder.append("Setting ").append(argName).append(" to ").append(argHash).append("\n");
        }
    }

    public static void exitMethod() {
        var x = stack.pop();
        builder.append("Exiting ").append(x).append("\n");
        currentLocals = currentLocals.prev;
        localVariables = currentLocals.locals;
    }

    public static Object setField(Object object, String fieldName, Object value) {
        // TODO: find better solution for finding unique identifiers for objects
        int objectHash = System.identityHashCode(object);
        int valueHash = System.identityHashCode(value);
        var identifier = object.getClass().getName() + "." + fieldName;
        builder.append("Setting ").append(identifier).append(" of ").append(objectHash).append(" to ").append(valueHash).append("\n");
        return value;
    }

    public static String setField(Object object, String fieldName, String value) {
        int objectHash = System.identityHashCode(object);
        int valueHash = System.identityHashCode(value);
        var identifier = object.getClass().getName() + "." + fieldName;
        builder.append("Setting ").append(identifier).append(" of ").append(objectHash).append(" to ").append(valueHash).append("\n");
        return value;
    }

    public static int setField(Object object, String fieldName, int value) {
        int objectHash = System.identityHashCode(object);
        var identifier = object.getClass().getName() + "." + fieldName;
        builder.append("Setting ").append(identifier).append(" of ").append(objectHash).append(" to ").append(value).append("\n");
        return value;
    }

    public static Object createObject(Object object) {
        int objectHash = System.identityHashCode(object);
        var identifier = object.getClass().getName();
        builder.append("Creating <").append(identifier).append("> with hash ").append(objectHash).append("\n");
        return object;
    }

    public static String createObject(String object) {
        var objectHash = System.identityHashCode(object);
        builder.append("Creating String <").append(objectHash).append("> with \"").append(object).append("\"\n");
        return object;
    }


    // set local variables all of them...
    public static Object setLocalVariable(String name, Object value) {
        int hashValue = System.identityHashCode(value);
        setLocalVariable(name, hashValue);
        return value;
    }

    public static int setLocalVariable(String name, int value) {
        localVariables.put(name, value);
        builder.append("Setting ").append(name).append(" to ").append(value).append("\n");
        localVariables.remove(name);
        return value;
    }

    public static double setLocalVariable(String name, double value) {
        builder.append("Setting ").append(name).append(" to ").append(value).append("\n");
        localVariables.remove(name);
        localVariables.put(name, value);
        return value;
    }

    public static float setLocalVariable(String name, float value) {
        builder.append("Setting ").append(name).append(" to ").append(value).append("\n");
        localVariables.remove(name);
        localVariables.put(name, value);
        return value;
    }

    public static boolean setLocalVariable(String name, boolean value) {
        builder.append("Setting ").append(name).append(" to ").append(value).append("\n");
        localVariables.remove(name);
        localVariables.put(name, value);
        return value;
    }

    public static byte setLocalVariable(String name, byte value) {
        builder.append("Setting ").append(name).append(" to ").append(value).append("\n");
        localVariables.remove(name);
        localVariables.put(name, value);
        return value;
    }

    public static long setLocalVariable(String name, long value) {
        builder.append("Setting ").append(name).append(" to ").append(value).append("\n");
        localVariables.remove(name);
        localVariables.put(name, value);
        return value;
    }

    public static char setLocalVariable(String name, char value) {
        builder.append("Setting ").append(name).append(" to ").append(value).append("\n");
        localVariables.remove(name);
        localVariables.put(name, value);
        return value;
    }

    public static short setLocalVariable(String name, short value) {
        builder.append("Setting ").append(name).append(" to ").append(value).append("\n");
        localVariables.remove(name);
        localVariables.put(name, value);
        return value;
    }

    // Return values from methods
    public static Object returnValue(Object value) {
        int hashValue = System.identityHashCode(value);
        builder.append("Returning value: ").append(hashValue).append("\n");
        return value;
    }

    public static int returnValue(int value) {
        builder.append("Returning value: ").append(value).append("\n");
        return value;
    }

    public static double returnValue(double value) {
        builder.append("Returning value: ").append(value).append("\n");
        return value;
    }

    public static float returnValue(float value) {
        builder.append("Returning value: ").append(value).append("\n");
        return value;
    }

    public static boolean returnValue(boolean value) {
        builder.append("Returning value: ").append(value).append("\n");
        return value;
    }

    public static byte returnValue(byte value) {
        builder.append("Returning value: ").append(value).append("\n");
        return value;
    }

    public static long returnValue(long value) {
        builder.append("Returning value: ").append(value).append("\n");
        return value;
    }

    public static char returnValue(char value) {
        builder.append("Returning value: ").append(value).append("\n");
        return value;
    }

    public static short returnValue(short value) {
        builder.append("Returning value: ").append(value).append("\n");
        return value;
    }

    public static boolean recordCondition(boolean condition, String conditionDescription) {
        builder.append("Condition: ").append(conditionDescription).append(" is ").append(condition).append("\n");
        return condition;
    }

    public static void verifyFinished() {

        try (var writer = new FileWriter(OUT)) {
            writer.write(builder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        if (!stack.isEmpty()) {
            throw new IllegalStateException("Stack is not empty");
        }
    }

    private File ensureFile() {
        var file = new File(OUT);
        if (!file.exists()) {
            try {
                Files.createFile(Path.of(OUT));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    // TODO: these should never be removed additionally level is bad, better name or something?
    private record LocalsTable(String name, Map<String, Object> locals, LocalsTable prev) {
    }
}
