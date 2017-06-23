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
	
	public ComponentID cid;
	public WeightedBidirectionalEdge[] edge_list;
	public EdgeStatus[] status;
	public boolean isLeader;
	public NodeState state;
	
	public int index_testing_edge;
	public GHSNode best_node;
	public double edge_to_conn;
	public double min_weight;

	public List<Integer> wait_list;
	public List<GHSNode> best_nodes;
	public List<TestMessage> test_queue;
	public List<ConnectMessage> conn_queue;
	
	Logging log = Logging.getLogger("ghs_log.txt");

	@Override
	public void handleMessages(Inbox inbox) {
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			
			System.out.println("Node " + this.ID + " received a " + 
							   msg.getClass().getSimpleName() + " from Node " +
							   ((BasicMessage)msg).get_srcID());
			
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
			
	/**
	 * 
	 * @param msg
	 */
	private void handleInitMessage(InitiateMessage msg) {			
		// Updating component id.
		this.cid = msg.cid;	
		
		this.checkTestQueue();
		this.checkConnQueue();
		
		// Forwarding the message.
		for (GHSNode node : this.best_nodes) {
			if (node.ID != msg.get_srcID()) {
				this.send(new InitiateMessage(msg.cid, this), node);
				this.wait_list.add(node.ID);
			}
		}
		
		if (msg.state == NodeState.FIND) {
			this.state		= NodeState.FIND;
			this.cid       	= msg.cid;
			this.best_node 	= msg.src; 					// to convergecast.
			this.min_weight = Double.POSITIVE_INFINITY;	// to convergecast.
			
			// Starting the tests.
			if (!this.startSendTestMessage()) {
				if (this.can_report())
					this.sendReportMessage();
			}
		}
	}
	
	/**
	 * 
	 * @param msg
	 */
	private void handleTestMessage(TestMessage msg) {
		ComponentID rcvd_cid, own_cid;
		int i = 0;
		
		rcvd_cid = msg.cid;
		own_cid  = this.cid;
		
		if (rcvd_cid == this.cid) {	// Nodes are in same component.
			this.send(new AnswerMessage(false, this), msg.src);
			
			// Marking the edge as rejected.
			while (i < this.edge_list.length && 
				   rcvd_cid.core == this.edge_list[i].weight)
				i++;
			
			if (i < this.edge_list.length)
				this.status[i] = EdgeStatus.REJECTED;
				
			
		} else {
			if (rcvd_cid.level <= own_cid.level)
				this.send(new AnswerMessage(true, this), msg.src);		
			else // Can not respond yet.
				this.test_queue.add(msg);
		}	
	}
	
	/**
	 * 
	 * @param msg
	 */
	private void handleAnswerMessage(AnswerMessage msg) {
		int src;
		WeightedBidirectionalEdge min_edge;
		
		src = msg.get_srcID();
		if (this.edge_list[this.index_testing_edge].endNode.ID == src) {
			// Node is not in the same component.
			if (msg.accepted) {				
				min_edge = this.edge_list[this.index_testing_edge];
				if (min_edge.weight < this.min_weight)
					this.min_weight = min_edge.weight;

				this.wait_list.remove(0); // Own ID will be in first index.
				this.callReportCheck();
				
			} else {
				this.status[this.index_testing_edge] = EdgeStatus.REJECTED;  // COLORIR ARESTAS !!!!
				
				// More edges to verify.
				if (!this.sendNextTestMessage()) {
					// All neighboors are in the same component.
					this.wait_list.remove(0); // Own ID will be in first index.
					this.callReportCheck();
				}
			}
		}
	}	
	
	/**
	 * 
	 * @param msg
	 */
	private void handleReportMessage(ReportMessage msg) {
		int src, pos;
		
		src = msg.get_srcID();
		pos = this.checkWaitListFor(src);
		
		if (pos >= 0) {
			if (msg.min_weight < this.min_weight)
				this.min_weight = msg.min_weight;
			
			this.wait_list.remove(pos);
			this.callReportCheck();
		}		
	}
		
	/**
	 * 
	 * @param msg
	 */
	private void handleCRMessage(ChangeRootMessage msg) {
		WeightedBidirectionalEdge core_edge; // used to send connect message.
		double new_core;
		int i;
		
		new_core = msg.mwoe;
		i = 0;
		while (i < this.edge_list.length && 
				new_core != this.edge_list[i].weight)
			i++;
		
		// MWOE is one of its edge.
		if (i < this.edge_list.length) {
			core_edge = this.edge_list[i]; // Bidirectional = 2 edges.
			this.send(new ConnectMessage(this.cid.level, core_edge, this), 
					core_edge.endNode);	
			this.checkConnQueue();
			
		} else {
			// Forwarding the message.
			for (GHSNode node : this.best_nodes) {
				if (node.ID != msg.get_srcID()) {
					this.send(msg, node);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param msg
	 */
	private void handleConnectMessage(ConnectMessage msg) {
		int i, srcID;
		
		srcID = msg.get_srcID();
		
		i = 0;
		while (i < this.edge_list.length && 
				   srcID == this.edge_list[i].endNode.ID)
				i++;
				
		if (msg.level == this.cid.level) {
			// MWOE in common.
			if (msg.edge_weight == this.edge_to_conn) {	// MERGE.
				System.out.println("Merge between " + this.cid.toString() + 
								   " (Node " + this.ID + ") " + " and " + 
								   msg.src.cid.toString() + " (Node " + 
								   msg.src.ID + ").");
				
				this.status[i] = EdgeStatus.BRANCH;
				this.best_nodes.add(msg.src);
				
				// New leader.
				if (this.ID > srcID) {
					this.isLeader = true;
					this.cid = new ComponentID(this.edge_list[i].weight, 
											   this.cid.level+1);
					this.checkConnQueue();
					this.state = NodeState.FIND;
					this.sendInitMessages();
					this.startSendTestMessage();	
				}
				
			} else {
				this.conn_queue.add(msg);
			}
			
		} else if (msg.level < this.cid.level) {		// ABSORPTION.
			System.out.println("Absorption between " + this.cid.toString() +
					           " (Node " + this.ID + ") " + " and " +
			           		   " and " + msg.src.cid.toString() + " (Node " + 
							   msg.src.ID + ").");
			
			this.status[i] = EdgeStatus.BRANCH;
			this.best_nodes.add(msg.src);
			
			// Did not reported yet.
			if (this.state == NodeState.FIND)
				this.wait_list.add(srcID);
			
			this.send(new InitiateMessage(this.cid, this), msg.src);		
			
		} else {
			this.conn_queue.add(msg);
		}		
	}
	
	/**
	 * 
	 * @param nodeID
	 * @return
	 */
	private int checkWaitListFor(int nodeID) {
		int i = 0;
		// Checks if the node is in the waiting list.
		while (i < this.wait_list.size() && this.wait_list.get(i) != nodeID)
			i++;
		
		if (i == this.wait_list.size())
			i = -1; // Not found.
				
		return i;
	}
	
	/**
	 * 
	 */
	private void leaderReport() {
		double new_core;
		int i;
		
		// New core edge to update component id.
		new_core = this.min_weight;
		
		if (new_core == Double.POSITIVE_INFINITY) {
			System.out.println("END");
			
		} else {
			this.isLeader = false;
					
			i = 0;
			while (i < this.edge_list.length &&
				   new_core != this.edge_list[i].weight)
				i++;
			
			if (i < this.edge_list.length) {
				this.sendConnectMessage(new_core, this.edge_list[i].endNode);
				
			} else {
				// Sending change root messages to update nodes comp id.
				for (GHSNode node: this.best_nodes)
					this.send(new ChangeRootMessage(new_core, this), node);
			}
		}
		
	}

	/**
	 * 
	 */
	private void checkTestQueue() {
		List<TestMessage> temp = new ArrayList<TestMessage>();
		
		while (!this.test_queue.isEmpty()) {
			temp.add(this.test_queue.remove(0));
		}
		
		for (TestMessage msg: temp) {
			this.handleTestMessage(msg);
		}
	}
	
	/**
	 * 
	 */
	private void checkConnQueue() {
		List<ConnectMessage> temp = new ArrayList<ConnectMessage>();
		
		while (!this.conn_queue.isEmpty()) {
			temp.add(this.conn_queue.remove(0));
		}
		
		for (ConnectMessage msg: temp) {
			this.handleConnectMessage(msg);
		}
	}
	
	/**
	 * 
	 */
	private void startNewPhase() {
		this.sendInitMessages();
		if (!this.startSendTestMessage()) {
			// FIM.
		}
	}
	
	/**
	 * 
	 */
	private void sendInitMessages() {
		for (GHSNode node: this.best_nodes)
			this.send(new InitiateMessage(this.cid, this), node);
	}
	
	/**
	 * 
	 * @return
	 */
	private int getNextUnknownEdgeIndex() {
		int i = this.index_testing_edge;
		while (i < this.status.length && this.status[i] != EdgeStatus.UNKNOWN)
			i++;
		
		if (i == this.status.length)
			i = -1;
		return i;
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean startSendTestMessage() {
		this.index_testing_edge = 0;
		return this.sendNextTestMessage();
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean sendNextTestMessage() {
		int i = this.getNextUnknownEdgeIndex();
		if (i > 0){
			this.index_testing_edge = i;
			this.send(new TestMessage(this.cid, this), this.edge_list[i].endNode);
		}
		return (i > 0);
	}
	
	/**
	 * 
	 */
	private void callReportCheck() {
		if (this.can_report()) {	
			if (this.isLeader)
				this.leaderReport();		
			else
				this.sendReportMessage();			
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean can_report() {
		return this.wait_list.isEmpty();
	}
	
	/**
	 * 
	 */
	private void sendReportMessage() {
		this.send(new ReportMessage(this.min_weight, this), this.best_node);
		this.state = NodeState.FOUND;
	}
	
	/**
	 * 
	 * @param mwoe
	 * @param dst
	 */
	private void sendConnectMessage(double mwoe, Node dst) {
		this.send(new ConnectMessage(this.cid.level, mwoe, this), dst);
		this.edge_to_conn = mwoe;
	}
	

	@Override
	public void preStep() {	}

	@Override
	public void init() {
		StartTimer timer = new StartTimer(); 
		timer.set(this);
		this.state = NodeState.SLEEPING;
	}
	
	/**
	 * 
	 */
	public void real_init() {
		WeightedBidirectionalEdge wbe, temp, mwoe;
		int i;
		
		this.best_node 			= null;
		this.isLeader    		= true;
		this.index_testing_edge = 0;
		
		this.best_nodes = new ArrayList<GHSNode>();
		this.wait_list  = new ArrayList<Integer>();
		this.test_queue = new ArrayList<TestMessage>();
		this.conn_queue = new ArrayList<ConnectMessage>();
		
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
		
		mwoe = this.edge_list[0];
		this.state = NodeState.FOUND;
		this.cid = new ComponentID(mwoe, 0);
		this.sendConnectMessage(mwoe.weight, mwoe.endNode);
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

}
