package edu.harvard.hms.dbmi.bd2k.irct;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author gabor
 */
public class IRCTApplicationTest {

	private static IRCTApplication app = new IRCTApplication();

	/**
	 * Test method for {@link IRCTApplication#getVersion()}.
	 */
	@Test
	public void testGetVersion() {
		assertEquals("X.X", app.getVersion());
	}
}
