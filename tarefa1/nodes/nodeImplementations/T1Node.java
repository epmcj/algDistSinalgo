package projects.tarefa1.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;

import projects.defaultProject.nodes.timers.MessageTimer;
import projects.tarefa1.nodes.messages.T1Message;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

public class T1Node extends Node{	
	public int totalNodes;
	public int netDiameter;
	public HashSet<Integer> nodesDiscovered;
	public boolean end;
	
	Logging log = Logging.getLogger("t1_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {
		boolean newNode = false;		
		
		if (inbox.size() == 0) {
			this.end = true;
			return;
		}
				
		while (inbox.hasNext()) {
			Message temp = inbox.next();
			if (!(temp instanceof T1Message)) 
				throw new RuntimeException("Unknown message type");
			
			T1Message msg = (T1Message) temp;
			
			// Checking if there is some unknown node in the list of the 
			// neighbors.
			for (Integer nid: msg.nodes) {		
				if (!this.nodesDiscovered.contains(nid)) {
					log.logln(LogL.ALWAYS, "Node " + this.ID 
								+ " discovered node " + nid + " from node "
								+ msg.srcID);
					
					this.nodesDiscovered.add(nid);
					newNode = true;
				}			
			}	
		}
		
		// Sending the updated list of discovered nodes for the neighbors.
		if (newNode) {
			this.broadcast(new T1Message(this.nodesDiscovered, this.ID));
			this.netDiameter++;
		}
		
		this.totalNodes = this.nodesDiscovered.size();
	}

	@Override
	public void preStep() {		
	}

	@Override
	public void init() {
		this.totalNodes  = 1;
		this.netDiameter = 0;
		this.end = false;
		
		this.nodesDiscovered = new HashSet<Integer>();
		this.nodesDiscovered.add(this.ID);
		
		MessageTimer timer = new MessageTimer(
								new T1Message(this.nodesDiscovered, this.ID));
		timer.startRelative(1, this);
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
		if (this.end)
			this.setColor(Color.RED);
		else
			this.setColor(Color.BLUE);
		
		int num_nodes = this.nodesDiscovered.size();
		String text = " " + Integer.toString(this.ID) + ": "
						+ Integer.toString(this.netDiameter) + "|"  
						+ Integer.toString(num_nodes) + " ";
		
		// draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 28, Color.WHITE);
	}
	
	@Override
	public String toString() {
		return "Node ID: " + this.ID;
	}
}
