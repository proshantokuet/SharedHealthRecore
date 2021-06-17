package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.UBSMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescribedMedicines;
import org.openmrs.module.sharedhealthrecord.UBSPrescription;
import org.openmrs.module.sharedhealthrecord.api.db.UBSPrescriptionDAO;

public class HibernateUBSPrescriptionDAO implements UBSPrescriptionDAO {

protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
	    this.sessionFactory = sessionFactory;
    }
    
	/**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
	    return sessionFactory;
    }

	@Override
	public UBSPrescription saveorUpdate(UBSPrescription ubsPrescription) {
		// TODO Auto-generated method stub
		sessionFactory.getCurrentSession().saveOrUpdate(ubsPrescription);
		return ubsPrescription;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UBSMedicines> getMedicineList(String type) {
		// TODO Auto-generated method stub
		List<UBSMedicines> ubsMedicines = sessionFactory
				.getCurrentSession()
				.createQuery(
						"from UBSMedicines where category = :categoryName and voided = 0 order by name ASC").setString("categoryName", type).list();
		return ubsMedicines;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UBSPrescription findById(int id) {
		// TODO Auto-generated method stub
		List<UBSPrescription> ubsPrescriptions = sessionFactory
				.getCurrentSession()
				.createQuery(
						"from UBSPrescription where prescriptionId = :id").setInteger("id", id).list();
		if(ubsPrescriptions.size() > 0) {
			return ubsPrescriptions.get(0);
		}
		else return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UBSPrescribedMedicines findPrescribedMedicineById(int id) {
		// TODO Auto-generated method stub
		List<UBSPrescribedMedicines> ubsPrescribedMedicine = sessionFactory
				.getCurrentSession()
				.createQuery(
						"from UBSPrescribedMedicines where pmId = :id").setInteger("id", id).list();
		if(ubsPrescribedMedicine.size() > 0) {
			return ubsPrescribedMedicine.get(0);
		}
		else return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UBSPrescription findPrescriptionByVisitId(String visitUuid) {
		// TODO Auto-generated method stub
		List<UBSPrescription> ubsPrescriptions = sessionFactory
				.getCurrentSession()
				.createQuery(
						"from UBSPrescription where visitUuid = :id order by prescriptionId desc").setString("id", visitUuid).list();
		if(ubsPrescriptions.size() > 0) {
			return ubsPrescriptions.get(0);
		}
		else return null;
	}
	
}