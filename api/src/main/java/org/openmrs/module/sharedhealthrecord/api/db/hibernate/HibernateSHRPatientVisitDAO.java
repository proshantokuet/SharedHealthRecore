package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.module.sharedhealthrecord.SHRPatientVisit;
import org.openmrs.module.sharedhealthrecord.api.db.SHRPatientVisitDAO;

public class HibernateSHRPatientVisitDAO implements SHRPatientVisitDAO {
	
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
	public SHRPatientVisit savePatientVisit(SHRPatientVisit shrPatientVisit) {
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String startDate = formatter.format(shrPatientVisit.getDate_started());
		String endDate = "";
		if (shrPatientVisit.getDate_stopped() != null) {
			 String endDateFormat = formatter.format(shrPatientVisit.getDate_stopped());
			 endDate = "'"+endDateFormat+"'";
		}
		else {
			 endDate = null;
		}
		String date_created = formatter.format(new Date());
		
		String visitSavingSql = ""
				+ "INSERT INTO visit "
				+ "(patient_id, visit_type_id, date_started, date_stopped,location_id, uuid,creator,date_created) "
				+ "VALUES("+ shrPatientVisit.getPatient_id() +","+shrPatientVisit.getVisit_type_id() +", '"+ startDate +"',"+ endDate +", "+ shrPatientVisit.getLocation_id() +",'"+ shrPatientVisit.getUuid() +"',0,'"+date_created+"');";
		SQLQuery visit = sessionFactory.getCurrentSession().createSQLQuery(visitSavingSql);
		int Status = visit.executeUpdate();
		if (Status == 1) {
			shrPatientVisit.setSuccessfull(true);
		}
		else {
			shrPatientVisit.setSuccessfull(false);
		}
		return shrPatientVisit;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SHRPatientVisit getPatientVisitByVisitUuid(String visitUuid) {
		// TODO Auto-generated method stub
		String sql = "select v.visit_type_id,v.date_started,v.date_stopped as stopDatetime,v.location_id,v.patient_id,v.uuid from visit v  where v.uuid = '"+visitUuid+"'";
		List<SHRPatientVisit> visitList = new ArrayList<SHRPatientVisit>();
		try {
			visitList = sessionFactory.getCurrentSession().createSQLQuery(sql).addScalar("visit_type_id", StandardBasicTypes.INTEGER)
					.addScalar("date_started", StandardBasicTypes.TIMESTAMP).addScalar("stopDatetime", StandardBasicTypes.STRING)
					.addScalar("location_id", StandardBasicTypes.INTEGER).addScalar("patient_id", StandardBasicTypes.INTEGER)
					.addScalar("uuid", StandardBasicTypes.STRING).setResultTransformer(new AliasToBeanResultTransformer(SHRPatientVisit.class)).
					list();
			if (visitList.size() > 0) {
				return visitList.get(0);
			}
			else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public SHRPatientVisit getPatientIdByPatientUuid(String patientUuid) {
		// TODO Auto-generated method stub
		String patientSql = "select person_id from person where uuid = '"+patientUuid+"'";
		List<SHRPatientVisit> patientVisit = new ArrayList<SHRPatientVisit>();
		try {
			patientVisit = sessionFactory.getCurrentSession().createSQLQuery(patientSql).addScalar("person_id", StandardBasicTypes.INTEGER).setResultTransformer(new AliasToBeanResultTransformer(SHRPatientVisit.class)).
					list();
			
			if (patientVisit.size() > 0) {
				return patientVisit.get(0);
			}
			else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

}
