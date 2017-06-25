package projects.ghs.models.connectivityModels;

import java.util.Enumeration;
import java.util.Random;

import projects.ghs.CustomGlobal;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.models.ConnectivityModel;
import sinalgo.nodes.Node;
import sinalgo.runtime.Runtime;
import sinalgo.tools.Tools;

public class RandomStaticConnectivity extends ConnectivityModel {

	public Random rand = Tools.getRandomNumberGenerator();
	
	@Override
	public boolean updateConnections(Node n) throws WrongConfigurationException {
		boolean edgeAdded = false;
		
		Enumeration<Node> neighbors = Runtime.nodes.getPossibleNeighborsEnumeration(n);
		
		while (neighbors.hasMoreElements()){
			Node possibleNeighbor = neighbors.nextElement();
			if (n.ID != possibleNeighbor.ID){
				if (possibleNeighbor.ID == (n.ID + 1)) {	// To guarantee connectivity.
					if (!isAlreadyConnected(n, possibleNeighbor))
						edgeAdded = !n.outgoingConnections.add(n, possibleNeighbor, true) || edgeAdded;
				
				} else {
					// if the possible neighbor is connected with the the node: 
					// add the connection to the outgoing connection of n. 
					if (!isAlreadyConnected(n, possibleNeighbor) && 
						(rand.nextDouble() < CustomGlobal.prob)) {
						// add it to the outgoing Edges of n. The EdgeCollection
						// itself checks, if the Edge is already contained.	
						// note: don't write it the other way round, otherwise, 
						// 	     the edge is not added if edgeAdded is true.
						edgeAdded = !n.outgoingConnections.add(n, possibleNeighbor, true) || edgeAdded; 
						CustomGlobal.connections.get(n.ID-1).add(possibleNeighbor);
					} 
				}
			}
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
