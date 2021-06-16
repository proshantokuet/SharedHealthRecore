package org.openmrs.module.sharedhealthrecord.api.db;

import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistributeDetails;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistribution;
import org.openmrs.module.sharedhealthrecord.UBSUniqueIdGenerator;

public interface UBSCommoditiesDAO {

	UBSUniqueIdGenerator getLastEntry(String date);
	
	UBSUniqueIdGenerator saveOrUpdate(UBSUniqueIdGenerator uniqueId);
	
	UBSCommoditiesDistribution saveOrUpdate(UBSCommoditiesDistribution ubsCommoditiesDistribution);
	
	UBSCommoditiesDistribution findByDistributeId(int distributeId);
	
	UBSCommoditiesDistributeDetails findByDistributeDetailsId(int distributeDetailsId);
}
