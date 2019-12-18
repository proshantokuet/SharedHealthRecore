package org.openmrs.module.sharedhealthrecord.api;

import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class AsyncProcessTest extends BaseModuleContextSensitiveTest {
	
	@Test
	public void asyncTestMethgod(){
		AsyncTest test = new AsyncTest();
		test.execute();
	}

}
