package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.SHRPatientOrigin;
import org.openmrs.module.sharedhealthrecord.api.db.SHRPatientOriginDAO;

public class HibernateSHRPatientOriginDAO implements SHRPatientOriginDAO {
	
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
	public SHRPatientOrigin savePatientOrigin(SHRPatientOrigin shrPatientOrigin) {
		// TODO Auto-generated method stub
		sessionFactory.getCurrentSession().saveOrUpdate(shrPatientOrigin);
		return shrPatientOrigin;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SHRPatientOrigin getpatientOriginByPatientuuid(String patientUuid) {
		List <SHRPatientOrigin> shrPatientOrigins = sessionFactory.getCurrentSession()
				.createQuery("from SHRPatientOrigin where patient_uuid = :patientid")
		        .setString("patientid", patientUuid).list();
		if (shrPatientOrigins.size() != 0) {
			return shrPatientOrigins.get(0); 
		} else {
			return null;
		}
	}

}
