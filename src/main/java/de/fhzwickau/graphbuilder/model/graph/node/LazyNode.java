package de.fhzwickau.graphbuilder.model.graph.node;

import de.fhzwickau.graphbuilder.model.graph.node.listener.LazyNodeListener;

import java.io.Serializable;

/**
 * Dieser besondere Knoten kennt nur die ID des eigentlichen Knotens. Wenn der eigentliche Knoten geladen wurde,
 * wird dieser Knoten mit dem tatsächlichen Knoten ersetzt.
 * Dies ist notwendig, da so der Graph in mehreren kleineren Dateien gesichert werden kann,
 * die dann nach und nach in den Hauptspeicher geladen werden können.
 * Somit können trotzdem schon die Kanten der Knoten, die tatsächlich geladen sind, erstellt werden und mit diesen
 * Platzhalterobjekten gefüllt werden.
 * @version 0.1.0
 * @since 0.1.0
 * @author Jonas Langner
 */
public class LazyNode extends Node implements Serializable, LazyNodeListener {

    private static final long serialVersionUID = 1L;

    public LazyNode(String id) {
        super();
        super.id = id;
    }


    @Override
    public void onCompleteNodeLoaded(LazyNode lazyNode, Node completeNode) {

        if (this.equals(lazyNode))
            getEdges().forEach(e -> e.replaceLazyNodeWith(completeNode));
    }
}

