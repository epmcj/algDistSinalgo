package projects.ghs.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import com.sun.java.swing.plaf.windows.WindowsGraphicsUtils;

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
	public boolean leader;
	
	public int index_testing_edge;
	public GHSNode best_node;
	public WeightedBidirectionalEdge edge_to_report;
	
	public List<GHSNode> best_nodes;
	public List<Integer> wait_list;
	public List<TestMessage> test_queue;
	
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
	
	/**
	 * 
	 * @param msg
	 */
	private void handleCRMessage(ChangeRootMessage msg) {
		System.out.println("Node " + this.ID + " received a " + "ChangeRoot" + 
						   " Message from " + msg.get_srcID());
		WeightedBidirectionalEdge new_core;
		int i;
		
		// Forwarding the message.
		for (GHSNode node : this.best_nodes) {
			if (node.ID != msg.get_srcID()) {
				this.send(new ChangeRootMessage(msg.cid, this), node);
			}
		}
		
		if (msg.cid.level > this.cid.level){	
			this.cid = msg.cid;	// Updating component id.
			
			new_core = msg.cid.core;
			i = 0;
			while (i < this.edge_list.length && 
				   this.edge_list[i].weight == new_core.weight)
				i++;
			
			// MWOE is one of its edge.
			if (i < this.edge_list.length) {
				new_core = this.edge_list[i]; // Bidirectional = 2 edges.
				this.send(new ConnectMessage(msg.cid.core, this), 
						  new_core.endNode);
				this.status[i] = EdgeStatus.BRANCH;
				// ADD TO LIST OF BEST EDGES
				this.best_nodes.add((GHSNode)new_core.endNode);
				// If it is the new leader.
				if (this.ID >= new_core.endNode.ID && 
					this.ID >= new_core.startNode.ID)
					this.leader = true;
			}
		}
				
		this.checkTestQueue();		
	}
	
	/**
	 * 
	 * @param msg
	 */
	private void handleReportMessage(ReportMessage msg) {
		System.out.println("Node " + this.ID + " received a " + "Report" + 
				   		   " Message from " + msg.get_srcID());
		int src, pos, new_leader;
		WeightedBidirectionalEdge new_core;
		ComponentID new_cid;
		
		src = msg.get_srcID();
		pos = this.checkWaitListFor(src);
		
		if (pos >= 0) {
			if (this.checkBetterCandidate(msg.mwoe_candidate))
				this.edge_to_report = msg.mwoe_candidate;
			
			this.wait_list.remove(pos);
			this.callReportCheck();
		}		
	}
	
	/**
	 * 
	 * @param msg
	 */
	private void handleConnectMessage(ConnectMessage msg) {
		System.out.println("Node " + this.ID + " received a " + "Connect" + 
				   		   " Message from " + msg.get_srcID());
		
		//On Connect(L) messages:
		//i. Lsender = Lreceiver
		//		new (L+1)-fragment with new core. Initiate
		//		messages are broadcasted: < L+1 ; ID >
		//ii. Lsender < Lreceiver
		//		immediate absorption. Initiate message to small
		//		fragment includes search command (if Report is
		//		not already sent) 
		
	}
	
	/**
	 * 
	 * @param msg
	 */
	private void handleInitMessage(InitiateMessage msg) {	
		System.out.println("Node " + this.ID + " received a " + "Initiate" + 
				   		   " Message from " + msg.get_srcID());
		this.cid       		= msg.cid;
		this.best_node 		= msg.src; // to convergecast.
		this.edge_to_report = null;	   // to convergecast.
		
		// Forwarding the message.
		for (GHSNode node : this.best_nodes) {
			if (node.ID != msg.get_srcID()) {
				this.send(new InitiateMessage(msg.cid, this), node);
				this.wait_list.add(node.ID);
			}
		}
		
		// Starting the tests.
		if (!this.startSendTestMessage()) {
			if (this.can_report())
				this.sendReportMessage();
		}
	}
	
	// PRECISA INCLUIR CONDICAO DO LIDER
	/**
	 * 
	 * @param msg
	 */
	private void handleAnswerMessage(AnswerMessage msg) {
		System.out.println("Node " + this.ID + " received a " + "Answer" + 
				   		   " Message from " + msg.get_srcID());
		int src;
		WeightedBidirectionalEdge own_mwoe;
		
		src = msg.get_srcID();
		if (this.edge_list[this.index_testing_edge].endNode.ID == src) {
			// Node is not in the same component.
			if (msg.accepted) {				
				own_mwoe = this.edge_list[this.index_testing_edge];
				if (this.checkBetterCandidate(own_mwoe))
					this.edge_to_report = own_mwoe;

				this.wait_list.remove(0); // Own ID will be in first index.
				this.callReportCheck();
				
			} else {
				this.status[this.index_testing_edge] = EdgeStatus.REJECTED;
				
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
	private void handleTestMessage(TestMessage msg) {
		ComponentID rcvd_cid, own_cid;
		rcvd_cid = msg.cid;
		own_cid  = this.cid;
		
		if (rcvd_cid == this.cid) {	// Nodes are in same component.
			this.send(new AnswerMessage(false, this), msg.src);
			
		} else {
			if (rcvd_cid.level <= own_cid.level)
				this.send(new AnswerMessage(true, this), msg.src);		
			else // Can not respond yet.
				this.test_queue.add(msg);
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
		WeightedBidirectionalEdge new_core;
		ComponentID new_cid;
		int new_leaderID;
		
		// New core edge to update component id.
		new_core = this.edge_to_report;
		new_cid = new ComponentID(new_core, this.cid.level + 1);
		if (new_core.endNode.ID > new_core.startNode.ID)
			new_leaderID = new_core.endNode.ID;
		else
			new_leaderID = new_core.startNode.ID;
		// Sending change root messages to update nodes comp id.
		for (GHSNode node: this.best_nodes)
			this.send(new ChangeRootMessage(new_cid, this), node);
		
		if (new_leaderID == this.ID)
			this.startNewPhase();
		else
			this.leader = false;
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
	/*
	private int getFirstUnknownEdgeIndex() {
		int i = 0;
		while (i < this.status.length && this.status[i] != EdgeStatus.UNKNOWN)
			i++;
		
		if (i == this.status.length)
			i = -1;
		return i;
	}
	*/
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
			if (this.leader)
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
		this.send(new ReportMessage(this.edge_to_report, this), this.best_node);
	}
	
	/**
	 * 
	 * @param edge
	 * @return
	 */
	private boolean checkBetterCandidate(WeightedBidirectionalEdge edge) {
		return (this.edge_to_report == null) || 
			   (edge.weight < this.edge_to_report.weight);
		
	}

	@Override
	public void preStep() {	}

	@Override
	public void init() {
		StartTimer timer = new StartTimer(); 
		timer.set(this);
	}
	
	/**
	 * 
	 */
	public void real_init() {
		WeightedBidirectionalEdge wbe, temp, mwoe;
		int i;
		
		this.best_node = null;
		this.leader    = true;
		this.index_testing_edge = 0;
		
		this.best_nodes = new ArrayList<GHSNode>();
		this.wait_list  = new ArrayList<Integer>();
		this.test_queue = new ArrayList<TestMessage>();
		
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
		
		this.status = new EdgeStatus[num_edges];
		for (i = 0; i < this.status.length; i++)
			this.status[i] = EdgeStatus.UNKNOWN;
		
		mwoe = this.edge_list[0];
		this.cid = new ComponentID(mwoe, 0);
		this.send(new ConnectMessage(mwoe, this), mwoe.endNode);
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
		if (this.leader)
			this.setColor(Color.GREEN);
		else
			this.setColor(Color.BLUE);
		
		String text = " " + Integer.toString(this.ID) + " ";			
		// Draw the node as a circle with the text inside
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 28, Color.BLACK);
	}

}
