package de.fhzwickau.roomfinder.model.graph;

import de.fhzwickau.roomfinder.model.graph.node.LazyNode;
import de.fhzwickau.roomfinder.model.graph.node.Node;
import de.fhzwickau.roomfinder.model.graph.node.listener.LazyNodeListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Der Graph hält alle Knoten als {@link HashMap}. Somit können die Knoten sofort per ID angefordert werden.
 * Das spart Komplexität für Suchalgorithmen a.ä. Als Key wird die ID des Knotens verwendet.
 * Kanten (also Relationen) werden nur von den Knoten selbst gesichert.
 * @version 0.1.0
 * @since 0.1.0
 * @author Jonas Langner
 */
public class Graph extends HashMap<String, Node> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Collection<LazyNodeListener> listeners = new HashSet<>();

    public boolean registerListener(LazyNodeListener l) {
        return listeners.add(l);
    }

    private String getKey(Node node) {
        return node.getId();
    }

    /**
     * Fügt einen Knoten zum Graphen hinzu.
     * Dabei wird auch überprüft, ob ein Knoten vorher als {@link LazyNode} existiert hat.
     * Wenn dies so ist, werden alle registrierten {@link LazyNodeListener}s aufgerufen.
     * @param node Der Knoten der hinzugefügt werden soll.
     */
    public void add(Node node) {
        String key = getKey(node);

        if (containsKey(key)) {
            Node old = get(key);

            if ((old instanceof LazyNode) && !(node instanceof LazyNode)) {
                listeners.forEach(l -> l.onCompleteNodeLoaded((LazyNode) old, node));
            }

            remove(old);
        }

        put(node.getId(), node);
        node.setGraph(this);

        if (node instanceof LazyNodeListener) {
            registerListener((LazyNodeListener) node);
        }


    }

    /**
     * Fügt mehrere Knoten hinzu. Arbeitet damit mit {@link #add(de.fhzwickau.roomfinder.model.graph.node.Node)}.
     * @param nodes Die Knoten die hinzugefügt werden sollen.
     */
    public void addAll(Collection<Node> nodes) {
        nodes.forEach(this::add);
    }

    /**
     * Fügt die Knoten-Werte aus einer Map hinzu. Arbeit dabei mit {@link #addAll(Collection)}.
     * @param nodes Die Map, die die Knoten enthält, die hinzugefügt werden sollen.
     */
    public void addAll(Map<?, Node> nodes) {
        addAll(nodes.values());
    }

    public Graph combine(Graph withGraph) {
        Graph copy = (Graph) clone();

        copy.addAll(withGraph);

        return copy;
    }

    public void remove(Node node) {
        if (contains(node)) {
            for (String k : keySet()) {
                if (get(k).equals(node))
                    remove(k);

                if (!contains(node))
                    return;
            }
        }
    }

    public boolean contains(Node node) {
        return containsValue(node);
    }

}
