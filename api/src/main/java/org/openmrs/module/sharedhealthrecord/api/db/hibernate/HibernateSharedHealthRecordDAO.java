/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.sharedhealthrecord.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.openmrs.module.sharedhealthrecord.UBSDataExtract;
import org.openmrs.module.sharedhealthrecord.api.db.SharedHealthRecordDAO;

/**
 * It is a default implementation of  {@link SharedHealthRecordDAO}.
 */
public class HibernateSharedHealthRecordDAO implements SharedHealthRecordDAO {
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
	public boolean ubsSaveExtractedFieldsToTable(UBSDataExtract dto,String tableName) {
		try {
			String sql = ""
					+ "INSERT INTO openmrs." +tableName.trim()
					+ "(issues, encounter_uuid, value, patient_uuid, voided) "
					+ "VALUES(:issue,:enounterUuid,:value,:patientUuid,:flag);";
			
			SQLQuery saveDetails = sessionFactory.getCurrentSession().createSQLQuery(sql);
			int Status = saveDetails.setString("issue", dto.getQuestion())
					   .setString("enounterUuid", dto.getEncounterUuid())
					   .setString("value", dto.getAnswer())
					   .setString("patientUuid", dto.getPatientUuid())
					   .setInteger("flag", 1).executeUpdate();
			if (Status == 1) return true;
			else return false;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}

	}

	@Override
	public boolean deleteExtractedFieldsByEncounterUuid(String encounterUuid,
			String tableName) {
		// TODO Auto-generated method stub
		String countSql = "	select Count(*) from openmrs."+tableName.trim()+" where encounter_uuid = '"+encounterUuid+"'";
		log.error("countSql" + countSql);
		String Deletesql = ""
				+ " DELETE FROM openmrs." +tableName.trim()
				+ " WHERE encounter_uuid = '"+encounterUuid+"' ";
		log.error("Deletesql" + Deletesql);
		
		try{
			String CountValue = sessionFactory.getCurrentSession().createSQLQuery(countSql).list().get(0).toString();
			if(Integer.parseInt(CountValue) > 0) {
				 int status = sessionFactory.getCurrentSession().
						createSQLQuery(Deletesql).executeUpdate();
			}
			return true;
			
		}catch(Exception e){
			return false;
		}	
	}
}