package org.openmrs.module.sharedhealthrecord;

import java.io.Serializable;

import org.openmrs.BaseOpenmrsData;

public class UBSUniqueIdGenerator extends BaseOpenmrsData implements Serializable {

private static final long serialVersionUID = 1L;
	
	private int eid;
	
	
	private int generateId;

	public int getEid() {
		return eid;
	}

	public void setEid(int eid) {
		this.eid = eid;
	}

	public int getGenerateId() {
		return generateId;
	}

	public void setGenerateId(int generateId) {
		this.generateId = generateId;
	}

	@Override
	public Integer getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(Integer id) {
		// TODO Auto-generated method stub
		
	}
}
