package projects.ghs.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

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
import projects.ghs.nodes.timers.StartTimer;
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
	public boolean isLeader;
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
			
			System.out.println("Node " + this.ID + " received a " + 
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
				System.out.println("Node " + this.ID + " received *again* a " +
						   msg.getClass().getSimpleName() + " from Node " +
						   ((BasicMessage)msg).get_srcID());
				this.handleMessage(msg);
			}
		}
		
	}
	
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
			
	/**
	 * 
	 * @param msg
	 */
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
	
	/**
	 * 
	 * @param msg
	 */
	private void handleTestMessage(TestMessage msg) {		
		if (msg.cid.level > this.cid.level) {
			System.out.println("- Message going to second inbox.");
			this.sec_inbox.add(msg);
			
		} else {
			if (msg.cid.core != this.cid.core) {	// Nodes are in same component.
				this.sendMsg(new AnswerMessage(true, this), msg.src);
				
			} else {				
				// Marking the edge as rejected.
				int i = this.getIndexOfEdgeTo(msg.get_srcID());
				if (i >= 0)
					this.turnEdgeRejected(i);	

				this.sendMsg(new AnswerMessage(false, this), msg.src);
			}
		}	
	}
		
	/**
	 * 
	 * @param msg
	 */
	private void handleAnswerMessage(AnswerMessage msg) {
		WeightedBidirectionalEdge min_edge;
		
		// Node is not in the same component.
		if (msg.accepted) {		
			System.out.println("Node " + this.ID + 
					 		   " received an Accept from Node " + msg.get_srcID());
			
			min_edge = this.edge_list[i_testing_edge];
			if (min_edge.weight < this.min_weight) {
				System.out.println("Node " + this.ID + " w_min: " + 
								   this.min_weight +  " -> " + min_edge.weight);
				this.i_best_edge = i_testing_edge;
				this.min_weight  = min_edge.weight; 
			}
			
			this.isTesting = false;
			this.report();
			
		} else {
			System.out.println("Node " + this.ID + 
			 		   		   " received a Reject from Node " + msg.get_srcID());
			this.turnEdgeRejected(this.i_testing_edge);
			this.test();			
		}
	}	
	
	/**
	 * 
	 * @param msg
	 */
	private void handleReportMessage(ReportMessage msg) {
		int src = msg.get_srcID();
		if (src != this.in_node.ID) { 
			this.find_count--;
			if (msg.min_weight < this.min_weight) {
				System.out.println("Node " + this.ID + " w_min: " + 
						   this.min_weight +  " -> " + msg.min_weight);
		
				this.min_weight = msg.min_weight;
				this.i_best_edge = this.getIndexOfEdgeTo(src);
			}
			
			this.report();
			
		} else { // Nodes in core edge.
			if (this.state == NodeState.FIND) {
				System.out.println("- Message going to second inbox.");
				this.sec_inbox.add(msg);
				
			} else {
				//this.find_count--;
				if (msg.min_weight > this.min_weight) {
					this.isLeader = false;
					this.changeroot();
					
				} else if (msg.min_weight == Double.POSITIVE_INFINITY &&
					this.min_weight == Double.POSITIVE_INFINITY) {
					System.out.println("End");
				}
			}
		}			
	}
		
	/**
	 * 
	 * @param msg
	 */
	private void handleCRMessage(ChangeRootMessage msg) {
		this.changeroot();
	}
	
	/**
	 * 
	 * @param msg
	 */
	private void handleConnectMessage(ConnectMessage msg) {
		int i, srcID;
		ComponentID new_cid;
		
		srcID = msg.get_srcID();
		i = this.getIndexOfEdgeByWeight(msg.edge_weight);
		
		if (msg.level < this.cid.level) { // ABSORPTION
			System.out.println("Absorption between " + this.cid.toString() +
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
				System.out.println("- Message going to second inbox.");
				this.sec_inbox.add(msg);

			} else {	// MERGE
				System.out.println("Merge between " + this.cid.toString() + 
						   " (Node " + this.ID + ") " + " and " + 
						   msg.src.cid.toString() + " (Node " + 
						   msg.src.ID + ")");

				// New leader.
				this.isLeader = (this.ID > srcID);
				
				new_cid = new ComponentID(this.edge_list[i].weight, 
				   	  					  this.cid.level+1);
				
				this.state = NodeState.FIND;
				this.sendMsg(new InitiateMessage(new_cid, this), 
							 this.edge_list[i].endNode);
			}
		}
	}
	
	/**
	 * 
	 * @param msg
	 * @param dst
	 */
	private void sendMsg(Message msg, Node dst) {
		System.out.println("Node " + this.ID + " sending a " + 
				   msg.getClass().getSimpleName() + " to Node " + dst.ID);
		this.send(msg, dst);
	}
	
	/**
	 * 
	 * @param newID
	 */
	private void changeCompID(ComponentID newID) {
		System.out.println("Node " + this.ID + " changing CID from " + 
						   this.cid.toString() + " to " + newID.toString());
		this.cid = newID;
	}
	
	/**
	 * 
	 */
	private void test() {
		int i = this.i_testing_edge;
		if (i < 0) 
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
	 * 
	 */
	private void report() {
		if (this.find_count == 0 && !this.isTesting) {
			this.isLeader = false;
			this.state = NodeState.FOUND;
			this.sendMsg(new ReportMessage(this.min_weight, this), this.in_node);
		}		
	}
	
	private void changeroot() {
		WeightedBidirectionalEdge best_edge;
		best_edge = this.edge_list[this.i_best_edge];
		
		if (this.status[this.i_best_edge] == EdgeStatus.BRANCH) {
			this.isLeader = false;
			this.sendMsg(new ChangeRootMessage(this.min_weight, this), 
					 	 best_edge.endNode);
			
		} else { // MWOE is one of the leader's.
			this.sendConnectMessage(best_edge, best_edge.endNode);
		}
	}
	
	/**
	 * 
	 * @param nodeID
	 * @return
	 */
	private int getIndexOfEdgeTo(int nodeID) {
		int i = 0;
		while (i < this.edge_list.length && this.edge_list[i].endNode.ID != nodeID)
			i++;
		
		if (i == this.edge_list.length)
			i = -1;
		
		return i;
	}
	
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
	 * 
	 * @param index
	 */
	private void turnEdgeBranch(int index) {	
		if (this.status[index] == EdgeStatus.UNKNOWN) {
			this.status[index] = EdgeStatus.BRANCH;
			this.edge_list[index].changeStatus(EdgeStatus.BRANCH); // just for vis.
			System.out.println("Node "+ this.ID +" adding:" + 
					   this.edge_list[index].weight);
		}
	}
	
	/**
	 * 
	 * @param index
	 */
	private void turnEdgeRejected(int index) {
		if (this.status[index] == EdgeStatus.UNKNOWN) {
			this.status[index] = EdgeStatus.REJECTED;
			this.edge_list[index].changeStatus(EdgeStatus.REJECTED); // just for vis.
			
			System.out.println("Node "+ this.ID +" rejecting:" + 
					   this.edge_list[index].weight);
		}
	}
		
	/**
	 * 
	 * @param mwoe
	 * @param dst
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
		//StartTimer timer = new StartTimer(); 
		//timer.set(this);
		this.state = NodeState.SLEEPING;
	}
	
	/**
	 * 
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
		this.isLeader = false;
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
		if (this.isLeader)
			this.setColor(Color.GREEN);
		else
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
