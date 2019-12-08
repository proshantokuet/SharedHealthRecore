package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.openmrs.module.sharedhealthrecord.SHRActionErrorLog;
import org.openmrs.module.sharedhealthrecord.api.db.SHRActionErrorLogDAO;

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
		String sql = "";
		return null;
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

}
