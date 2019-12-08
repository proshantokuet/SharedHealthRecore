package org.openmrs.module.sharedhealthrecord;

import java.io.Serializable;

import org.openmrs.BaseOpenmrsObject;

public class SHRActionErrorLog extends BaseOpenmrsObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private int eid;
    private String action_type;
    private String error_message;
    private int id;
   
	public String getAction_type() {
		return action_type;
	}

	public void setAction_type(String action_type) {
		this.action_type = action_type;
	}

	@Override
	public Integer getId() {
		// TODO Auto-generated method stub
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		// TODO Auto-generated method stub
		this.setId(id);
	}

	public int getEid() {
		return this.eid;
	}

	public void setEid(int eid) {
		this.eid = eid;
	}

	

	public String getError_message() {
		return this.error_message;
	}

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}

	

}