package projects.ghs.nodes.messages;

import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class AnswerMessage extends BasicMessage {

	public boolean accepted;
	
	public AnswerMessage(boolean accepted, GHSNode src) {
		super(src);
		this.accepted = accepted;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
