package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.api.db.SHRActionErrorLogDAO;
import org.openmrs.module.sharedhealthrecord.domain.EventRecordsDTO;

public class HibernateSHRActionErrorLogDAO implements SHRActionErrorLogDAO {
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
	public void insertErrorLog(SHRActionErrorLog log) {
		// TODO Auto-generated method stub

		sessionFactory.getCurrentSession().saveOrUpdate(log);
	}

	@Override
	public List<SHRActionErrorLog> get_list_by_Action_type(String action_type) {
		// TODO Auto-generated method stub
		//Query
		List<SHRActionErrorLog> ret = new ArrayList<SHRActionErrorLog>();
		String sql = ""
				+ "SELECT eid as eid, action_type as action_type, "
				+ "error_message as error_message, uuid as uuid, "
				+" voided as voided, sent_status as sent_status "
				+ "FROM openmrs.shr_action_error_log "
				+ "WHERE action_type = '"+action_type+"'";
		
		ret = sessionFactory.getCurrentSession().
				createSQLQuery(sql)
				.addScalar("eid",StandardBasicTypes.INTEGER)
					.addScalar("action_type",StandardBasicTypes.STRING)
					.addScalar("error_message",StandardBasicTypes.STRING)
					.addScalar("uuid",StandardBasicTypes.STRING)
					.addScalar("voided",StandardBasicTypes.INTEGER)
					.addScalar("sent_status",StandardBasicTypes.INTEGER)
					.setResultTransformer(new AliasToBeanResultTransformer(SHRActionErrorLog.class)).
					list();
		return ret;
	}
	@Override
	public String delete_by_type_and_id(String action_type,
			String mid) {
		// TODO Auto-generated method stub
		String sql = ""
				+ " DELETE FROM openmrs.shr_action_error_log "
				+ " WHERE action_type = '"+action_type+"' "
				+ " AND id='"+mid+"' ";
		
		try{
			return sessionFactory.getCurrentSession().
				createSQLQuery(sql).list().get(0).toString();
		}catch(Exception e){
			
		}
		return "";
	}

	@Override
	public void testInsert() {
		// TODO Auto-generated method stub
		String sql = "INSERT into openmrs.shr_action_error_log values(1,'error','I am hitting',NULL,NULL)";
		
		try{
			 sessionFactory.getCurrentSession().
				createSQLQuery(sql).list().get(0).toString();
		}catch(Exception e){
			
		}
		
	}

	@Override
	public String delete_by_type_and_uuid(String action_type, String uuid) {
		// TODO Auto-generated method stub
		String sql = ""
				+ " DELETE FROM openmrs.shr_action_error_log "
				+ " WHERE action_type = '"+action_type+"' "
				+ " AND uuid='"+uuid+"' ";
		
		try{
			return Integer.toString(sessionFactory.getCurrentSession().
				createSQLQuery(sql).executeUpdate());
		}catch(Exception e){
			return e.toString();
		}
		
	}

	@Override
	public String failedUpdate(String action_type, String uuid) {
		// TODO Auto-generated method stub
				String sql = ""
						+ " UPDATE openmrs.shr_action_error_log "
						+" SET voided = 2 "
						+ " WHERE action_type = '"+action_type+"' "
						+ " AND uuid='"+uuid+"' ";
				
				try{
					return Integer.toString(sessionFactory.getCurrentSession().
						createSQLQuery(sql).executeUpdate());
				}catch(Exception e){
					return e.toString();
				}
	}

	@Override
	public String updateSentStatus(int eid, int sent_status) {
		// TODO Auto-generated method stub
		String sql = ""
				+ "UPDATE openmrs.shr_action_error_log "
				+ "SET sent_status = '"+sent_status+"' "
				+ "WHERE eid = '"+eid+"'";
		try{
			return Integer.toString(sessionFactory.getCurrentSession().
				createSQLQuery(sql).executeUpdate());
		}catch(Exception e){
			return e.toString();
		}
	}

}
