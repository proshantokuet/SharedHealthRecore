package org.openmrs.module.sharedhealthrecord.api;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistributeDetails;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistribution;
import org.openmrs.module.sharedhealthrecord.UBSUniqueIdGenerator;
import org.openmrs.module.sharedhealthrecord.dto.UBSCommoditiesReportDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UBSCommoditiesService extends OpenmrsService {

	UBSUniqueIdGenerator getLastEntry(String date);
	
	UBSUniqueIdGenerator saveOrUpdate(UBSUniqueIdGenerator uniqueId);
	
	UBSCommoditiesDistribution saveOrUpdate(UBSCommoditiesDistribution ubsCommoditiesDistribution);
	
	UBSCommoditiesDistribution findByDistributeId(int distributeId);
	
	UBSCommoditiesDistributeDetails findByDistributeDetailsId(int distributeDetailsId);
	
	List<UBSCommoditiesReportDTO> findAllByPatientUuid(String patientUuid);
	
	List<UBSCommoditiesDistribution> getAllDistributionList(int id);
	
	UBSCommoditiesDistribution findByDistributeUuid(String uuid);
	
}
