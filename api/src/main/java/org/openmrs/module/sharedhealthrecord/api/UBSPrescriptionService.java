package org.openmrs.module.sharedhealthrecord.api;

import java.util.List;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.sharedhealthrecord.UBSMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescribedMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescription;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UBSPrescriptionService extends OpenmrsService {

	List<UBSMedicines> getMedicineList();
	
	UBSPrescription saveorUpdate(UBSPrescription ubsPrescription);
	
	UBSPrescription findById(int id);
	
	UBSPrescribedMedicines findPrescribedMedicineById (int id);
}
 