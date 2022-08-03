package io.intino.master.model;

import io.intino.magritte.framework.Graph;

public class MasterGraph extends io.intino.master.model.AbstractGraph {

	public MasterGraph(Graph graph) {
		super(graph);
	}

	public MasterGraph(io.intino.magritte.framework.Graph graph, MasterGraph wrapper) {
	    super(graph, wrapper);
	}
}