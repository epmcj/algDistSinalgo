package projects.ghs.nodes.messages;

import projects.ghs.nodes.edges.WeightedBidirectionalEdge;
import sinalgo.nodes.messages.Message;

public class ConnectMessage extends Message {

	public WeightedBidirectionalEdge mwoe;
	
	public ConnectMessage(WeightedBidirectionalEdge mwoe) {
		this.mwoe = mwoe;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
