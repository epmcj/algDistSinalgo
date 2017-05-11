package projects.tarefa2.nodes.messages;

import projects.tarefa2.nodes.nodeImplementations.T2Node;
import sinalgo.nodes.messages.Message;

/**
 * This message is used in order to spread the local information so the nodes
 * can be able to calculate the Global Clustering Coefficient.
 */
public class T2FloodingMessage extends Message{
	private int srcID;
	private int closedTri;
	private int totalTri;
	
	public T2FloodingMessage(T2Node src, int closedTri, int totalTri) {
		this.srcID = src.ID;
		this.closedTri = closedTri;
		this.totalTri = totalTri;
	}
	
	public int getSrcID() {
		return this.srcID;
	}
	
	public int getLocalNumClosedTri() {
		return this.closedTri;
	}
	
	public int getLocalNumTri() {
		return this.totalTri;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
