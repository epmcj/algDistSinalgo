package projects.ghs.nodes.messages;

import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class AnswerMessage extends Message {

	public boolean accepted;
	public GHSNode src;
	
	public AnswerMessage(boolean accepted, GHSNode src) {
		this.accepted = accepted;
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
