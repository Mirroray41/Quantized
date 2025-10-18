package net.zapp.quantized.core.utils.data;

import java.lang.reflect.Array;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class ArrayUtils {
    public static <T> boolean contains(T[] arr, T target) {
        if (arr == null || arr.length == 0) return false;
        for (T element : arr) {
            if (element == null && target == null) return true;
            if (element != null && element.equals(target)) return true;
        }
        return false;
    }

    public static <T> T[] removeAndResize(T[] arr, T toRemove) {
        if (arr == null || arr.length == 0) return arr;

        int index = -1;
        for (int i = 0; i < arr.length; i++) {
            if ((arr[i] == null && toRemove == null) || (arr[i] != null && arr[i].equals(toRemove))) {
                index = i;
                break;
            }
        }

        if (index == -1) return arr;

        T[] newArr = (T[]) Array.newInstance(arr.getClass().getComponentType(), arr.length - 1);

        if (index > 0)
            System.arraycopy(arr, 0, newArr, 0, index);
        if (index < arr.length - 1)
            System.arraycopy(arr, index + 1, newArr, index, arr.length - index - 1);

        return newArr;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] removeAll(T[] arr, T toRemove) {
        return Arrays.stream(arr)
                .filter(e -> !((e == null && toRemove == null) || (e != null && e.equals(toRemove))))
                .toArray(size -> (T[]) Array.newInstance(arr.getClass().getComponentType(), size));
    }
}
