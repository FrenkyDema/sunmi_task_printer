package dev.francescodema.sunmi_task_printer;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * The type Utilities.
 */
public class Utilities {
    /**
     * Array list to int list int [ ].
     *
     * @param list the list
     * @return the int [ ]
     */
    public static int[] arrayListToIntList(ArrayList<Integer> list) {

        final int[] ints = new int[list.size()];

        for (int i = 0; i < list.size(); i++) {
            ints[i] = list.get(i);
        }

        return ints;
    }

    /**
     * Array list to string string [ ].
     *
     * @param list the list
     * @return the string [ ]
     */
    public static String[] arrayListToString(ArrayList<String> list) {

        final String[] strings = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            strings[i] = list.get(i);
        }

        return strings;
    }

    /**
     * Scale down bitmap bitmap.
     *
     * @param realImage    the real image
     * @param maxImageSize the max image size
     * @param filter       the filter
     * @return the bitmap
     */
    public static Bitmap scaleDownBitmap(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }
}