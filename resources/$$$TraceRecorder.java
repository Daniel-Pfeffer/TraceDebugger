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

        /*for (int i = 0; i < args.length; i++) {
            var arg = args[i];
            var argName = "arg" + i;
            builder.append("Setting ").append(argName).append(" to ");
            switch (arg) {
                case Integer realArg -> builder.append(realArg);
                case String realArg -> builder.append(realArg);
                case Double realArg -> builder.append(realArg);
                case Float realArg -> builder.append(realArg);
                case Boolean realArg -> builder.append(realArg);
                case Byte realArg -> builder.append(realArg);
                case Long realArg -> builder.append(realArg);
                case Character realArg -> builder.append(realArg);
                case Short realArg -> builder.append(realArg);
                case null, default -> builder.append(System.identityHashCode(arg));
            }

            localVariables.put(argName, arg);
            builder.append("\n");
        }*/
    }

    public static void exitMethod() {
        var x = stack.pop();
        builder.append("Exiting ").append(x).append("\n");
        currentLocals = currentLocals.prev;
        localVariables = currentLocals.locals;
    }

    public static <T> T setField(Object object, String fieldName, T value) {
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

    public static <T> T createObject(T object) {
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

    // set static variables
    public static <T> T setStaticVariable(String className, String fieldName, T value) {
        int hashValue = System.identityHashCode(value);
        builder.append("Setting static field ").append(className).append(".").append(fieldName).append(" to ").append(hashValue).append("\n");
        return value;
    }

    public static int setStaticVariable(String className, String fieldName, int value) {
        builder.append("Setting static field ").append(className).append(".").append(fieldName).append(" to ").append(value).append("\n");
        return value;
    }

    public static double setStaticVariable(String className, String fieldName, double value) {
        builder.append("Setting static field ").append(className).append(".").append(fieldName).append(" to ").append(value).append("\n");
        return value;
    }

    public static float setStaticVariable(String className, String fieldName, float value) {
        builder.append("Setting static field ").append(className).append(".").append(fieldName).append(" to ").append(value).append("\n");
        return value;
    }

    public static boolean setStaticVariable(String className, String fieldName, boolean value) {
        builder.append("Setting static field ").append(className).append(".").append(fieldName).append(" to ").append(value).append("\n");
        return value;
    }

    public static byte setStaticVariable(String className, String fieldName, byte value) {
        builder.append("Setting static field ").append(className).append(".").append(fieldName).append(" to ").append(value).append("\n");
        return value;
    }

    public static long setStaticVariable(String className, String fieldName, long value) {
        builder.append("Setting static field ").append(className).append(".").append(fieldName).append(" to ").append(value).append("\n");
        return value;
    }

    public static char setStaticVariable(String className, String fieldName, char value) {
        builder.append("Setting static field ").append(className).append(".").append(fieldName).append(" to ").append(value).append("\n");
        return value;
    }

    public static short setStaticVariable(String className, String fieldName, short value) {
        builder.append("Setting static field ").append(className).append(".").append(fieldName).append(" to ").append(value).append("\n");
        return value;
    }

    // Return values from methods
    public static <T> T returnValue(T value) {
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

    public static <T> T recordNewObjectCreation(T object) {
        int objectHash = System.identityHashCode(object);
        var identifier = object.getClass().getName();
        builder.append("Creating new object <").append(identifier).append("> with hash ").append(objectHash).append("\n");
        return object;
    }

    // increment local variables
    public static int unaryLocal(String name, int value, int newValue) {
        setLocalVariable(name, newValue);
        return value;
    }

    public static double unaryLocal(String name, double value, double newValue) {
        setLocalVariable(name, newValue);
        return value;
    }

    public static float unaryLocal(String name, float value, float newValue) {
        setLocalVariable(name, newValue);
        return value;
    }

    public static boolean unaryLocal(String name, boolean value, boolean newValue) {
        setLocalVariable(name, newValue);
        return value;
    }

    public static byte unaryLocal(String name, byte value, byte newValue) {
        setLocalVariable(name, newValue);
        return value;
    }

    public static long unaryLocal(String name, long value, long newValue) {
        setLocalVariable(name, newValue);
        return value;
    }

    public static char unaryLocal(String name, char value, char newValue) {
        setLocalVariable(name, newValue);
        return value;
    }

    public static short unaryLocal(String name, short value, short newValue) {
        setLocalVariable(name, newValue);
        return value;
    }

    // increment static variables

    public static int unaryStatic(String className, String fieldName, int value, int newValue) {
        setStaticVariable(className, fieldName, newValue);
        return value;
    }

    public static double unaryStatic(String className, String fieldName, double value, double newValue) {
        setStaticVariable(className, fieldName, newValue);
        return value;
    }

    public static float unaryStatic(String className, String fieldName, float value, float newValue) {
        setStaticVariable(className, fieldName, newValue);
        return value;
    }

    public static boolean unaryStatic(String className, String fieldName, boolean value, boolean newValue) {
        setStaticVariable(className, fieldName, newValue);
        return value;
    }

    public static byte unaryStatic(String className, String fieldName, byte value, byte newValue) {
        setStaticVariable(className, fieldName, newValue);
        return value;
    }

    public static long unaryStatic(String className, String fieldName, long value, long newValue) {
        setStaticVariable(className, fieldName, newValue);
        return value;
    }

    public static char unaryStatic(String className, String fieldName, char value, char newValue) {
        setStaticVariable(className, fieldName, newValue);
        return value;
    }

    public static short unaryStatic(String className, String fieldName, short value, short newValue) {
        setStaticVariable(className, fieldName, newValue);
        return value;
    }


    // Unary field
    public static int unaryField(Object object, String fieldName, int value, int newValue) {
        setField(object, fieldName, newValue);
        return value;
    }

    public static double unaryField(Object object, String fieldName, double value, double newValue) {
        setField(object, fieldName, newValue);
        return value;
    }

    public static float unaryField(Object object, String fieldName, float value, float newValue) {
        setField(object, fieldName, newValue);
        return value;
    }

    public static boolean unaryField(Object object, String fieldName, boolean value, boolean newValue) {
        setField(object, fieldName, newValue);
        return value;
    }

    public static byte unaryField(Object object, String fieldName, byte value, byte newValue) {
        setField(object, fieldName, newValue);
        return value;
    }

    public static long unaryField(Object object, String fieldName, long value, long newValue) {
        setField(object, fieldName, newValue);
        return value;
    }

    public static char unaryField(Object object, String fieldName, char value, char newValue) {
        setField(object, fieldName, newValue);
        return value;
    }

    public static short unaryField(Object object, String fieldName, short value, short newValue) {
        setField(object, fieldName, newValue);
        return value;
    }


    public static void verifyFinished() {

        try (var writer = new FileWriter(OUT)) {
            writer.write(builder.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
