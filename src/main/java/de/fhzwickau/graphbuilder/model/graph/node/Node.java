package de.fhzwickau.graphbuilder.model.graph.node;

import de.fhzwickau.graphbuilder.model.graph.Graph;
import de.fhzwickau.graphbuilder.model.graph.edge.Edge;
import de.fhzwickau.graphbuilder.model.metadata.Metadata;

import java.io.Serializable;
import java.util.*;

/**
 * Die Knoten unseres Graphen.
 * Sie halten Metadaten für den Raumfindungsalgorithmus & das UI, die mit {@link Metadata} gekennzeichnet sind.
 * Zudem werden die {@link Edge}s zu anderen Knoten hier gehalten.
 * Die Knoten müssen schlussendlich noch einem {@link Graph}en zugeordnet werden.
 * @version 0.1.0
 * @since 0.1.0
 * @author Jonas Langner
 */
public class Node implements Serializable {

    private static final String NULL_STRING = "";

    private static final long serialVersionUID = 1L;

    private Set<Edge> edges;
    private Graph graph;

    @Metadata(description = "Die ID des Knotens: Name des Raums in Kleinbuchstaben ohne Leer- und Sonderzeichen.")
    protected String id;

    @Metadata(nullable = true, description = "Der Name des Knotens, der dem Nutzer angezeigt werden soll. Leer lassen, wenn kein Name angezeigt werden soll.")
    protected String displayName;

    @Metadata(nullable = true, description = "Die Ressource, auf dem der Knoten zu sehen ist. Wenn der Knoten nicht eingezeichnet werden soll frei lassen.")
    protected String ressource;

    @Metadata(nullable = true, description = "Die X-Koordinate der Position des Knotens innerhalb der Resource. Wenn der Knoten nicht eingezeichnet werden soll auf -1 setzen.")
    protected int positionX;

    @Metadata(nullable = true, description = "Die Y-Koordinate der Position des Knotens innerhalb der Resource. Wenn der Knoten nicht eingezeichnet werden soll auf -1 setzen.")
    protected int positionY;

    @Metadata(description = "Gibt an, ob der Knoten als Ziel ausgewählt werden darf.")
    protected boolean asTarget;

    public Node() {
        id = UUID.randomUUID().toString(); // Verhindern eines nullpointers
        edges = new HashSet<>();

        setDefaults();
    }

    protected void setDefaults() {
        positionX = -1;
        positionY = -1;
        asTarget = false;
    }

    public void setGraph(Graph graph) {
        if (!graph.containsValue(this))
            throw new IllegalStateException("The graph does not contain this node. Use Graph::add(Node) instead!");

        this.graph = graph;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return hasDisplayName() ? displayName : NULL_STRING;
    }

    public String getRessource() {
        return hasRessource() ? ressource : NULL_STRING;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public Graph getGraph() {
        return graph;
    }

    public boolean asTarget() {
        return asTarget;
    }

    public boolean hasDisplayName() {
        return displayName != null;
    }

    public boolean hasRessource() {
        return ressource != null;
    }

    public boolean hasPosition() {
        return positionX >= 0 && positionY >= 0;
    }

    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    public boolean addEdge(Edge edge) {
        for (Edge e : edges) {
            if (e.getOther(this).equals(edge.getOther(this))) {
                edges.remove(e);
                e.getOther(this).edges.remove(e);
            }
        }

        return edges.add(edge);
    }

    /**
     * Entfernt die Kante nur von diesem Knoten.
     * Falls die Kante allgemein gelöscht werden soll, {@link Edge#destroy()} verwenden.
     * @param edge Die Kante, die entfernt werden soll.
     * @return Gibt an, ob die Kante entfernt werden konnte.
     */
    public boolean removeEdgeOnlyForThisNode(Edge edge) {
        return edges.remove(edge);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
