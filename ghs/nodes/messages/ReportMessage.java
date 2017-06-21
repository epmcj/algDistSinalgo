package projects.ghs.nodes.messages;

import projects.ghs.nodes.edges.WeightedBidirectionalEdge;
import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class ReportMessage extends BasicMessage{

	public WeightedBidirectionalEdge mwoe_candidate;
	
	public ReportMessage(WeightedBidirectionalEdge mwoe_candidate, GHSNode src) {
		super(src);
		this.mwoe_candidate = mwoe_candidate;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
