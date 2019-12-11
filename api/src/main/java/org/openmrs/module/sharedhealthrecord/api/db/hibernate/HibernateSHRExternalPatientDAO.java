package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.api.db.SHRExternalPatientDAO;

public class HibernateSHRExternalPatientDAO  implements SHRExternalPatientDAO {
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
	public SHRExternalPatient saveExternalPatient(SHRExternalPatient externalPatient) {
		// TODO Auto-generated method stub
		sessionFactory.getCurrentSession().saveOrUpdate(externalPatient);
		return externalPatient;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SHRExternalPatient findExternalPatientByPatientUUid(String patientUuid) {
		List <SHRExternalPatient> shrExternalPatient = sessionFactory.getCurrentSession()
				.createQuery("from SHRExternalPatient where patient_uuid = :patientid and action_type = 'patient'")
		        .setString("patientid", patientUuid).list();
		if (shrExternalPatient.size() != 0) {
			return shrExternalPatient.get(0); 
		} else {
			return null;
		}
	}
}
