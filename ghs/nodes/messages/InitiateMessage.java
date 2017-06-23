package projects.ghs.nodes.messages;

import projects.ghs.aux.ComponentID;
import projects.ghs.aux.NodeState;
import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class InitiateMessage extends BasicMessage {

	public ComponentID cid;
	public NodeState state;
	
	public InitiateMessage(ComponentID cid, GHSNode src) {
		super(src);
		this.state = src.state;
		this.cid   = cid;
	}
	
	public InitiateMessage(ComponentID cid, GHSNode src, NodeState state) {
		super(src);
		this.state = state;
		this.cid   = cid;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
