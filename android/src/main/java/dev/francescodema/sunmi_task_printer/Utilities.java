package dev.francescodema.sunmi_task_printer;

import java.util.ArrayList;

/**
 * Provides static helper methods to perform basic conversion logic bridging Flutter Map types to Java Primitives.
 */
public class Utilities {

    /**
     * Converts a boxed Integer ArrayList into a primitive int array.
     *
     * @param list The ArrayList of Integers to unpack.
     * @return A primitive int array, or an empty array if null.
     */
    public static int[] arrayListToIntList(ArrayList<Integer> list) {
        if (list == null) return new int[0];
        final int[] ints = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ints[i] = list.get(i);
        }
        return ints;
    }

    /**
     * Standardizes a String ArrayList into a native primitive String array format.
     *
     * @param list The ArrayList of Strings.
     * @return A primitive String array, or an empty array if null.
     */
    public static String[] arrayListToString(ArrayList<String> list) {
        if (list == null) return new String[0];
        final String[] strings = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            strings[i] = list.get(i);
        }
        return strings;
    }
}