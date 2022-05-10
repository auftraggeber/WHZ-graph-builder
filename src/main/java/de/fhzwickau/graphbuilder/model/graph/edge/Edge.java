package de.fhzwickau.graphbuilder.model.graph.edge;

import de.fhzwickau.graphbuilder.model.graph.node.LazyNode;
import de.fhzwickau.graphbuilder.model.graph.node.Node;
import de.fhzwickau.graphbuilder.model.metadata.Metadata;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Die Klasse realisiert die Verbindungen (Kanten) zwischen zwei Knoten.
 * @version 0.1.0
 * @since 0.1.0
 * @author Jonas Langner
 */
public class Edge implements Serializable {

    private static final long serialVersionUID = 1L;

    private Node[] nodes;

    @Metadata(description = "Das Gewicht der Kante (> 0)")
    private int weight;

    /**
     * Initialisiert ein neues Objekt. Die Reihenfolge der beiden ersten Parameter ist dabei zufällig.
     * Über {@link #getOther(Node)} wird die Reihenfolge aufgelöst.
     * @param n1 Knoten 1 der Kante.
     * @param n2 Knoten 2 der Kante.
     * @param weight Das Gewicht der Kante, welches für die Berechnung des optimalen Weges benötigt wird.
     * @throws IllegalStateException Die beiden Knoten müssen schon Teil des gleichen Graphen sein.
     * @throws IllegalArgumentException Falls die Knoten gleich sind (Ein Knoten darf keine Kante zu sich selbst aufbauen).
     * Falls sie das nicht sind, wird eine Exception geworfen.
     */
    public Edge(Node n1, Node n2, int weight) throws IllegalStateException {

        if (n1.getGraph() == null || n2.getGraph() == null || !n1.getGraph().contains(n2) || !n2.getGraph().contains(n1))
            throw new IllegalStateException("The nodes are not part of the same graph.");

        if (n1.equals(n2))
            throw new IllegalArgumentException("The nodes are equal.");

        nodes = new Node[]{
                n1,
                n2
        };
        this.weight = weight;
    }

    /**
     * Gibt den anderen Knoten aus. So erfährt ein Knoten ziemlich einfach, mit wem er überhaupt
     * in Verbindung steht.
     * @param that Der Knoten, der nicht zurückgegeben werden soll.
     * @return Der andere Knoten.
     * @throws IllegalArgumentException Wenn der als Parameter angegebene Knoten gar nicht Teil der Kante ist,
     * wird eine Exception geworfen.
     */
    public Node getOther(Node that) throws IllegalArgumentException {
        if (nodes[0].equals(that))
            return nodes[1];
        else if (nodes[1].equals(that))
            return nodes[0];

        throw new IllegalArgumentException("This node is not part of the edge. Therefore there is no other participant for this node.");
    }

    /**
     * Tauscht eine {@link de.fhzwickau.graphbuilder.model.graph.node.LazyNode} mit einer tatsächlichen Node aus.
     * @param node Die tatsächlich geladene Node.
     * @throws IllegalArgumentException Wird geworfen,
     * falls es sich nicht um eine geladene Node handelt oder die geladene Node nicht an der Kante anliegt.
     * @throws IllegalStateException Wenn es keine {@link de.fhzwickau.graphbuilder.model.graph.node.LazyNode} an dieser Kante gibt.
     */
    public void replaceLazyNodeWith(Node node) throws IllegalArgumentException, IllegalStateException {
        if (node instanceof LazyNode)
            throw new IllegalArgumentException("The new node is also a lazynode.");

        boolean lnFound = false;

        for (int i = 0; i < nodes.length; i++) {
            Node ln = nodes[i];

            if (ln instanceof LazyNode) {
                lnFound = true;

                if (ln.getId().equals(node.getId())) {
                    ln.removeEdgeOnlyForThisNode(this);

                    nodes[i] = node;

                    node.addEdge(this);

                    return;
                }
            }
        }

        if (lnFound)
            throw new IllegalStateException("There is no lazynode.");
        else
            throw new IllegalArgumentException("The new node is not part of this edge.");
    }

    /**
     * Zerstört die Kante und entfernt sie von den Knoten.
     */
    public void destroy() {
        Arrays.stream(nodes).forEach(n -> n.removeEdgeOnlyForThisNode(this));
    }


    public int getWeight() {
        return weight;
    }
}
