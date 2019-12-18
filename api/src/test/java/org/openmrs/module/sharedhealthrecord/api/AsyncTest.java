package org.openmrs.module.sharedhealthrecord.api;

import org.openmrs.scheduler.tasks.AbstractTask;

public class AsyncTest extends AbstractTask {

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		System.out.print("Test async start");
		super.startExecuting();
		System.out.print("Test async stop");
		
	}

}
