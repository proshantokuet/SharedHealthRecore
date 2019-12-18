package org.openmrs.module.sharedhealthrecord;

import java.io.Serializable;

import org.openmrs.BaseOpenmrsObject;


public class SHRActionAuditInfo extends BaseOpenmrsObject implements Serializable{
	private static final long serialVersionUID = 1L;
	private int aid;
	private String record_name;
	private String last_id;
	private String last_timestamp;
	private String action_time;

	public String getRecord_name() {
		return record_name;
	}

	public void setRecord_name(String record_name) {
		this.record_name = record_name;
	}

	public String getAction_time() {
		return this.action_time;
	}

	public void setAction_time(String action_time) {
		this.action_time = action_time;
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

	public void setAid(int aid){
		this.aid = aid;
	}
	public int getAid(){
		return this.aid;
	}
	

	public void setLast_id(String last_id){
		this.last_id = last_id;
	}
	public String getLast_id(){
		return this.last_id;
	}
	public void setLast_timestamp(String last_timestamp){
		this.last_timestamp = last_timestamp;
	}
	public String getLast_timestamp(){
		return this.last_timestamp;
	}


}
