package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.module.sharedhealthrecord.api.db.UBSReportDAO;

public class HibernateUBSReportDAO implements UBSReportDAO {
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
	public List<Object[]> getSelectedReport(String startDate, String endDate,
			String reportName) {
		
		String sql = "CALL "+reportName+"(:startdate,:enddate)";
		log.error("SQL print " + sql);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		
		List<Object[]> data = query.setString("startdate", startDate)
							.setString("enddate", endDate).list();
		log.error("data size " + data.size());
		return data;
	}

}
