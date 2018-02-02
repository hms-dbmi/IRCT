/**
 *
 */
package edu.harvard.hms.dbmi.bd2k.irct;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * @author gabor
 *
 */
public class IRCTApplicationTest {

	private static IRCTApplication app = null;

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
		
		assertEquals("X.X", this.app.getVersion());
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
