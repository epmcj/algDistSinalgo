package projects.tarefa3.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.tarefa1.nodes.messages.T1Message;
import projects.tarefa3.nodes.messages.T3Message;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

public class T3Node extends Node{	
	
	Logging log = Logging.getLogger("t3_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {
		
		while (inbox.hasNext()) {
			Message temp = inbox.next();
			if (!(temp instanceof T1Message)) 
				throw new RuntimeException("Unknown message type");
			
			T3Message msg = (T3Message) temp;
			
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
		
		String text = " " + Integer.toString(this.ID) + ": ";
		//				+ Integer.toString(this.netDiameter) + "|"  
		//				+ Integer.toString(num_nodes) + " ";
		
		// draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 28, Color.WHITE);
	}
	
	@Override
	public String toString() {
		return "Node ID: " + this.ID;
	}
}
