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
package org.openmrs.module.sharedhealthrecord.api.db;

import java.util.List;

import org.openmrs.module.sharedhealthrecord.UBSDataExtract;
import org.openmrs.module.sharedhealthrecord.api.SharedHealthRecordService;
import org.openmrs.module.sharedhealthrecord.dto.UBSCommonDTO;

/**
 *  Database methods for {@link SharedHealthRecordService}.
 */
public interface SharedHealthRecordDAO {
	
	/*
	 * Add DAO methods here
	 */
	
	public boolean ubsSaveExtractedFieldsToTable(UBSDataExtract dto, String tableName);
	
	public boolean deleteExtractedFieldsByEncounterUuid(String encounterUuid,String tableName);
	
	public boolean checkIsProviderIsLabTechnicin(String uuid);
	
	public List<UBSCommonDTO>getChildInfo(String encounter_uuid);
	
	public int insertIntoChildInfoTable(UBSCommonDTO dto);

}