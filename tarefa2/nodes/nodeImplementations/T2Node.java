package projects.tarefa2.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.tarefa1.nodes.messages.T1Message;
import projects.tarefa2.nodes.messages.T2Message;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

public class T2Node extends Node{	
		
	Logging log = Logging.getLogger("t2_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {
						
		while (inbox.hasNext()) {
			Message temp = inbox.next();
			if (!(temp instanceof T1Message)) 
				throw new RuntimeException("Unknown message type");
			
			T2Message msg = (T2Message) temp;
			
		}
	}

	@Override
	public void preStep() {		
	}

	@Override
	public void init() {
		/*MessageTimer timer = new MessageTimer(
		new T1Message(this.nodesDiscovered, this.ID));
		timer.startRelative(1, this);*/
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void postStep() {	
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {		
	}
			
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		this.setColor(Color.BLUE);
		
		//int num_nodes = this.nodesDiscovered.size();
		String text = " " + Integer.toString(this.ID) + ": ";
		
		// draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 28, Color.WHITE);
	}
	
	@Override
	public String toString() {
		return "Node ID: " + this.ID;
	}
}
