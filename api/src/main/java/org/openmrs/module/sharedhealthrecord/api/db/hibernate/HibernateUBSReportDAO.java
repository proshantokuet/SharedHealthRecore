package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.List;

import org.hibernate.SessionFactory;
import org.openmrs.module.sharedhealthrecord.api.db.UBSReportDAO;

public class HibernateUBSReportDAO implements UBSReportDAO {

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
	public List<Object> getSelectedReport(String startDate, String endDate,
			String reportName) {
		// TODO Auto-generated method stub
		return null;
	}

}
