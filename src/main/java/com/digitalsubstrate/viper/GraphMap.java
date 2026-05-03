package com.digitalsubstrate.viper;

import java.util.HashMap;

public class GraphMap<T> {

    public Integer current = 0;
    public final HashMap<Integer, T> vertexToValue = new HashMap<>();
    public final HashMap<T, Integer> valueToVertex = new HashMap<>();

    public int size() {
        return vertexToValue.size();
    }

    public void insert(T value) {
        vertexToValue.put(current, value);
        valueToVertex.put(value, current);
        current += 1;
    }

    public Integer vertex(T value) {
        return valueToVertex.get(value);
    }

    public T value(Integer vertex) {
        return vertexToValue.get(vertex);
    }
}


