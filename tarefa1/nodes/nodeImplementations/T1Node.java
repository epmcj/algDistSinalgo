package projects.tarefa1.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;

import projects.tarefa1.CustomGlobal;
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
	public int maxDist;
	public HashSet<Integer> nodesDiscovered;
	public int phase;
	public int roundsLeft;
	public boolean isAwake;
	
	Logging log = Logging.getLogger("t1_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {
		Message temp;
		T1Message msg;
		
		if (this.isAwake) {
			switch (this.phase) {
				// Starting the node discovery process.
				case 0:
					this.broadcast(new T1Message(this));
					this.phase++;
					break;
				// Process to know the number of nodes and the most distant.
				case 1:
					boolean newNodeDiscovered = false;
					while (inbox.hasNext()) {
						temp = inbox.next();
						if (!(temp instanceof T1Message)) 
							throw new RuntimeException("Unknown message type");					
						msg = (T1Message) temp;
						
						// Checking if there is some unknown node in the list 
						// of the neighbors.
						for (Integer nid: msg.getNodesDiscovered()) {		
							if (!this.nodesDiscovered.contains(nid)) {
								log.logln(LogL.ALWAYS, "Node " + this.ID 
											+ " discovered node " + nid 
											+ " from node " + msg.getSrcID());
								
								this.nodesDiscovered.add(nid);
								newNodeDiscovered = true;
							}			
						}	
					}
					
					// Sending the updated list of discovered nodes for the neighbors.
					if (newNodeDiscovered) {
						this.broadcast(new T1Message(this));
						this.maxDist++;			
					} else {
						this.netDiameter = this.maxDist;
						this.totalNodes = this.nodesDiscovered.size();
						this.roundsLeft = this.totalNodes;
						this.phase++;
					}
					break;
				// Finding the network diameter.
				case 2:
					if (this.roundsLeft > 0) {				
						while (inbox.hasNext()) {
							temp = inbox.next();
							if (!(temp instanceof T1Message)) 
								throw new RuntimeException("Unknown message type");					
							msg = (T1Message) temp;
							
							if (msg.getNetDiameter() > this.netDiameter)
								this.netDiameter = msg.getNetDiameter();
						}
						
						this.broadcast(new T1Message(this));
						this.roundsLeft--;
						
					} else {
						this.isAwake = false;
					}
				
					break;
				default:
					throw new RuntimeException("Unknown phase");
			}
		}
	}

	@Override
	public void preStep() {		
	}

	@Override
	public void init() {
		this.totalNodes  = 1;
		this.netDiameter = this.maxDist = 0;
		this.phase = this.roundsLeft = 0;
		this.isAwake = true;
		
		this.nodesDiscovered = new HashSet<Integer>();
		this.nodesDiscovered.add(this.ID);
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void postStep() {	
		CustomGlobal.finishThisRound &= !this.isAwake;
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {		
	}
			
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		if (this.isAwake)
			this.setColor(Color.BLUE);
		else
			this.setColor(Color.LIGHT_GRAY);
		
		String nDiam = (this.phase < 2) ? Integer.toString(this.maxDist): 
							Integer.toString(this.netDiameter);
		
		String nNodes = (this.phase < 2) ? Integer.toString(this.nodesDiscovered.size()) : 
							Integer.toString(this.totalNodes);
		
		String text = " " + Integer.toString(this.ID) + ": "
						  + nDiam + "|"  + nNodes + " ";
		
		// draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 28, Color.WHITE);
	}
	
	@Override
	public String toString() {
		return "Node ID: " + this.ID + "\ntotalNodes: " + this.totalNodes
				+ "\nnetDiameter: " + this.netDiameter;
	}
}
