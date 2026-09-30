package org.jdriven;

import java.util.List;

public class ExampleNullPropagates {

    public ExampleNullPropagates(@NullPropagates String ignoredForConstructor) {
        // impl
    }

    public static void main(String[] args) {
        System.out.println("upper(\"max\") = " + upper("max"));
        System.out.println("upper(null) = " + upper(null));

        System.out.println("concat(\"Hello \", \"world\") = " + concat("Hello ", "world"));
        System.out.println("concat(null, \"world\") = " + concat(null, "world"));

        System.out.println("describe(null, \"value\") = " + describe(null, "value"));
        System.out.println("describe(\"context\", null) = " + describe("context", null));

        System.out.println("identity(\"value\") = " + identity("value"));
        System.out.println("identity(null) = " + identity(null));

        System.out.println("firstLength(List.of(\"abc\")) = " + firstLength(List.of("abc")));
        System.out.println("firstLength(null) = " + firstLength(null));

        System.out.println("doubled(21) = " + doubled(21));

        System.out.println("add(21, 22) = " + add(21, 22));
        System.out.println("add(21, null) = " + add(21, null));

        System.out.print("log(null) = ");
        log(null);
    }

    public static String upper(@NullPropagates String value) {
        return value.toUpperCase();
    }

    public static String concat(@NullPropagates String left, @NullPropagates String right) {
        return left + right;
    }

    public static String describe(String context, @NullPropagates Object value) {
        return context + ": " + value;
    }

    public static <T> T identity(@NullPropagates T value) {
        return value;
    }

    public static Integer firstLength(@NullPropagates List<String> values) {
        return values.getFirst().length();
    }

    public static int doubled(@NullPropagates int ignoredForPrimitive) {
        return ignoredForPrimitive * 2;
    }

    public static Integer add(@NullPropagates int ignoredForPrimitive, @NullPropagates Integer value) {
        return ignoredForPrimitive + value;
    }

    public static void log(@NullPropagates String ignoredForMethodsThatReturnVoid) {
        System.out.println(ignoredForMethodsThatReturnVoid);
    }
}
