package projects.ghs.nodes.messages;

import projects.ghs.nodes.edges.WeightedBidirectionalEdge;
import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class ConnectMessage extends BasicMessage {

	public WeightedBidirectionalEdge mwoe;
	
	public ConnectMessage(WeightedBidirectionalEdge mwoe, GHSNode src) {
		super(src);
		this.mwoe = mwoe;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
