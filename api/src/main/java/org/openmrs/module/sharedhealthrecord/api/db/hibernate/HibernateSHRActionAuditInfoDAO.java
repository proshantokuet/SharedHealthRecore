package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.api.db.SHRActionAuditInfoDAO;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;
import org.openmrs.module.sharedhealthrecord.domain.MoneyReceiptDTO;

public  class HibernateSHRActionAuditInfoDAO implements SHRActionAuditInfoDAO{
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
	public List<EventRecordsDTO> getEventRecords(String type) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT id as id, uuid as uuid, title as title, "
				+ " `timestamp` as timestamp, uri as uri, `object` as object, "
				+ " category as category, date_created as date_created, "
				+ " tags as tags "
				+ "FROM openmrs.event_records "
				+ "WHERE title='"+type+"'";
		try{
			List<EventRecordsDTO> records = sessionFactory.getCurrentSession().createSQLQuery(sql)
					.addScalar("id",StandardBasicTypes.INTEGER)
					.addScalar("uuid",StandardBasicTypes.STRING)
					.addScalar("title",StandardBasicTypes.STRING)
					.addScalar("timestamp",StandardBasicTypes.STRING)
					.addScalar("uri",StandardBasicTypes.STRING)
					.addScalar("object",StandardBasicTypes.STRING)
					.addScalar("category",StandardBasicTypes.STRING)
					.addScalar("date_created",StandardBasicTypes.STRING)
					.addScalar("tags",StandardBasicTypes.STRING).list();
			
			return records;
		}
		catch(Exception e){
			return null;
		}
		
	}

	@Override
	public List<MoneyReceiptDTO> getMoneyReceipt() {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT mid as mid, patient_uuid as patient_uuid, "
				+ " `timestamp` as `timestamp` "
				+ " FROM openmrs.psi_money_receipt";
		try{
			List<MoneyReceiptDTO> receipts = sessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.addScalar("mid",StandardBasicTypes.INTEGER)
					.addScalar("patient_uuid",StandardBasicTypes.STRING)
					.addScalar("timestamp",StandardBasicTypes.STRING)
					.list();
			return receipts;
		}catch(Exception e){
			return null;
		}
		
	}

	@Override
	public List<MoneyReceiptDTO> getMoneyReceipt(String timestamp) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT mid as mid, patient_uuid as patient_uuid, "
				+ " `timestamp` as `timestamp` "
				+ " FROM openmrs.psi_money_receipt"
				+ " WHERE timestamp >= '"+timestamp+"' "
				+ " LIMIT 500 ";
		try{
			List<MoneyReceiptDTO> receipts = sessionFactory.getCurrentSession()
					.createSQLQuery(sql)
					.addScalar("mid",StandardBasicTypes.INTEGER)
					.addScalar("patient_uuid",StandardBasicTypes.STRING)
					.addScalar("timestamp",StandardBasicTypes.STRING)
					.list();
			return receipts;
		}catch(Exception e){
			return null;
		}
	}

	@Override
	public String getLastEntryForPatient() {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT last_id "
				+ "FROM openmrs.shr_action_audit_info "
				+ "WHERE record_name = 'Patient'";
		try{
			return sessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0).toString();
		}catch(Exception e){
			return "0";
		}
	}

	@Override
	public String getLastEntryForEncounter() {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT last_id "
				+ "FROM openmrs.shr_action_audit_info "
				+ "WHERE record_name = 'Encounter'";
		try{
			return sessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0).toString();
		}catch(Exception e){
			return "0";
		}
	}

	@Override
	public String getLastEntryForMoneyReceipt() {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT  last_timestamp "
				+ "FROM openmrs.shr_action_audit_info "
				+ "WHERE record_name = 'Money Receipt'";
		
		try{
			return sessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0).toString();
		}catch(Exception e){
			return "0";
		}
		
	}

	@Override
	public String updateAuditPatient(String last_id) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "UPDATE openmrs.shr_action_audit_info "
				+ "SET last_id = '"+last_id+"' "
				+ "WHERE record_name = 'Patient'";
		try{
			return sessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0).toString();
		}catch(Exception e){
			return "0";
		}
		
	}

	@Override
	public String updateAuditEncounter(String last_id) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "UPDATE openmrs.shr_action_audit_info "
				+ "SET last_id = '"+last_id+"' "
				+ "WHERE record_name = 'Encounter'";
		try{
			return sessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0).toString();
		}catch(Exception e){
			return "0";
		}
		
	}

	@Override
	public String updateAuditMoneyReceipt(String last_timestamp) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "UPDATE openmrs.shr_action_audit_info "
				+ " 	SET last_timestamp = '"+last_timestamp+"' "
				+ "	WHERE record_name = 'Money Receipt'";
		
		try{
			return sessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0).toString();
		}catch(Exception e){
			return "0";
		}
		
	}

	@Override
	public String getTimeStampForMoneyReceipt(String mid) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT `timestamp` as `timestamp` "
				+ "FROM openmrs.psi_money_receipt "
				+ "WHERE mid = '"+mid+"' ";
		try{
		return sessionFactory.getCurrentSession().createSQLQuery(sql).list().get(0).toString();
		}
		catch(Exception e){
			return "0";
		}
	}

	@Override
	public List<EventRecordsDTO> getEventRecords(String type, String last_entry) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "SELECT id as id, uuid as uuid, title as title, "
				+ " `timestamp` as timestamp, uri as uri, `object` as object, "
				+ " category as category, date_created as date_created, "
				+ " tags as tags "
				+ " FROM openmrs.event_records "
				+ " WHERE title='"+type+"'"
				+ " AND id > '"+last_entry+"' ";
		try{
			List<EventRecordsDTO> records = sessionFactory.getCurrentSession().createSQLQuery(sql)
					.addScalar("id",StandardBasicTypes.INTEGER)
					.addScalar("uuid",StandardBasicTypes.STRING)
					.addScalar("title",StandardBasicTypes.STRING)
					.addScalar("timestamp",StandardBasicTypes.STRING)
					.addScalar("uri",StandardBasicTypes.STRING)
					.addScalar("object",StandardBasicTypes.STRING)
					.addScalar("category",StandardBasicTypes.STRING)
					.addScalar("date_created",StandardBasicTypes.STRING)
					.addScalar("tags",StandardBasicTypes.STRING).list();
			
			return records;
		}
		catch(Exception e){
			return null;
		}
	}

	

	
	
}
