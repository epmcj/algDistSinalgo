package projects.tarefa2.nodes.messages;

import java.util.HashSet;

import projects.tarefa2.nodes.nodeImplementations.T2Node;
import sinalgo.nodes.messages.Message;

public class T2Message extends Message{
	
	private T2Node src;
	
	public T2Message(T2Node src) {
		this.src = src;
	}
	
	public int getSrcID() {
		return this.src.ID;
	}
	
	public HashSet<Integer> getSrcNeighbors() {
		return this.src.neighborsID;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
