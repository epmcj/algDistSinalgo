package projects.tarefa2.nodes.messages;

import projects.tarefa2.nodes.nodeImplementations.T2Node;
import sinalgo.nodes.messages.Message;

public class T2FloodingMessage extends Message{

	public int srcID;
	public float localCoef;
	
	public T2FloodingMessage(T2Node src) {
		this.srcID = src.ID;
		this.localCoef = src.coefLocal;
	}
	
	public int getSrcID() {
		return this.srcID;
	}
	
	public float getLocalCoef() {
		return this.localCoef;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
