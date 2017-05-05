package projects.tarefa1.models.connectivityModels;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;
import sinalgo.tools.logging.LogL;
import sinalgo.tools.logging.Logging;

public class RandomStaticConnectivity extends ConnectivityModelHelper {

	Logging log = Logging.getLogger("t1_log.txt");
	
	@Override
	protected boolean isConnected(Node from, Node to) {
		
		MessageDigest m;
		int min = Math.min(from.ID, to.ID);
		int max = Math.max(from.ID, to.ID);
		try {
			// This MD5 hash is just to keep connectivity constant throughout the simulation.
			// (ConnectivityModelHelper recreates edges every round.)
			m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(BigInteger.valueOf(min).toByteArray());
			m.update(BigInteger.valueOf(max).toByteArray());
			
			if ((m.digest()[0] % 5) == 0)
				log.logln(LogL.ALWAYS, from.ID + " is Connected to " + to.ID);
			
			return (m.digest()[0] % 5) == 0;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}
}
