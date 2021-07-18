package org.openmrs.module.sharedhealthrecord.dto;

public class UBSCommonDTO {
	
	private String question;
	
	private String answer;
	
	private String patient_uuid;
	
	private String birth_Ashphyxia;
	
	private String neonatal_sepsis;
	
	private String gender;
	
	private String newborn_weight;
	
	private String encounter_uuid;

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getPatient_uuid() {
		return patient_uuid;
	}

	public void setPatient_uuid(String patient_uuid) {
		this.patient_uuid = patient_uuid;
	}

	public String getBirth_Ashphyxia() {
		return birth_Ashphyxia;
	}

	public void setBirth_Ashphyxia(String birth_Ashphyxia) {
		this.birth_Ashphyxia = birth_Ashphyxia;
	}

	public String getNeonatal_sepsis() {
		return neonatal_sepsis;
	}

	public void setNeonatal_sepsis(String neonatal_sepsis) {
		this.neonatal_sepsis = neonatal_sepsis;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getNewborn_weight() {
		return newborn_weight;
	}

	public void setNewborn_weight(String newborn_weight) {
		this.newborn_weight = newborn_weight;
	}

	public String getEncounter_uuid() {
		return encounter_uuid;
	}

	public void setEncounter_uuid(String encounter_uuid) {
		this.encounter_uuid = encounter_uuid;
	}
	
}
