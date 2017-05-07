package projects.tarefa3.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import projects.tarefa3.CustomGlobal;
import projects.tarefa3.nodes.messages.T3Message;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

public class T3Node extends Node{	
	
	public int independentSet;
	public int round;
	public int value;
	public boolean awake;
	public Random rand;
	
	public static final int NUM_NODES = 15;
	
	Logging log = Logging.getLogger("t3_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {			
		Message temp;
		T3Message msg;		
		
		if (this.awake) {
			switch (this.round) {
				// in the first round the node just generates a random value 
				// and broadcasts it to its neighbors.
				case 0:
					// Choosing the node value that will be broadcasted.
					this.value = rand.nextInt((int)Math.pow(T3Node.NUM_NODES, 4));
					this.broadcast(new T3Message(this));
					break;					
					
				// Checks if it is a winner and broadcasts it.
				case 1:	
					boolean valueIsBigger = true;
					while (inbox.hasNext() && valueIsBigger) {
						temp = inbox.next();
						if (!(temp instanceof T3Message)) 
							throw new RuntimeException("Unknown message type");
						msg = (T3Message) temp;
						
						if (msg.getNodeValue() > this.value)
							valueIsBigger = false;
					}
					
					if (valueIsBigger) {
						this.independentSet = 1;
						log.logln(LogL.ALWAYS, "Node " + this.ID + 
									" is in the independent set.");
					}
					
					this.broadcast(new T3Message(this));
					break;
					
				// Checks if it is a loser and broadcasts it.
				case 2:
					while (inbox.hasNext() && (this.independentSet != 0)) {
						temp = inbox.next();
						if (!(temp instanceof T3Message)) 
							throw new RuntimeException("Unknown message type");
						
						msg = (T3Message) temp;
						
						if (msg.isaNodeWinner()) {
							this.independentSet = 0;
							log.logln(LogL.ALWAYS, "Node " + this.ID + 
										" is not in the independent set.");
						}
					}
					
					this.broadcast(new T3Message(this));
					
					if (this.independentSet != -1)
						this.awake = false;
					
					break;					
				default:			
					throw new RuntimeException("Unknown round number");
			}
			
			this.round = (this.round + 1) % 3;
		}
	}

	@Override
	public void preStep() {		
	}

	@Override
	public void init() {
		this.independentSet = -1; // Unknown
		this.rand = sinalgo.tools.Tools.getRandomNumberGenerator();
		this.round = 0;
		this.awake = true;
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void postStep() {
		CustomGlobal.finishThisRound &= !this.awake;
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {		
	}
			
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {

		if (this.independentSet == 1)
			this.setColor(Color.GREEN);
		else if (this.independentSet == 0)
			this.setColor(Color.RED);
		else
			this.setColor(Color.BLUE);
		
		String status = (this.independentSet != -1) ? 
							Integer.toString(this.independentSet) : "?";
		
		String text = " " + Integer.toString(this.ID) + ": " + status + " ";
		
		// draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 28, Color.WHITE);
	}
	
	@Override
	public String toString() {
		return "Node ID: " + this.ID;
	}
}

