<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>


<style>
/* .dataTables_wrapper .dt-buttons {
  float:none;  
  text-align:right;
  position: static;
  top: -26px;
  margin-left: 1036px
}
.dataTables_wrapper .dataTables_filter {
    float: left;
    text-align: right;
} */
/* table.dataTable tbody th, table.dataTable tbody td {
     padding: 0px 0px; 
} */

#loader{
	background-color:#fff;
	 padding: 15px;
  	 position: absolute;
     top: 50%;
     left: 50%;
     opacity:1.2;
   	-ms-transform: translateX(-50%) translateY(-50%);
  	-webkit-transform: translate(-50%,-50%);
  	transform: translate(-50%,-50%);
}
#tabs{
	display:none;
}
</style>
<div id="loader"> 
			<img width="50px" height="50px" src="<c:url value="/moduleResources/sharedhealthrecord/images/ajax-loading.gif"/>">
	</div>
<div id="tabs">	
<div class="form-content">
    <form:form id="patienterrorvisualize">
        	<div class="row">
	              	<div class="col-md-3">
	               		<div class="form-group">
	                  		<label for="reportName">Report Name</label> <br />
	                  		<select name="reportName" id="reportName"  class="form-control selcls" required="true">
	                  			<option value="SP_ubs_acute_health_condition">Acute Health Condition</option>
	                  			<option value="SP_ubs_communicable_disease">Communicable Diseases</option>
	                  			<option value="SP_ubs_child_vaccination">EPI and Vaccination</option>
	                  			<option value="SP_ubs_injuries">Injuries</option>
	                  			<option value="SP_ubs_inPatient_service">In Patient Services</option>
	                  			<option value="SP_ubs_medical_and_other_distribute">Medical And Other Distributes</option>
	                  			<option value="SP_ubs_medical_investigation">Medical Investigation</option>
	                  			<option value="SP_ubs_mental_health">Mental Health</option>
	                  			<option value="SP_ubs_mortality">Mortality</option>
	                  			<option value="SP_ubs_newborn_child_health">Newborn and Child Health</option>
	                  			<option value="SP_ubs_non_communicable_disease">Non-communicable diseases</option>
	                  			<option value="SP_ubs_nutrition">Nutrition Services</option>
	                  			<option value="SP_outpatient_medical_consultation">Outpatient Medical Consultation</option>
	                  			<option value="SP_ubs_referrals">Referrals</option>
	                  			<option value="SP_ubs_sexual_reproductive_health">Sexual and Reproductive Health</option>
							</select>                			
						</div>                  	
	              	</div>
	              <div class="col-md-3">
                	<div class="form-group">                							
						<label for="startDate">Start Date</label><br />
						<input id="startDate"  name="startDate" type="text" />
                  	</div>
                  	
             	</div>
              	<div class="col-md-3">
               		<div class="form-group">
                  	<label for="endDate">End Date</label><br />
						<input id="endDate" name="endDate" type="text" />
					</div>
                  	
              	</div>
              	<div class="col-md-2">
               		<div class="form-group">
               		<label></label><br />
                  	<button style="width: 120px; margin-top: 30px;" type="submit" class="btnSubmit">Submit</button>                  			
					</div>
                  	
              	</div>
              	
          	</div>
          	

  </form:form>
  <div id="loading_prov" style="display: none;position: absolute; z-index: 1000;margin-left:45%"> 
			<img width="50px" height="50px" src="<c:url value="/moduleResources/sharedhealthrecord/images/ajax-loading.gif"/>">
	</div>
		<div id="patientsyncReport">
		<div class="form-content">
				<div>
					Reports : <span id="reportTitle"></span>
						</div>
					</div>
			<div id="thead_id" style="overflow:auto;"></div>
		</div>
</div>
</div>
<script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/service.js"></script>
<script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/jquery.dataTables.js"></script>
<!-- <script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/jszip.min.js"></script>
<script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/buttons.html5.min.js"></script>-->
