package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

import org.openmrs.module.sharedhealthrecord.UBSMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescribedMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescription;

public interface UBSPrescriptionDAO {
	
	List<UBSMedicines> getMedicineList(String type);
	
	UBSPrescription saveorUpdate(UBSPrescription ubsPrescription);
	
	UBSPrescription findById(int id);
	
	UBSPrescribedMedicines findPrescribedMedicineById (int id);
	
	UBSPrescription findPrescriptionByVisitId(String visitUuid);
	
	List<UBSMedicines> getMedicineListAll();




}
