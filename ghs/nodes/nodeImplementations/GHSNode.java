package projects.ghs.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import projects.ghs.LogL;
import projects.ghs.aux.ComponentID;
import projects.ghs.aux.EdgeStatus;
import projects.ghs.aux.NodeState;
import projects.ghs.nodes.edges.WeightedBidirectionalEdge;
import projects.ghs.nodes.messages.AnswerMessage;
import projects.ghs.nodes.messages.BasicMessage;
import projects.ghs.nodes.messages.ChangeRootMessage;
import projects.ghs.nodes.messages.ConnectMessage;
import projects.ghs.nodes.messages.InitiateMessage;
import projects.ghs.nodes.messages.ReportMessage;
import projects.ghs.nodes.messages.TestMessage;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.logging.Logging;

public class GHSNode extends Node{
	
	public List<Message> sec_inbox;
	
	public ComponentID cid;
	public WeightedBidirectionalEdge[] edge_list;
	public EdgeStatus[] status;
	public NodeState state;
	public int find_count;
	public boolean isTesting;
	public int i_best_edge;
	
	public int i_testing_edge;
	public GHSNode in_node;
	public double min_weight;
	
	Logging log = Logging.getLogger("ghs_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {
		Message msg;
		boolean check_sec = !this.sec_inbox.isEmpty();
		
		while(inbox.hasNext()) {
			msg = inbox.next();
			
			log.logln(LogL.ALWAYS, "Node " + this.ID + " received a " + 
							   msg.getClass().getSimpleName() + " from Node " +
							   ((BasicMessage)msg).get_srcID());
			
			this.handleMessage(msg);
		}
		
		if (check_sec) {
			List<Message> temp_inbox = new ArrayList<Message>();
			
			while (!this.sec_inbox.isEmpty())
				temp_inbox.add(this.sec_inbox.remove(0));
			
			while (!temp_inbox.isEmpty()) {
				msg = temp_inbox.remove(0);
				log.logln(LogL.ALWAYS, "Node " + this.ID + " received *again* a " +
						   msg.getClass().getSimpleName() + " from Node " +
						   ((BasicMessage)msg).get_srcID());
				this.handleMessage(msg);
			}
		}
		
	}
	
	// Just to apply on the second inbox too.
	public void handleMessage(Message msg) {
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
			
	private void handleInitMessage(InitiateMessage msg) {	
		Node node;
		
		this.changeCompID(msg.cid);
		this.state		 = msg.state;
		this.in_node 	 = msg.src; 			
		this.i_best_edge = -1;
		this.min_weight  = Double.POSITIVE_INFINITY;	
		
		// Forwarding the message.
		for (int i = 0; i < this.edge_list.length; i++) {
			if (this.status[i] == EdgeStatus.BRANCH) {
				node = this.edge_list[i].endNode;
				if (node.ID != msg.get_srcID()) {
					this.sendMsg(new InitiateMessage(msg.cid, this), node);
					
					if (this.state == NodeState.FIND)
						this.find_count++;
				}
			}
		}
		
		if (this.state == NodeState.FIND)		
			this.test();
	}
	
	private void handleTestMessage(TestMessage msg) {
		if (msg.cid.level > this.cid.level) {
			log.logln(LogL.ALWAYS, "- Message going to second inbox.");
			this.sec_inbox.add(msg);
			
		} else {
			if (msg.cid.core != this.cid.core) {	
				this.sendMsg(new AnswerMessage(true, this), msg.src);
				
			} else {	// Nodes are in same component.
				// Marking the edge as rejected.
				int i = this.getIndexOfEdgeTo(msg.get_srcID());
				if (i >= 0)
					this.turnEdgeRejected(i);	

				this.sendMsg(new AnswerMessage(false, this), msg.src);
			}
		}	
	}
		
	private void handleAnswerMessage(AnswerMessage msg) {
		WeightedBidirectionalEdge min_edge;
		
		// Node is not in the same component.
		if (msg.accepted) {		
			log.logln(LogL.ALWAYS, "Node " + this.ID + 
					 		   " received an Accept from Node " + msg.get_srcID());
			
			min_edge = this.edge_list[i_testing_edge];
			if (min_edge.weight < this.min_weight) {
				log.logln(LogL.ALWAYS, "Node " + this.ID + " w_min: " + 
								   this.min_weight +  " -> " + min_edge.weight);
				this.i_best_edge = i_testing_edge;
				this.min_weight  = min_edge.weight; 
			}
			
			this.isTesting = false;
			this.report();
			
		} else {
			log.logln(LogL.ALWAYS, "Node " + this.ID + 
			 		   		   " received a Reject from Node " + msg.get_srcID());
			this.turnEdgeRejected(this.i_testing_edge);
			this.test();			
		}
	}	
	
	private void handleReportMessage(ReportMessage msg) {
		int src = msg.get_srcID();
		if (src != this.in_node.ID) { 
			this.find_count--;
			if (msg.min_weight < this.min_weight) {
				log.logln(LogL.ALWAYS, "Node " + this.ID + " w_min: " + 
						   this.min_weight +  " -> " + msg.min_weight);
		
				this.min_weight = msg.min_weight;
				this.i_best_edge = this.getIndexOfEdgeTo(src);
			}
			
			this.report();
			
		} else { // Nodes in core edge.
			if (this.state == NodeState.FIND) {
				log.logln(LogL.ALWAYS, "- Message going to second inbox.");
				this.sec_inbox.add(msg);
				
			} else {
				if (msg.min_weight > this.min_weight) {
					this.changeroot();
					
				} else if (msg.min_weight == Double.POSITIVE_INFINITY &&
					this.min_weight == Double.POSITIVE_INFINITY) {
					log.logln(LogL.ALWAYS, "End");
				}
			}
		}			
	}
		
	private void handleCRMessage(ChangeRootMessage msg) {
		this.changeroot();
	}
	
	private void handleConnectMessage(ConnectMessage msg) {
		int i;
		ComponentID new_cid;

		i = this.getIndexOfEdgeByWeight(msg.edge_weight);
		
		if (msg.level < this.cid.level) { // ABSORPTION
			log.logln(LogL.ALWAYS, "Absorption between " + this.cid.toString() +
					           " (Node " + this.ID + ") " + " and " +
			           		   msg.src.cid.toString() + " (Node " + 
							   msg.src.ID + ")");
	
			this.turnEdgeBranch(i);
			this.sendMsg(new InitiateMessage(this.cid, this), msg.src);
			
			// Did not reported yet.
			if (this.state == NodeState.FIND)
				this.find_count++;
			
		} else { 	
			if (this.status[i] == EdgeStatus.UNKNOWN) { 
				log.logln(LogL.ALWAYS, "- Message going to second inbox.");
				this.sec_inbox.add(msg);

			} else {	// MERGE
				log.logln(LogL.ALWAYS, "Merge between " + this.cid.toString() + 
						   " (Node " + this.ID + ") " + " and " + 
						   msg.src.cid.toString() + " (Node " + 
						   msg.src.ID + ")");
				
				new_cid = new ComponentID(this.edge_list[i].weight, 
				   	  					  this.cid.level+1);
				
				this.state = NodeState.FIND;
				this.sendMsg(new InitiateMessage(new_cid, this), 
							 this.edge_list[i].endNode);
			}
		}
	}
	
	/**
	 * Sends a message and logs the info about it.
	 * @param msg Message to send.
	 * @param dst Destination node.
	 */
	private void sendMsg(Message msg, Node dst) {
		log.logln(LogL.ALWAYS, "Node " + this.ID + " sending a " + 
				   msg.getClass().getSimpleName() + " to Node " + dst.ID);
		this.send(msg, dst);
	}
	
	/**
	 * Changes the component ID and logs the event.
	 * @param newID New Component ID.
	 */
	private void changeCompID(ComponentID newID) {
		log.logln(LogL.ALWAYS, "Node " + this.ID + " changing CID from " + 
						   this.cid.toString() + " to " + newID.toString());
		this.cid = newID;
	}
	
	/**
	 * Checks if it can send a test message or try to report.
	 */
	private void test() {
		int i = this.i_testing_edge;
		if (i < 0) // for the first time.
			i = 0;
		
		while (i < this.status.length && this.status[i] != EdgeStatus.UNKNOWN)
			i++;
		
		if (i != this.status.length) {
			this.isTesting = true;
			this.i_testing_edge = i;
			this.sendMsg(new TestMessage(this.cid, this), this.edge_list[i].endNode);
			
		} else {
			this.isTesting = false;
			this.report();
		}			
	}
	
	/**
	 * Tries to report to in_node.
	 */
	private void report() {
		if (this.find_count == 0 && !this.isTesting) {
			this.state = NodeState.FOUND;
			this.sendMsg(new ReportMessage(this.min_weight, this), this.in_node);
		}		
	}
	
	/**
	 * Checks if it is necessary to send a ChangeRoot message or sends a 
	 * Connect message in one of its edges.
	 */
	private void changeroot() {
		WeightedBidirectionalEdge best_edge;
		best_edge = this.edge_list[this.i_best_edge];
		
		if (this.status[this.i_best_edge] == EdgeStatus.BRANCH) {
			this.sendMsg(new ChangeRootMessage(this.min_weight, this), 
					 	 best_edge.endNode);
			
		} else { // MWOE is one of the leader's.
			this.sendConnectMessage(best_edge, best_edge.endNode);
		}
	}
	
	/**
	 * Gets the index of the edge that leads to a node in the list.
	 * @param nodeID Node ID on the other side of the edge.
	 * @return index or -1.
	 */
	private int getIndexOfEdgeTo(int nodeID) {
		int i = 0;
		while (i < this.edge_list.length && this.edge_list[i].endNode.ID != nodeID)
			i++;
		
		if (i == this.edge_list.length)
			i = -1;
		
		return i;
	}
	
	/**
	 * Gets the index of the edge with some weight.
	 * @param e_weigth The weight of the edge to be found.
	 * @return index or -1.
	 */
	private int getIndexOfEdgeByWeight(double e_weigth) {
		int i = 0;
		while (i < this.edge_list.length && 
				   e_weigth != this.edge_list[i].weight)
				i++;
		
		if (i == this.edge_list.length)
			i = -1;
		return i;
	}
	
	/**
	 * If possible, puts edge status equals to branch and log the event.
	 * @param index Index of the edge in the list.
	 */
	private void turnEdgeBranch(int index) {	
		if (this.status[index] == EdgeStatus.UNKNOWN) {
			this.status[index] = EdgeStatus.BRANCH;
			this.edge_list[index].changeStatus(EdgeStatus.BRANCH); // just for vis.
			log.logln(LogL.ALWAYS, "Node "+ this.ID +" adding:" + 
					   this.edge_list[index].weight);
		}
	}
	
	/**
	 * If possible, puts edge status equals to rejected and log the event.
	 * @param index Index of the edge in the list.
	 */
	private void turnEdgeRejected(int index) {
		if (this.status[index] == EdgeStatus.UNKNOWN) {
			this.status[index] = EdgeStatus.REJECTED;
			this.edge_list[index].changeStatus(EdgeStatus.REJECTED); // just for vis.
			
			log.logln(LogL.ALWAYS, "Node "+ this.ID +" rejecting:" + 
					   this.edge_list[index].weight);
		}
	}
		
	/**
	 * Sends a Connect message throw an edge and marks the edge as branch.
	 * @param mwoe Edge to send the message.
	 * @param dst Destination node.
	 */
	private void sendConnectMessage(WeightedBidirectionalEdge mwoe, Node dst) {
		this.sendMsg(new ConnectMessage(this.cid.level, mwoe.weight, this), dst);
		
		int i = 0;
		while (i < this.edge_list.length && mwoe != this.edge_list[i])
				i++;
		
		if (i != this.edge_list.length)
			this.turnEdgeBranch(i);
	}
	

	@Override
	public void preStep() {	}

	@Override
	public void init() {
		this.sec_inbox = new ArrayList<Message>();
		this.state = NodeState.SLEEPING;
	}
	
	/**
	 * Wake up procedure. Initializes the node.
	 */
	public void wake_up() {
		WeightedBidirectionalEdge wbe, temp, mwoe;
		int i;
		
		this.in_node 		= null;
		this.i_testing_edge = -1;
		this.find_count		= 0;
		this.isTesting		= false;
		
		// Initializing the ordered edge list and the status.
		int num_edges  = this.outgoingConnections.size();
		this.edge_list = new WeightedBidirectionalEdge[num_edges];
		this.status    = new EdgeStatus[num_edges];
		
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
		
		for (i = 0; i < this.status.length; i++)
			this.status[i] = EdgeStatus.UNKNOWN;
		
		this.turnEdgeBranch(0);
		mwoe = this.edge_list[0];
		this.state = NodeState.FOUND;
		this.cid = new ComponentID(mwoe, 0);
		this.sendMsg(new ConnectMessage(0, mwoe, this), mwoe.endNode);
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
		
		String text = " " + Integer.toString(this.ID) + " " + this.state.toString().substring(0, 2);			
		// Draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 22, Color.BLACK);
	}
	
	@Override
	public String toString() {
		return "Node " + this.ID + " \nCID:" + this.cid.toString();
	}

}
