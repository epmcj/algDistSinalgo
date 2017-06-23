package projects.ghs.aux;

import projects.ghs.nodes.edges.WeightedBidirectionalEdge;

public class ComponentID {
	public double core;
	public int level;
	
	public ComponentID(double core_weight, int level) {
		this.core = core_weight;
		this.level = level;
	}
	
	public ComponentID(WeightedBidirectionalEdge core, int level) {
		this.core = core.weight;
		this.level = level;
	}
	
	public String toString() {
		return "<" + Double.toString(this.core) + "," + 
			   Integer.toString(this.level) + ">";
	}
	
}
