package org.openmrs.module.sharedhealthrecord;

import java.io.Serializable;

import org.openmrs.BaseOpenmrsObject;

public class SHRExternalPatient extends BaseOpenmrsObject implements Serializable  {
	 private static final long serialVersionUID = 1L;
	 private int epid;
	 private String action_type;
	 private String patient_uuid;
	 private String encounter_uuid;
	 private String is_send_to_central;
	 
	 
	public String getAction_type() {
		return action_type;
	}

	public void setAction_type(String action_type) {
		this.action_type = action_type;
	}

	public int getEpid() {
		return epid;
	}

	public void setEpid(int epid) {
		this.epid = epid;
	}

	

	public String getPatient_uuid() {
		return patient_uuid;
	}

	public void setPatient_uuid(String patient_uuid) {
		this.patient_uuid = patient_uuid;
	}

	public String getEncounter_uuid() {
		return encounter_uuid;
	}

	public void setEncounter_uuid(String encounter_uuid) {
		this.encounter_uuid = encounter_uuid;
	}

	public String getIs_send_to_central() {
		return is_send_to_central;
	}

	public void setIs_send_to_central(String is_send_to_central) {
		this.is_send_to_central = is_send_to_central;
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
