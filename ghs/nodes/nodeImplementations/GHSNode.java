package projects.ghs.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import javafx.collections.ListChangeListener.Change;
import projects.ghs.aux.ComponentID;
import projects.ghs.aux.EdgeStatus;
import projects.ghs.nodes.edges.WeightedBidirectionalEdge;
import projects.ghs.nodes.messages.AnswerMessage;
import projects.ghs.nodes.messages.ChangeRootMessage;
import projects.ghs.nodes.messages.ConnectMessage;
import projects.ghs.nodes.messages.InitiateMessage;
import projects.ghs.nodes.messages.ReportMessage;
import projects.ghs.nodes.messages.TestMessage;
import projects.ghs.nodes.timers.StartTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;

public class GHSNode extends Node{
	
	public ComponentID cid;
	public WeightedBidirectionalEdge[] edge_list;
	public EdgeStatus[] status;
	public int phase;
	public boolean leader;
	
	public int test_edge;
	public GHSNode parent;
	
	Logging log = Logging.getLogger("ghs_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			
			if (msg instanceof InitiateMessage) {
				this.handleInitMessage((InitiateMessage)msg);
				
			} else if (msg instanceof ReportMessage) {
				this.handleReportMessage((ReportMessage)msg);
				
			} else if (msg instanceof TestMessage) {
				this.handleTestMessage((TestMessage) msg);
				
			} else if (msg instanceof ConnectMessage) {
				this.handleConnectMessage((ConnectMessage) msg);
				
			} else if (msg instanceof AnswerMessage) {
				this.handleAnswerMessage((AnswerMessage)msg);
				
			} else if (msg instanceof ChangeRootMessage) {
				this.handleCRMessage((ChangeRootMessage) msg);
			}
		}
		
	}
	
	private void handleCRMessage(ChangeRootMessage msg) {
		
	}
	
	private void handleTestMessage(TestMessage msg) {
		int src;
		ComponentID rcvd_cid;
		
		src 	 = msg.get_srcID();
		rcvd_cid = msg.cid;
		
		// Nodes are in same component.
		if (rcvd_cid == this.cid) {
			
		} else {
			if (rcvd_cid.level > this.cid.level) {
				
			}
		}	
	}
	
	private void handleReportMessage(ReportMessage msg) {
		
	}
	
	private void handleConnectMessage(ConnectMessage msg) {
		
	}
	
	private void handleInitMessage(InitiateMessage msg) {
		int src, dst;
		
		this.parent = msg.src;
		this.cid    = msg.cid;
		
		src = msg.get_srcID();
		// Forwarding the message.
		for (int i = 0; i < this.edge_list.length; i++) {
			dst = this.edge_list[i].endNode.ID;
			
			if (this.status[i] == EdgeStatus.BRANCH &&  dst != src)
				this.send(msg, this.edge_list[i].endNode);
		}
		
		// Starting the tests.
		int i = 0;
		while (i < this.status.length && this.status[i] != EdgeStatus.UNKNOWN)
			i++;
		
		if (i < this.status.length) {
			this.test_edge = i;
			this.send(new TestMessage(msg.cid, this), this.edge_list[i].endNode);
		} else {
			this.send(new ReportMessage(null), this.parent);
		}
	}
	
	// ALTERAR STATUS DAS ARESTAS !!!
	private void handleAnswerMessage(AnswerMessage msg) {
		int src;
		
		src = msg.get_srcID();
		if (this.edge_list[this.test_edge].endNode.ID == src) {
			// Node is not in the same component.
			if (msg.accepted) {
				this.send(new ReportMessage(this.edge_list[this.test_edge]), this.parent);
				
			} else {
				this.status[this.test_edge] = EdgeStatus.REJECTED;
				
				int i = this.test_edge;
				while (i < this.status.length && this.status[i] != EdgeStatus.UNKNOWN)
					i++;
				// More edges to verify.
				if (i < this.status.length) {
					this.test_edge = i;
					this.send(new TestMessage(this.cid, this), this.edge_list[i].endNode);
				} else {
					// All neighboors are in the same component.
					this.send(new ReportMessage(null), this.parent);
				}
			}
		}
	}

	@Override
	public void preStep() {	}

	@Override
	public void init() {
		StartTimer timer = new StartTimer(); 
		timer.set(this);
	}
	
	public void real_init() {
		WeightedBidirectionalEdge wbe, temp, mwoe;
		int i;
		
		this.phase     = 0;
		this.parent    = null;
		this.leader    = true;
		this.test_edge = 0;
		
		// Initializing the ordered edge list and the status.
		int num_edges  = this.outgoingConnections.size();
		this.edge_list = new WeightedBidirectionalEdge[num_edges];
		// Sorting edge list.
		for (Edge e : this.outgoingConnections) {
			wbe = (WeightedBidirectionalEdge) e;
			i = 0;
			while (this.edge_list[i] != null && this.edge_list[i].weight < wbe.weight)
				i++;
			
			if (this.edge_list[i] == null) {
				this.edge_list[i] = wbe;
				
			} else {
				while (this.edge_list[i] != null && i < this.edge_list.length) {
					temp = this.edge_list[i];
					this.edge_list[i] = wbe;
					wbe = temp;
					i++;
				}
				this.edge_list[i] = wbe;
			}
		}
		
		this.status    = new EdgeStatus[num_edges];
		for (i = 0; i < this.status.length; i++)
			this.status[i] = EdgeStatus.UNKNOWN;
		
		mwoe = this.edge_list[0];
		this.cid = new ComponentID(mwoe, 0);
		this.send(new ConnectMessage(mwoe), mwoe.endNode);
	}

	@Override
	public void neighborhoodChange() { }

	@Override
	public void postStep() { }

	@Override
	public void checkRequirements() throws WrongConfigurationException { }
	
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#draw(java.awt.Graphics, sinalgo.gui.transformation.PositionTransformation, boolean)
	 */
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		this.setColor(Color.BLUE);
		String text = " " + Integer.toString(this.ID) + " ";			
		// draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 28, Color.GREEN);
	}

}
