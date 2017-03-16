/**
 *
 */
package edu.harvard.hms.dbmi.bd2k.irct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author gabor
 *
 */
public class IRCTApplicationTest {

	private IRCTApplication app = null;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.app = new IRCTApplication();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication#init()}.
	 */
	@Test
	public void testInit() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication#getVersion()}.
	 */
	@Test
	public void testGetVersion() {
		String appversion = this.app.getVersion();
		System.out.println("testGetVersion() appversion:"+appversion);

		if (appversion.equals("N/A")) {
			//fail("Could not determine version of the IRCTApplication object.");
		} else {
			assertEquals("1.4", appversion);
		}
	}

	/**
	 * Test method for {@link edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication#getResources()}.
	 */
	@Test
	public void testGetResources() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication#getSupportedJoinTypes()}.
	 */
	@Test
	public void testGetSupportedJoinTypes() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication#getResultDataConverters()}.
	 */
	@Test
	public void testGetResultDataConverters() {
		//System.out.println(this.app.getResultDataConverters());
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication#getResultDataFolder()}.
	 */
	@Test
	public void testGetResultDataFolder() {
		//fail("Not yet implemented"); // TODO
	}

}
