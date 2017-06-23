package projects.ghs.nodes.messages;

import projects.ghs.nodes.edges.WeightedBidirectionalEdge;
import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class ReportMessage extends BasicMessage{

	public double min_weight;
	
	public ReportMessage(double min_weight, GHSNode src) {
		super(src);
		this.min_weight = min_weight;
	}
	
	public ReportMessage(WeightedBidirectionalEdge mwoe_candidate, GHSNode src) {
		super(src);
		this.min_weight = mwoe_candidate.weight;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
