package projects.tarefa2.nodes.messages;

import java.util.HashSet;

import projects.tarefa2.nodes.nodeImplementations.T2Node;
import sinalgo.nodes.messages.Message;

public class T2DiscoveryMessage extends Message{
	
	public HashSet<Integer> nodes;
	public int srcID;
	
	public T2DiscoveryMessage(T2Node src) {
		this.srcID = src.ID;
		this.nodes = new HashSet<Integer>(src.neighborsID);
	}
	
	public int getSrcID() {
		return this.srcID;
	}
	
	public HashSet<Integer> getSrcNeighbors() {
		return this.nodes;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
