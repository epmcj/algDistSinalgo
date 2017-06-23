package projects.ghs.nodes.messages;

import projects.ghs.nodes.nodeImplementations.GHSNode;
import sinalgo.nodes.messages.Message;

public class ChangeRootMessage extends BasicMessage {
	
	public double mwoe;
	
	public ChangeRootMessage(double mwoe, GHSNode src) {
		super(src);
		this.mwoe = mwoe;
	}

	@Override
	public Message clone() {
		return this;
	}

}
