package projects.ghs.nodes.edges;

import java.awt.Graphics;
import java.text.DecimalFormat;

import projects.ghs.CustomGlobal;
import projects.ghs.LogL;
import projects.ghs.aux.EdgeStatus;
import sinalgo.gui.GraphPanel;
import sinalgo.gui.helper.Arrow;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Position;
import sinalgo.nodes.edges.BidirectionalEdge;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.UniformDistribution;

public class WeightedBidirectionalEdge extends BidirectionalEdge {
	public double weight;
	public EdgeStatus status;
	
	Logging log = Logging.getLogger("sp_log.txt");
	
	@Override
	public void initializeEdge() {
		super.initializeEdge();
		
		do {
			this.weight = UniformDistribution.nextUniform(1.0, 100.0);
		} while (CustomGlobal.edge_weights.contains(this.weight));
		CustomGlobal.edge_weights.add(this.weight);
		
		log.logln(LogL.ALWAYS, "Edge " + this.startNode.ID + "-" + this.endNode.ID + ", weight: " + this.weight);
		// We need the following because Sinalgo creates *two* edges for bidirectional edges:
		WeightedBidirectionalEdge wbe = (WeightedBidirectionalEdge) this.oppositeEdge;
		wbe.weight = this.weight;
		this.status = EdgeStatus.UNKNOWN;
	}
	
	/**
	 * Change edge status.
	 * @param new_status New edge status.
	 */
	public void changeStatus(EdgeStatus new_status) {
		this.status = new_status;
	}
	
	@Override
	public String toString() {
		return "Edge " + this.startNode.ID + "-" + this.endNode.ID + 
			   ", weight: " + this.weight + ", status: " + this.status.toString();
	}
	
	public void draw(Graphics g, PositionTransformation pt) {
		Position p1 = startNode.getPosition();
		pt.translateToGUIPosition(p1);
		int fromX = pt.guiX, fromY = pt.guiY; // temporarily store
		Position p2 = endNode.getPosition();
		pt.translateToGUIPosition(p2);
		
		if((this.numberOfMessagesOnThisEdge == 0)&&
				(this.oppositeEdge != null)&&
				(this.oppositeEdge.numberOfMessagesOnThisEdge > 0)){
			
			g.setColor(getColor());
			
			if (this.status == EdgeStatus.BRANCH)
				GraphPanel.drawBoldLine(g, fromX, fromY, pt.guiX, pt.guiY, 2);
			else if (this.status == EdgeStatus.REJECTED)
				GraphPanel.drawDottedLine(g, fromX, fromY, pt.guiX, pt.guiY);
			else
				Arrow.drawArrowHead(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
			
		} else {
			if(numberOfMessagesOnThisEdge > 0) {
				g.setColor(getColor());
				
				if (this.status == EdgeStatus.BRANCH)
					GraphPanel.drawBoldLine(g, fromX, fromY, pt.guiX, pt.guiY, 2);
				else if (this.status == EdgeStatus.REJECTED)
					GraphPanel.drawDottedLine(g, fromX, fromY, pt.guiX, pt.guiY);
				else
					Arrow.drawArrow(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
			} else {
				g.setColor(getColor());
				
				if (this.status == EdgeStatus.BRANCH)
					GraphPanel.drawBoldLine(g, fromX, fromY, pt.guiX, pt.guiY, 2);
				else if (this.status == EdgeStatus.REJECTED)
					GraphPanel.drawDottedLine(g, fromX, fromY, pt.guiX, pt.guiY);
				else 
					Arrow.drawArrow(fromX, fromY, pt.guiX, pt.guiY, g, pt, getColor());
			}
		}
		DecimalFormat df = new DecimalFormat("#.00");
		g.drawString(df.format(this.weight), (fromX + pt.guiX)/2, (fromY + pt.guiY)/2);
	}
}
