package projects.ghs.nodes.messages;

import projects.ghs.nodes.edges.WeightedBidirectionalEdge;
import sinalgo.nodes.messages.Message;

public class ReportMessage extends Message{

	public WeightedBidirectionalEdge mwoe_candidate;
	
	public ReportMessage(WeightedBidirectionalEdge mwoe_candidate) {
		this.mwoe_candidate = mwoe_candidate;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
