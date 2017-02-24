package edu.harvard.hms.dbmi.bd2k.irct.join.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FullOuterHashJoinTest.class, InnerHashJoinTest.class,
		LeftOuterHashJoinTest.class, RightOuterHashJoinTest.class })
public class AllHashTests {

}
