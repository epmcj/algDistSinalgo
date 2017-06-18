package projects.ghs.models.connectivityModels;

import java.util.Enumeration;
import java.util.Random;

import projects.ghs.CustomGlobal;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.models.ConnectivityModel;
import sinalgo.nodes.Node;
import sinalgo.runtime.Runtime;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

public class RandomStaticConnectivity extends ConnectivityModel {

	public Logging log = Logging.getLogger("ghs_log.txt");
	public Random rand = Tools.getRandomNumberGenerator();
	
	@Override
	public boolean updateConnections(Node n) throws WrongConfigurationException {
		boolean edgeAdded = false;
		int num_neighbors = 0;
		
		Enumeration<Node> neighbors = Runtime.nodes.getPossibleNeighborsEnumeration(n);
		
		while (neighbors.hasMoreElements()){
			Node possibleNeighbor = neighbors.nextElement();
			if (n.ID != possibleNeighbor.ID){
				num_neighbors++;
				
				// if the possible neighbor is connected with the the node: add the connection to the outgoing connection of n 
				if (!isAlreadyConnected(n, possibleNeighbor) && (rand.nextDouble() < CustomGlobal.prob)){
					// add it to the outgoing Edges of n. The EdgeCollection itself checks, if the Edge is already contained	
					edgeAdded = !n.outgoingConnections.add(n, possibleNeighbor, true) || edgeAdded; // note: don't write it the other way round, otherwise, the edge is not added if edgeAdded is true.
					CustomGlobal.connections.get(n.ID-1).add(possibleNeighbor);
				} 
			}
		}
		
		// At least one must be added in order to have a connected graph.
		if (edgeAdded && num_neighbors > 0) {
			neighbors = Runtime.nodes.getPossibleNeighborsEnumeration(n);			
			edgeAdded = !n.outgoingConnections.add(n, neighbors.nextElement(), true) || edgeAdded;
		}
		
		return edgeAdded;
	}

	private boolean isAlreadyConnected(Node n, Node possibleNeighbor) {
		if (CustomGlobal.connections.get(n.ID-1).contains(possibleNeighbor)) {
			System.out.println(n.ID + "is already conn to " + possibleNeighbor.ID);
			return true;
		}
	
		return false;
	}
}
