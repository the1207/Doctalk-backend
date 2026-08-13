package com.Doctalk.Doctalk_backend.utils;

import java.util.List;
import java.util.Locale;

public class VectorUtils {

    private VectorUtils() {}

    /**
     * Convertit une liste de floats en littéral pgvector, ex: [0.10000000,0.20000000]
     */
    public static String toPgVectorLiteral(List<Float> embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(String.format(Locale.US, "%.8f", embedding.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
}
