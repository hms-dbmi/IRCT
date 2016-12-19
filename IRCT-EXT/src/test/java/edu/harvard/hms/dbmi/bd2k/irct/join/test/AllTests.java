package edu.harvard.hms.dbmi.bd2k.irct.join.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FullOuterJoinTest.class, InnerJoinTest.class,
		LeftOuterJoinTest.class, RightOuterJoinTest.class, UnionJoinTest.class })
public class AllTests {

}
