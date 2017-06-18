package projects.ghs.nodes.edges;

import projects.ghs.LogL;
import sinalgo.nodes.edges.BidirectionalEdge;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.UniformDistribution;

public class WeightedBidirectionalEdge extends BidirectionalEdge {
	public double weight;
	
	Logging log = Logging.getLogger("sp_log.txt");
	
	@Override
	public void initializeEdge() {
		super.initializeEdge();
		this.weight = UniformDistribution.nextUniform(1.0, 100.0);
		log.logln(LogL.ALWAYS, "Edge " + this.startNode.ID + "-" + this.endNode.ID + ", weight: " + this.weight);
		// We need the following because Sinalgo creates *two* edges for bidirectional edges:
		WeightedBidirectionalEdge wbe = (WeightedBidirectionalEdge) this.oppositeEdge;
		wbe.weight = this.weight;	
	}
	
	@Override
	public String toString() {
		return "Edge " + this.startNode.ID + "-" + this.endNode.ID + ", weight: " + this.weight;
	}
}
