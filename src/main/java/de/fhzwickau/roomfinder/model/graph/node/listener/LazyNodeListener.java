package de.fhzwickau.roomfinder.model.graph.node.listener;

import de.fhzwickau.roomfinder.model.graph.node.LazyNode;
import de.fhzwickau.roomfinder.model.graph.node.Node;

public interface LazyNodeListener {

    void onCompleteNodeLoaded(LazyNode lazyNode, Node completeNode);

}
