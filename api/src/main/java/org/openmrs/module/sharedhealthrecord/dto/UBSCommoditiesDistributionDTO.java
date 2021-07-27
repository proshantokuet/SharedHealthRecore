package org.openmrs.module.sharedhealthrecord.dto;

import java.util.Date;
import java.util.Set;

public class UBSCommoditiesDistributionDTO {

	private int distributeId;
	
	private String patientUuid;
	
	private String patientName;
	
	private String gender;
	
	private String patientAge;
	
	private String providerName;
	
	private String slipNo;
	
	private Date distributeDate;
	
	private String uuid;
	
	private Set<UBSCommoditiesDistributeDetailsDTO> ubsCommoditiesDistributeDetailsDto;

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

	public Set<UBSCommoditiesDistributeDetailsDTO> getUbsCommoditiesDistributeDetailsDto() {
		return ubsCommoditiesDistributeDetailsDto;
	}

	public void setUbsCommoditiesDistributeDetailsDto(
			Set<UBSCommoditiesDistributeDetailsDTO> ubsCommoditiesDistributeDetailsDto) {
		this.ubsCommoditiesDistributeDetailsDto = ubsCommoditiesDistributeDetailsDto;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	
}
