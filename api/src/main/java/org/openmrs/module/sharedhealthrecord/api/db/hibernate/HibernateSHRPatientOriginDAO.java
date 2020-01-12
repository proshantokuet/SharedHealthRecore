package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.module.sharedhealthrecord.SHRExternalPatient;
import org.openmrs.module.sharedhealthrecord.SHRPatientOrigin;
import org.openmrs.module.sharedhealthrecord.SHRPatientVisit;
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
	public List<SHRPatientOrigin> getpatientOriginByOriginName(String originName, String actionType) {
		String patientOriginSql = ""
				+ "SELECT DISTINCT ep.patient_uuid, "
				+ "       ep.action_type, "
				+ "       po.patient_origin, "
				+ "       ep.encounter_uuid "
				+ "FROM   shr_external_patient AS ep "
				+ "       JOIN shr_patient_origin po "
				+ "         ON ep.patient_uuid = po.patient_uuid "
				+ "WHERE  ep.action_type = '"+actionType+"' "
				+ "       AND po.patient_origin = '"+originName+"' AND ep.is_send_to_central = '1'";
		
		List<SHRPatientOrigin> shrPatientOrigins = new ArrayList<SHRPatientOrigin>();
		
		try {
			shrPatientOrigins = sessionFactory
					.getCurrentSession()
					.createSQLQuery(patientOriginSql)
					.addScalar("patient_uuid", StandardBasicTypes.STRING)
					.addScalar("action_type", StandardBasicTypes.STRING)
					.addScalar("patient_origin", StandardBasicTypes.STRING)
					.addScalar("encounter_uuid", StandardBasicTypes.STRING)
					.setResultTransformer(
							new AliasToBeanResultTransformer(
									SHRPatientOrigin.class)).list();
			if (shrPatientOrigins.size() > 0) {
				return shrPatientOrigins;
			} 
			else {
				return null;
			}
		} 
		catch (Exception e) {
			return null;
		}
	}
}
