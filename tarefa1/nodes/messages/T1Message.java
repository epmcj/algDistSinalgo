package projects.tarefa1.nodes.messages;

import java.util.HashSet;

import projects.tarefa1.nodes.nodeImplementations.T1Node;
import sinalgo.nodes.messages.Message;

public class T1Message extends Message{
	
	public HashSet<Integer> nodes;
	public int srcID;
	public int maxDist;
	
	public T1Message(T1Node src) {
		this.srcID = src.ID;
		this.nodes = new HashSet<Integer>(src.nodesDiscovered);
		this.maxDist = src.netDiameter;
	}
	
	public HashSet<Integer> getNodesDiscovered() {
		return this.nodes;
	}
	
	public int getSrcID() {
		return this.srcID;
	}
	
	public int getSrcMaxDist() {
		return this.maxDist;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
