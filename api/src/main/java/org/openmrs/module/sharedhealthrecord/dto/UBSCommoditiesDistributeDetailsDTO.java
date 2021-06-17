package org.openmrs.module.sharedhealthrecord.dto;

public class UBSCommoditiesDistributeDetailsDTO {

	private int distributeDetailsId;
	
	private String commoditiesName;
	
	private int commoditiesId;
	
	private int quantity;
	
	private UBSCommoditiesDistributionDTO ubsCommoditiesDistributionDto;

	public int getDistributeDetailsId() {
		return distributeDetailsId;
	}

	public void setDistributeDetailsId(int distributeDetailsId) {
		this.distributeDetailsId = distributeDetailsId;
	}

	public String getCommoditiesName() {
		return commoditiesName;
	}

	public void setCommoditiesName(String commoditiesName) {
		this.commoditiesName = commoditiesName;
	}

	public int getCommoditiesId() {
		return commoditiesId;
	}

	public void setCommoditiesId(int commoditiesId) {
		this.commoditiesId = commoditiesId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public UBSCommoditiesDistributionDTO getUbsCommoditiesDistributionDto() {
		return ubsCommoditiesDistributionDto;
	}

	public void setUbsCommoditiesDistributionDto(
			UBSCommoditiesDistributionDTO ubsCommoditiesDistributionDto) {
		this.ubsCommoditiesDistributionDto = ubsCommoditiesDistributionDto;
	}
}
