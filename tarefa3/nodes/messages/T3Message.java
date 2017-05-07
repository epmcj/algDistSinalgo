package projects.tarefa3.nodes.messages;

import projects.tarefa3.nodes.nodeImplementations.T3Node;
import sinalgo.nodes.messages.Message;

public class T3Message extends Message{
	
	private T3Node src;
	
	public T3Message(T3Node src) {
		this.src = src;
	}
	
	public int getNodeValue() {
		return this.src.value;
	}
	
	public boolean isaNodeWinner() {
		return (this.src.independentSet == 1) ? true : false;
	}
	
	public boolean isaNodeLoser() {
		return (this.src.independentSet == 0) ? true : false;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
