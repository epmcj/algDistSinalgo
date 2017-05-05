package projects.tarefa1.nodes.messages;

import java.util.HashSet;

import sinalgo.nodes.messages.Message;

public class T1Message extends Message{
	
	public HashSet<Integer> nodes;
	public int srcID;
	
	public T1Message(HashSet<Integer> nodesID, int srcID) {
		this.nodes = nodesID;
		this.srcID = srcID;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
