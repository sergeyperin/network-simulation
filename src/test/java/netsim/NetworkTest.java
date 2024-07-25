package netsim;

import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * This class is blank in purpose.
 * Please don't look into it until further notice :)
 */
public class NetworkTest {
	private final static Logger LOGGER = Logger.getLogger(NetworkTest.class.getName());
	
	/**
	 * Setup routine that is executed before each test case.
	 */
	@Before
	public void setUp() {
		LOGGER.info("Preparing my tests to run");
	}

	@Test
	public void test() {
		LOGGER.info("hello from a simple dummy test!");
	}
}
