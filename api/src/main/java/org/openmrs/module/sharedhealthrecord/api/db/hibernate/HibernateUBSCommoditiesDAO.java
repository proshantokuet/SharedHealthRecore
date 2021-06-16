package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistributeDetails;
import org.openmrs.module.sharedhealthrecord.UBSCommoditiesDistribution;
import org.openmrs.module.sharedhealthrecord.UBSPrescription;
import org.openmrs.module.sharedhealthrecord.UBSUniqueIdGenerator;
import org.openmrs.module.sharedhealthrecord.api.db.UBSCommoditiesDAO;

public class HibernateUBSCommoditiesDAO implements UBSCommoditiesDAO {
	
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
	public UBSUniqueIdGenerator getLastEntry(String date) {
		// TODO Auto-generated method stub
		@SuppressWarnings("unchecked")

		UBSUniqueIdGenerator IdGenerator = new UBSUniqueIdGenerator();
		IdGenerator.setGenerateId(0);
		String sql = "SELECT generate_id FROM openmrs.ubs_id_generate where Date(date_created) = :date order by generate_id desc limit 1";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		
		List<Integer> data = query.setString("date", date).list();
		for (Integer newslip : data) {
			IdGenerator.setGenerateId(newslip.intValue());
		}
		
		return IdGenerator;
	}

	@Override
	public UBSUniqueIdGenerator saveOrUpdate(UBSUniqueIdGenerator uniqueId) {
		// TODO Auto-generated method stub
		sessionFactory.getCurrentSession().saveOrUpdate(uniqueId);
		return uniqueId;
	}

	@Override
	public UBSCommoditiesDistribution saveOrUpdate(
			UBSCommoditiesDistribution ubsCommoditiesDistribution) {
		// TODO Auto-generated method stub
		sessionFactory.getCurrentSession().saveOrUpdate(ubsCommoditiesDistribution);
		return ubsCommoditiesDistribution;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UBSCommoditiesDistribution findByDistributeId(int distributeId) {
		// TODO Auto-generated method stub
		List<UBSCommoditiesDistribution> ubsCommoditiesDistributions = sessionFactory
				.getCurrentSession()
				.createQuery(
						"from UBSCommoditiesDistribution where distributeId = :id").setInteger("id", distributeId).list();
		if(ubsCommoditiesDistributions.size() > 0) {
			return ubsCommoditiesDistributions.get(0);
		}
		else return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UBSCommoditiesDistributeDetails findByDistributeDetailsId(
			int distributeDetailsId) {
		// TODO Auto-generated method stub
		List<UBSCommoditiesDistributeDetails> ubsCommoditiesDistributeDetails = sessionFactory
				.getCurrentSession()
				.createQuery(
						"from UBSCommoditiesDistributeDetails where distributeDetailsId = :id").setInteger("id", distributeDetailsId).list();
		if(ubsCommoditiesDistributeDetails.size() > 0) {
			return ubsCommoditiesDistributeDetails.get(0);
		}
		else return null;
	}

}
