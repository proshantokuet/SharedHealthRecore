package org.openmrs.module.sharedhealthrecord;

import java.io.Serializable;

import org.openmrs.BaseOpenmrsObject;

public class SHRPatientOrigin  extends BaseOpenmrsObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int originId;
	
	private String patient_uuid;
	
	private String patient_origin;
	
	private String action_type;
	
	private String encounter_uuid;
	
	private Integer isSendToDHis;

	public int getOriginId() {
		return originId;
	}

	public void setOriginId(int originId) {
		this.originId = originId;
	}

	public String getPatient_uuid() {
		return patient_uuid;
	}

	public void setPatient_uuid(String patient_uuid) {
		this.patient_uuid = patient_uuid;
	}

	public String getPatient_origin() {
		return patient_origin;
	}

	public void setPatient_origin(String patient_origin) {
		this.patient_origin = patient_origin;
	}
	
	public String getAction_type() {
		return action_type;
	}

	public void setAction_type(String action_type) {
		this.action_type = action_type;
	}

	public String getEncounter_uuid() {
		return encounter_uuid;
	}

	public void setEncounter_uuid(String encounter_uuid) {
		this.encounter_uuid = encounter_uuid;
	}

	public Integer getIsSendToDHis() {
		return isSendToDHis;
	}

	public void setIsSendToDHis(Integer isSendToDHis) {
		this.isSendToDHis = isSendToDHis;
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
