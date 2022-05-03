package de.fhzwickau.graphbuilder.model.graph.node.listener;

import de.fhzwickau.graphbuilder.model.graph.node.LazyNode;
import de.fhzwickau.graphbuilder.model.graph.node.Node;

public interface LazyNodeListener {

    void onCompleteNodeLoaded(LazyNode lazyNode, Node completeNode);

}
