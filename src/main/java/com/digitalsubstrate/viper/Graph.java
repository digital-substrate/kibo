package com.digitalsubstrate.viper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public final class Graph {

    private final ArrayList<LinkedList<Integer>> adjacencyList;

    public Graph(int vertexCount) {
        adjacencyList = new ArrayList<>();
        for (int vertex = 0; vertex < vertexCount; ++vertex)
            adjacencyList.add(new LinkedList<>());
    }

    public void addEdge(int v, int w) {
        adjacencyList.get(v).add(w);
    }

    private void sort(int vertex, ArrayList<Boolean> visited, Stack<Integer> stack) {
        visited.set(vertex, true);
        for (var v : adjacencyList.get(vertex)) {
            if (!visited.get(v))
                sort(v, visited, stack);
        }
        stack.push(vertex);
    }

    public ArrayList<Integer> topologicalSort() {
        final var vertexCount = adjacencyList.size();
        final var visited = new ArrayList<Boolean>();
        for (int vertex = 0; vertex < vertexCount; vertex++)
            visited.add(false);

        final var stack = new Stack<Integer>();
        for (int vertex = 0; vertex < vertexCount; vertex++)
            if (!visited.get(vertex))
                sort(vertex, visited, stack);

        final var result = new ArrayList<Integer>();
        while (!stack.empty())
            result.add(stack.pop());

        return result;
    }
}
