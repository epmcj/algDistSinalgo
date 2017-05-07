package projects.tarefa2.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;

import projects.tarefa2.nodes.messages.T2Message;
import projects.tarefa2.CustomGlobal;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

public class T2Node extends Node{	
	
	public float coefLocal;
	public float coefGlobal;
	public HashSet<Integer> neighborsID;
	public int phase;
	public boolean isActive;
		
	Logging log = Logging.getLogger("t2_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {
		Message temp;
		T2Message msg;
		
		if (this.isActive) {
			switch (this.phase) {
				// Just broadcasts its ID for neighborhood discovery.
				case 0:
					this.broadcast(new T2Message(this));
					this.phase++;
					break;
					
				// Discovers its neighborhood and broadcasts it.
				case 1:
					while (inbox.hasNext()) {
						temp = inbox.next();
						if (!(temp instanceof T2Message)) 
							throw new RuntimeException("Unknown message type");
						
						msg = (T2Message) temp;
						this.neighborsID.add(msg.getSrcID());
					}
					this.broadcast(new T2Message(this));
					this.phase++;
					break;
					
				// Calculates the local clustering coefficient. 
				case 2:
					float connections = 0;
					while (inbox.hasNext()) {
						temp = inbox.next();
						if (!(temp instanceof T2Message)) 
							throw new RuntimeException("Unknown message type");
						
						msg = (T2Message) temp;
						for (Integer nid: msg.getSrcNeighbors()) {
							if (this.neighborsID.contains(nid))
								connections++;
						}
					}
					
					float maxConn = (float) (this.neighborsID.size() * 
											(this.neighborsID.size() - 1));
			
					if (maxConn != 0)
						this.coefLocal = connections / maxConn;
					else 
						this.coefGlobal = 0;
					
					log.logln(LogL.ALWAYS, "Node " + this.ID + " - LCC: " 
											+ this.coefLocal);
					
					this.phase++;
					break;
					
				// Calculates the global clustering coefficient.
				case 3:
					
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
		this.neighborsID = new HashSet<Integer>();
		this.coefLocal 	 = this.coefGlobal = -1;
		this.isActive 	 = true;
		this.phase 		 = 0;
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void postStep() {	
		CustomGlobal.finishThisRound &= !this.isActive;
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {		
	}
			
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		if (this.isActive)
			this.setColor(Color.BLUE);
		else
			this.setColor(Color.LIGHT_GRAY);
		
		String clocal = (this.coefLocal < 0) ? "?" : 
								String.format("%.3f", this.coefLocal);
		
		String cglobal = (this.coefGlobal < 0) ? "?" : 
								String.format("%.3f", this.coefGlobal);
		
		String text = " " + Integer.toString(this.ID) + ": "
						  +  clocal + " | " +  cglobal + " ";
		
		// draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 28, Color.WHITE);
	}
	
	@Override
	public String toString() {
		return "Node ID: " + this.ID;
	}
}
