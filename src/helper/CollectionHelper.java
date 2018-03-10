package helper;

import java.util.List;

public final class CollectionHelper {
    public static float[] toPrimitive(final List<Float> floatList) {
        if (floatList == null) {
            return null;
        }

        int size = floatList.size();
        final float[] result = new float[size];
        for (int i = 0; i < size; i++) {
            result[i] = floatList.get(i).floatValue();
        }
        return result;
    }
}
