package projects.ghs.nodes.timers;

import projects.ghs.nodes.edges.WeightedBidirectionalEdge;
import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.statistics.UniformDistribution;

public class StartTimer extends Timer{
	
	public void set(Node node) {
		this.startAbsolute(UniformDistribution.nextUniform(0.1, 1.5), node);
	}
	
	@Override
	public void fire() {
		GHSNode node = (GHSNode) this.node;
		WeightedBidirectionalEdge edge;
		
		node.real_init();
		
		System.out.println("Node " + node.ID);
		for (int i=0; i < node.edge_list.length; i++) {
			edge = node.edge_list[i];
			System.out.println("\t" + edge.startNode + " -> " + edge.endNode + 
							   ": " + edge.weight + " (" + 
							   node.status[i].toString().toLowerCase()+ ")");
		}
	}

}
