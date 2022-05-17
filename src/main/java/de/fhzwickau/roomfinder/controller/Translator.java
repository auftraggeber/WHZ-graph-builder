package de.fhzwickau.roomfinder.controller;

import de.fhzwickau.graphbuilder.model.metadata.Metadata;
import de.fhzwickau.roomfinder.model.graph.Graph;
import de.fhzwickau.roomfinder.model.graph.edge.Edge;
import de.fhzwickau.roomfinder.model.graph.node.Node;

import java.lang.reflect.Field;

public class Translator {

    private de.fhzwickau.graphbuilder.model.graph.Graph old;
    private Graph graph = new Graph();

    public Translator(de.fhzwickau.graphbuilder.model.graph.Graph old) {
        this.old = old;

        translateNodes();
        translateEdges();
    }

    private void translateNodes() {

        for (de.fhzwickau.graphbuilder.model.graph.node.Node n: old.values()) {
            graph.add(translateNode(n));
        }

    }

    private Node translateNode(de.fhzwickau.graphbuilder.model.graph.node.Node old) {
        try {
            Node node = new Node();

            for (Field f : old.getClass().getDeclaredFields()) {
                if (f.isAnnotationPresent(Metadata.class)) {
                    Field field = node.getClass().getDeclaredField(f.getName());
                    f.setAccessible(true);
                    field.setAccessible(true);
                    field.set(node, f.get(old));
                    field.setAccessible(false);
                    f.setAccessible(false);
                }
            }

            return node;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void translateEdges() {
        for (de.fhzwickau.graphbuilder.model.graph.node.Node n : old.values()) {
            for (de.fhzwickau.graphbuilder.model.graph.edge.Edge e : n.getEdges()) {
                Node n1 = graph.get(n.getId());
                Node n2 = graph.get(e.getOther(n).getId());

                Edge edge = new Edge(n1, n2, e.getWeight());

                n1.addEdge(edge);
                n2.addEdge(edge);
            }
        }
    }

    public Graph get() {
        return graph;
    }

}
