package projects.ghs.aux;

import projects.ghs.nodes.edges.WeightedBidirectionalEdge;

public class ComponentID {
	public WeightedBidirectionalEdge core;
	public int level;
	
	public ComponentID(WeightedBidirectionalEdge core, int level) {
		this.core = core;
		this.level = level;
	}
}
