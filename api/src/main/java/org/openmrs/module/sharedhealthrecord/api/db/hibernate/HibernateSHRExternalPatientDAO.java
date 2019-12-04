package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.type.StandardBasicTypes;
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
	public List<SHRExternalPatient> findByPatientUuid(String patientUuid,String type) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT epid as epid,action_type as action_type, "
				+ "patient_uuid as patient_uuid,is_send_to_central as is_send_to_central, "
				+ "uuid as uuid "
				+ "FROM openmrs.shr_external_patient "
				+ "WHERE patient_uuid = '"+patientUuid+"' and action_type='"+type+"' ";
//				+ " and is_send_to_central='"+is_sent+"' ";
		try{
			List<SHRExternalPatient> externalPatients = new ArrayList<SHRExternalPatient>();
			externalPatients = sessionFactory.getCurrentSession().createSQLQuery(sql)
					.addScalar("epid",StandardBasicTypes.INTEGER)
					.addScalar("action_type",StandardBasicTypes.STRING)
					.addScalar("patient_uuid",StandardBasicTypes.STRING)
					.addScalar("is_send_to_central",StandardBasicTypes.STRING)
					.addScalar("uuid",StandardBasicTypes.STRING).list();
			
			return externalPatients;
					
		}catch(Exception e){
			return null;
		}
		
	}

	

	
}
