package org.openmrs.module.sharedhealthrecord.api.impl;

import java.util.List;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.sharedhealthrecord.UBSMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescribedMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescription;
import org.openmrs.module.sharedhealthrecord.api.UBSPrescriptionService;
import org.openmrs.module.sharedhealthrecord.api.db.UBSPrescriptionDAO;

public class UBSPrescriptionServiceImpl extends BaseOpenmrsService implements UBSPrescriptionService {

	private UBSPrescriptionDAO dao;
	
	public UBSPrescriptionDAO getDao() {
		return dao;
	}

	public void setDao(UBSPrescriptionDAO dao) {
		this.dao = dao;
	}

	@Override
	public List<UBSMedicines> getMedicineList() {
		// TODO Auto-generated method stub
		return dao.getMedicineList();
	}

	@Override
	public UBSPrescription saveorUpdate(UBSPrescription ubsPrescription) {
		// TODO Auto-generated method stub
		return dao.saveorUpdate(ubsPrescription);
	}

	@Override
	public UBSPrescription findById(int id) {
		// TODO Auto-generated method stub
		return dao.findById(id);
	}

	@Override
	public UBSPrescribedMedicines findPrescribedMedicineById(int id) {
		// TODO Auto-generated method stub
		return dao.findPrescribedMedicineById(id);
	}

	
}
