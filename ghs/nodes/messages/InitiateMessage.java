package projects.ghs.nodes.messages;

import projects.ghs.aux.ComponentID;
import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class InitiateMessage extends Message {

	public ComponentID cid;
	public GHSNode src;
	
	public InitiateMessage(ComponentID cid, GHSNode src) {
		this.cid = cid;
		this.src = src;
	}
	
	public int get_srcID() {
		return src.ID;
	}

	@Override
	public Message clone() {
		return this;
	}

}
