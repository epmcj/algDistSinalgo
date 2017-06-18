package projects.ghs.nodes.messages;

import projects.ghs.aux.ComponentID;
import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class TestMessage extends Message {
	
	public ComponentID cid;
	public GHSNode src;
	
	public TestMessage(ComponentID cid, GHSNode src) {
		this.cid = cid;
		this.src = src;
	}
	
	public int get_srcID() {
		return this.src.ID;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
