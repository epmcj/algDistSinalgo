package projects.ghs.nodes.messages;

import projects.ghs.aux.ComponentID;
import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class InitiateMessage extends BasicMessage {

	public ComponentID cid;
	
	public InitiateMessage(ComponentID cid, GHSNode src) {
		super(src);
		this.cid = cid;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
