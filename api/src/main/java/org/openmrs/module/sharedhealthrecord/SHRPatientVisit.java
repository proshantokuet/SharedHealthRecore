package org.openmrs.module.sharedhealthrecord;

import java.io.Serializable;
import java.util.Date;

import org.openmrs.BaseOpenmrsObject;

public class SHRPatientVisit extends BaseOpenmrsObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String visitType;
	
	private Date date_started;
	
	private Date date_stopped;
	
	private String location;
	
	private  String Patient_uuid;
	
	private int visit_type_id;
	
	private int location_id;
	
	private int patient_id;
	
	private boolean successfull;
	
	
	public String getVisitType() {
		return visitType;
	}

	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}

	public Date getDate_started() {
		return date_started;
	}

	public void setDate_started(Date date_started) {
		this.date_started = date_started;
	}

	public Date getDate_stopped() {
		return date_stopped;
	}

	public void setDate_stopped(Date date_stopped) {
		this.date_stopped = date_stopped;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getPatient_uuid() {
		return Patient_uuid;
	}

	public void setPatient_uuid(String patient_uuid) {
		Patient_uuid = patient_uuid;
	}

	public int getVisit_type_id() {
		return visit_type_id;
	}

	public void setVisit_type_id(int visit_type_id) {
		this.visit_type_id = visit_type_id;
	}

	public int getLocation_id() {
		return location_id;
	}

	public void setLocation_id(int location_id) {
		this.location_id = location_id;
	}

	public int getPatient_id() {
		return patient_id;
	}

	public void setPatient_id(int patient_id) {
		this.patient_id = patient_id;
	}

	public boolean isSuccessfull() {
		return successfull;
	}

	public void setSuccessfull(boolean successfull) {
		this.successfull = successfull;
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
