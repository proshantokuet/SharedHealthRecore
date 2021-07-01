package org.openmrs.module.sharedhealthrecord;

import java.io.Serializable;

import org.openmrs.BaseOpenmrsObject;

public class SHRActionErrorLog extends BaseOpenmrsObject implements
		Serializable {
	private static final long serialVersionUID = 1L;
	private int eid;
	private String action_type;
	private String error_message;
	private String uuid;
	private String postJson;
	private int voided;
	private int sent_status;
	
	@Override
	public String toString() {
		return "SHRActionErrorLog [eid=" + eid + ", action_type=" + action_type
				+ ", error_message=" + error_message + ", uuid=" + uuid
				+ ", postJson=" + postJson + ", voided=" + voided + ", sent_status="
				+ sent_status + "]";
	}

	public int getSent_status() {
		return sent_status;
	}

	public void setSent_status(int sent_status) {
		this.sent_status = sent_status;
	}

	public int getVoided() {
		return voided;
	}

	public void setVoided(int voided) {
		this.voided = voided;
	}



	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getAction_type() {
		return action_type;
	}

	public void setAction_type(String action_type) {
		this.action_type = action_type;
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

	public String getPostJson() {
		return postJson;
	}

	public void setPostJson(String postJson) {
		this.postJson = postJson;
	}

	@Override
	public Integer getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(Integer id) {
		// TODO Auto-generated method stub
//		this.setId(id);
	}

}