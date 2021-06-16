package org.openmrs.module.sharedhealthrecord;

import java.io.Serializable;

import org.openmrs.BaseOpenmrsData;

public class UBSCommoditiesDistributeDetails extends BaseOpenmrsData implements Serializable {

	private static final long serialVersionUID = 1L;

	private int distributeDetailsId;
	
	private String commoditiesName;
	
	private int commoditiesId;
	
	private int quantity;
	
	private UBSCommoditiesDistribution ubsCommoditiesDistribution;

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

	public UBSCommoditiesDistribution getUbsCommoditiesDistribution() {
		return ubsCommoditiesDistribution;
	}

	public void setUbsCommoditiesDistribution(
			UBSCommoditiesDistribution ubsCommoditiesDistribution) {
		this.ubsCommoditiesDistribution = ubsCommoditiesDistribution;
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
