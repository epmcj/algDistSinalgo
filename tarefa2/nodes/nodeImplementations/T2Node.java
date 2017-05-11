package projects.tarefa2.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashSet;

import projects.tarefa2.nodes.messages.T2DiscoveryMessage;
import projects.tarefa2.nodes.messages.T2FloodingMessage;
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
	public HashSet<T2FloodingMessage> msgCoefRcvd;
	public int phase;
	public boolean isAwake;
		
	Logging log = Logging.getLogger("t2_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {
		Message temp;
		T2DiscoveryMessage msg1;
		T2FloodingMessage  msg2;
		
		if (this.isAwake) {
			switch (this.phase) {
				// Just broadcasts its ID for neighborhood discovery.
				case 0:
					this.broadcast(new T2DiscoveryMessage(this));
					this.phase++;
					break;
					
				// Discovers its neighborhood and broadcasts it.
				case 1:
					while (inbox.hasNext()) {
						temp = inbox.next();
						if (!(temp instanceof T2DiscoveryMessage)) 
							throw new RuntimeException("Unknown message type");
						
						msg1 = (T2DiscoveryMessage) temp;
						this.neighborsID.add(msg1.getSrcID());
					}
					this.broadcast(new T2DiscoveryMessage(this));
					this.phase++;
					break;
					
				// Calculates the local clustering coefficient. 
				case 2:
					float connections = 0;
					while (inbox.hasNext()) {
						temp = inbox.next();
						if (!(temp instanceof T2DiscoveryMessage)) 
							throw new RuntimeException("Unknown message type");
						
						msg1 = (T2DiscoveryMessage) temp;
						for (Integer nid: msg1.getSrcNeighbors()) {
							if (this.neighborsID.contains(nid))
								connections++;
						}
					}
					
					// Max number of connections between the neighbors.
					float maxConn = (float) (this.neighborsID.size() * 
											(this.neighborsID.size() - 1));
			
					// Avoiding division by zero.
					if (maxConn != 0)
						this.coefLocal = connections / maxConn;
					else 
						this.coefLocal = 0;
					
					log.logln(LogL.ALWAYS, "Node " + this.ID + " - LCC: " 
											+ this.coefLocal);
					
					
					// Starting the spreading the local information.
					msg2 = new T2FloodingMessage(this, (int)connections, 
												 	(int)maxConn);
					this.msgCoefRcvd.add(msg2);
					this.broadcast(msg2);
					
					this.phase++;
					break;
					
				// Calculates the global clustering coefficient.
				case 3:
					boolean newInfo = false;
					
					while (inbox.hasNext()) {
						temp = inbox.next();
						if (!(temp instanceof T2FloodingMessage)) 
							throw new RuntimeException("Unknown message type");
						
						msg2 = (T2FloodingMessage) temp;
						if (!this.msgCoefRcvd.contains(msg2)) {
							newInfo = true;
							this.msgCoefRcvd.add(msg2);
							this.broadcast(msg2);
						}
					}
					
					// Already received messages from all nodes.
					if (!newInfo) {
						float nClosedTri = 0;
						float totalTri   = 0;
						
						for (T2FloodingMessage msgRcvd : this.msgCoefRcvd) {
							nClosedTri += (float)msgRcvd.getLocalNumClosedTri();
							totalTri   += (float)msgRcvd.getLocalNumTri();
						}
						
						if (totalTri != 0)
							this.coefGlobal = nClosedTri/totalTri;
						else 
							this.coefGlobal = 0;
						
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
		this.neighborsID = new HashSet<Integer>();
		this.msgCoefRcvd = new HashSet<T2FloodingMessage>();
		this.coefLocal 	 = this.coefGlobal = -1;
		this.isAwake 	 = true;
		this.phase 		 = 0;
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
		
		String clocal = (this.coefLocal < 0) ? "?" : 
								String.format("%.2f", this.coefLocal);
		
		String cglobal = (this.coefGlobal < 0) ? "?" : 
								String.format("%.2f", this.coefGlobal);
		
		String text = " " //+ Integer.toString(this.ID) + ": "
						  +  clocal + " | " +  cglobal + " ";
		
		// draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 28, Color.WHITE);
	}
	
	@Override
	public String toString() {
		String clocal = (this.coefLocal < 0) ? "?" : 
			String.format("%.2f", this.coefLocal);

		String cglobal = (this.coefGlobal < 0) ? "?" : 
			String.format("%.2f", this.coefGlobal);
		
		return "Node ID: " + this.ID + "\ncoefLocal: " + clocal
				+ "\ncoefGlobal: " + cglobal;
	}
}
