package org.openmrs.module.sharedhealthrecord.domain;


public class BahmniDiagnoses {
	
	private CodedAnswer codedAnswer;
	
	private String order;
	
	private String certainty;
	
	private String existingObs;
	
	private String diagnosisDateTime;
	
	private DiagnosisStatusConcept diagnosisStatusConcept;
	
	private String comments;

	public CodedAnswer getCodedAnswer() {
		return codedAnswer;
	}

	public void setCodedAnswer(CodedAnswer codedAnswer) {
		this.codedAnswer = codedAnswer;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getCertainty() {
		return certainty;
	}

	public void setCertainty(String certainty) {
		this.certainty = certainty;
	}

	public String getExistingObs() {
		return existingObs;
	}

	public void setExistingObs(String existingObs) {
		this.existingObs = existingObs;
	}

	public String getDiagnosisDateTime() {
		return diagnosisDateTime;
	}

	public void setDiagnosisDateTime(String diagnosisDateTime) {
		this.diagnosisDateTime = diagnosisDateTime;
	}

	public DiagnosisStatusConcept getDiagnosisStatusConcept() {
		return diagnosisStatusConcept;
	}

	public void setDiagnosisStatusConcept(
			DiagnosisStatusConcept diagnosisStatusConcept) {
		this.diagnosisStatusConcept = diagnosisStatusConcept;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
}
