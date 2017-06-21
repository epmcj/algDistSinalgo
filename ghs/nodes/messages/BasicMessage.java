package projects.ghs.nodes.messages;

import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class BasicMessage extends Message{
	
	public GHSNode src;
	
	public BasicMessage(GHSNode src){
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
