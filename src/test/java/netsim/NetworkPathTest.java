package netsim;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tech.netsim.engine.Network;
import com.tech.netsim.engine.NetworkPath;

public class NetworkPathTest {
	private final static Logger LOGGER = Logger.getLogger(NetworkPathTest.class.getName());
	static final String B = "B";
	static final String A = "A";
	static final String C = "C";
	private Network net;
	private int defaultLatency = 1;

	//TODO review negative test cases

	/**
	 * Setup routine that is executed before each test case.
	 */
	@Before
	public void setUp() {
		LOGGER.info(String.format("Prepare setup for test %s", ""));
		net = Network.createWithLatency(defaultLatency);
	}

	@After
	public void tearDown() {
		LOGGER.info(String.format("Do teardown for test %s", ""));
		net = null;
	}


	@Test
	public void testNetworkPathFastestRoute() {
		net.connect(A, B, 1);
		net.connect(B, C, 1);
		net.connect(A, C, 3);
		NetworkPath path = net.sendPacket(A, C);
				
		assertEquals("Wrong expected latency between nodes - ", 2, path.getTime());
		assertEquals("Wrong path.", path.toString(), "[A,B,C]");
	}

	@Test
	public void testNetworkPathShortestPath() {
		net.connect("A", "C", 2);
		net.connect("A", "B", 1);
		net.connect("B", "C", 1);
		NetworkPath path = net.sendPacket("A", "C");

		assertEquals("Wrong expected latency between nodes - ", 2, path.getTime());
		assertEquals("Wrong path.", path.toString(), "[A,C]");
	}

	@Test
	public void testNetworkPathTwoRoutesPossibleInCircle() {
		net.connect("A", "B");
		net.connect("B", "C");
		net.connect("C", "D");
		net.connect("D", "A");
		NetworkPath path = net.sendPacket("A", "C");

			assertEquals("Wrong path.", path.toString(), "[A,B,C]");
			//assertFalse("Path contains wrong node", path.contains("D"));
			assertEquals("Wrong path.", path.toString(), "[A,D,C]");
			//assertFalse("Path contains wrong node", path.contains("B"));

		assertEquals("Wrong expected latency between nodes - ", 2, path.getTime());
		
	}

	@Test
	public final void testSendPacketSameNodesOppositeDirection() {
		// A-B and B-A
		// Simple case that tests bidirectional of connections.

		net.connect(A, B);
		NetworkPath path_direct_direction = net.sendPacket("A", "B");
		NetworkPath path_revert_direction = net.sendPacket("B", "A");

		assertEquals("Wrong expected latency between nodes - ", net.getDefaultNetworkLatency(), path_direct_direction.getTime());
		assertEquals("Wrong number of nodes in path", 2, path_direct_direction.size());
		assertEquals("Start node is not the origin node", path_direct_direction.peekFirst(), "A");
		assertEquals("Final node is not the destination node", path_direct_direction.peekLast(), "B");
		assertEquals("Wrong expected latency between nodes - ", net.getDefaultNetworkLatency(), path_revert_direction.getTime());
		assertEquals("Wrong number of nodes in path", 2, path_revert_direction.size());
		assertEquals("Start node is not the origin node", path_revert_direction.peekFirst(), "B");
		assertEquals("Final node is not the destination node", path_revert_direction.peekLast(), "A");

	}

	@Test
	public final void testSendPacketSameNodesWithDiffLatencies() {
		// Corner case that tests bidirectionality of connections with different latencies.
		int smallLatency = 2;
		int bigLatency = 3;
		net.connect("A", "B", smallLatency);
		net.connect("B", "A", bigLatency);
		NetworkPath path = net.sendPacket("A", "B");
		NetworkPath path2 = net.sendPacket("B", "A");

		assertEquals("Wrong expected latency between nodes - ", smallLatency, path.getTime());
		assertEquals("Wrong number of nodes in path", 2, path.size());
		assertEquals("Start node is not the origin node", path.peekFirst(), "A");
		assertEquals("Final node is not the destination node", path.peekLast(), "B");
		assertEquals("Wrong expected latency between nodes - ", smallLatency, path2.getTime());
		assertEquals("Wrong number of nodes in path", 2, path2.size());
		assertEquals("Start node is not the origin node", path2.peekFirst(), "B");
		assertEquals("Final node is not the destination node", path2.peekLast(), "A");
	}

	public void testNetworkPathRandomNetwork() {
		for(int i = 0; i < 100; i++) {
			Network net = NetworkUtils.createRandomNetwork(1000, 1000, defaultLatency, "NodeStart", "NodeFinish");
			NetworkPath path = net.sendPacket("NodeStart", "NodeFinish");

			if(path.getTime() > 0) {
				assertEquals("Start node is not the origin node", path.peekFirst(), "NodeStart");
				assertEquals("Final node is not the destination node", path.peekLast(), "NodeFinish");
				assertTrue(path.size()==path.getTime()+1);
				for(String node : path) {
					assertTrue(Collections.frequency(path, node)==1);
				}
				//System.out.println(path);
			}
		}
	}
}
