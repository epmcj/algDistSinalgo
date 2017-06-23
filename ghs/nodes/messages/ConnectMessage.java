package projects.ghs.nodes.messages;

import projects.ghs.nodes.edges.WeightedBidirectionalEdge;
import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class ConnectMessage extends BasicMessage {

	public int level;
	public double edge_weight;
	
	public ConnectMessage(int level, double edge_weight, GHSNode src) {
		super(src);
		this.level = level;
		this.edge_weight = edge_weight;
	}
	
	public ConnectMessage(int level, WeightedBidirectionalEdge edge, GHSNode src) {
		super(src);
		this.level = level;
		this.edge_weight = edge.weight;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
