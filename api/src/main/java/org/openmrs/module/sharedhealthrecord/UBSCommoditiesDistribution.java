package org.openmrs.module.sharedhealthrecord;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.openmrs.BaseOpenmrsData;

public class UBSCommoditiesDistribution extends BaseOpenmrsData implements Serializable {

	private static final long serialVersionUID = 1L;

	private int distributeId;
	
	private String patientUuid;
	
	private String patientName;
	
	private String gender;
	
	private String patientAge;
	
	private String providerName;
	
	private String slipNo;
	
	private Date distributeDate;
	
	private Set<UBSCommoditiesDistributeDetails> ubsCommoditiesDistributeDetails;

	public int getDistributeId() {
		return distributeId;
	}

	public void setDistributeId(int distributeId) {
		this.distributeId = distributeId;
	}

	public String getPatientUuid() {
		return patientUuid;
	}

	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getPatientAge() {
		return patientAge;
	}

	public void setPatientAge(String patientAge) {
		this.patientAge = patientAge;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getSlipNo() {
		return slipNo;
	}

	public void setSlipNo(String slipNo) {
		this.slipNo = slipNo;
	}

	public Date getDistributeDate() {
		return distributeDate;
	}

	public void setDistributeDate(Date distributeDate) {
		this.distributeDate = distributeDate;
	}

	public Set<UBSCommoditiesDistributeDetails> getUbsCommoditiesDistributeDetails() {
		return ubsCommoditiesDistributeDetails;
	}

	public void setUbsCommoditiesDistributeDetails(
			Set<UBSCommoditiesDistributeDetails> ubsCommoditiesDistributeDetails) {
		this.ubsCommoditiesDistributeDetails = ubsCommoditiesDistributeDetails;
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
