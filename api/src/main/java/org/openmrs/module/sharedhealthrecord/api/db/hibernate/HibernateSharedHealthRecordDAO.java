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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.module.sharedhealthrecord.UBSDataExtract;
import org.openmrs.module.sharedhealthrecord.api.db.SharedHealthRecordDAO;
import org.openmrs.module.sharedhealthrecord.dto.UBSCommonDTO;

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
			int Status = saveDetails.setString("issue", dto.getQuestion().trim())
					   .setString("enounterUuid", dto.getEncounterUuid())
					   .setString("value", dto.getAnswer())
					   .setString("patientUuid", dto.getPatientUuid())
					   .setInteger("flag", 0).executeUpdate();
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

	@Override
	public boolean checkIsProviderIsLabTechnicin(String uuid) {
		// TODO Auto-generated method stub
		String checkProvider = ""
				+ "select "
				+ "	ur.`role` "
				+ "from "
				+ "	provider p "
				+ "join person pr on "
				+ "	p.person_id = pr.person_id "
				+ "join users u on "
				+ "	u.person_id = pr.person_id "
				+ "join user_role ur on "
				+ "	ur.user_id = u.user_id "
				+ "where "
				+ "	p.uuid = :id "
				+ "and ur.`role` = 'Lab Technician'";
		
		try{
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(checkProvider);
			List<String> data = query.setString("id", uuid).list();
			if(data.size() > 0) {
				 return true;
			}
			else return false;
			
		}catch(Exception e){
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UBSCommonDTO> getChildInfo(String encounter_uuid) {
		// TODO Auto-generated method stub
		String birthQuery = "Call sp_extract_child_info('"+encounter_uuid+"')";
				
		
		List<UBSCommonDTO> report = new ArrayList<UBSCommonDTO>();
		try{
			report = sessionFactory.getCurrentSession().createSQLQuery(birthQuery).
					addScalar("question",StandardBasicTypes.STRING).
					addScalar("answer",StandardBasicTypes.STRING).
					addScalar("patient_uuid",StandardBasicTypes.STRING).
					setResultTransformer(new AliasToBeanResultTransformer(UBSCommonDTO.class)).
					list();
			return report;
		}catch(Exception e){
			return report;
		}
	}

	@Override
	public int insertIntoChildInfoTable(UBSCommonDTO dto) {
		// TODO Auto-generated method stub
		try {
			String sql = ""
					+ "INSERT INTO openmrs.ubs_report_child_information"
					+ "(birth_weight, neonatal_sepsis, birth_asphyxia, gender, encounter_uuid, patient_uuid,voided) "
					+ "VALUES(:weight,:neonatalsepsis,:asphyxia,:gender,:encounter,:patientid,:flag);";
			log.error("sql" + sql);
			SQLQuery saveDetails = sessionFactory.getCurrentSession().createSQLQuery(sql);
			int Status = saveDetails
						.setString("weight", dto.getNewborn_weight())
					   .setString("neonatalsepsis", dto.getNeonatal_sepsis())
					   .setString("asphyxia", dto.getBirth_Ashphyxia())
					   .setString("gender", dto.getGender())
					   .setString("encounter", dto.getEncounter_uuid())
					   .setString("patientid", dto.getPatient_uuid())
					   .setInteger("flag", 0).executeUpdate();
			return Status;

		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}

	}
}