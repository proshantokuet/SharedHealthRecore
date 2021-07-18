var $jq = jQuery.noConflict();
$jq(window).load(function() {
	   $jq("#loader").hide();
	    $jq("#tabs").show(); 
});
$jq( function() {
	$jq("#startDate").datepicker({ dateFormat: 'yy-mm-dd', maxDate: new Date });
	$jq("#endDate").datepicker({ dateFormat: 'yy-mm-dd', minDate: new Date});
  } );
  
$jq("#startDate").on("change",function(){
	/* console.log("change"); */
	$jq("#endDate").datepicker(
			'option',
			{ minDate: new Date($jq("#startDate").val()),
			  maxDate: new Date()
			});
});

$jq(document).ready( function () {
	$jq('#loading_prov').show();
	$jq('#reportTitle').html("Acute Health Conditions");
	var tbaleData = window["SP_ubs_acute_health_condition"]();
	$jq("#thead_id").html(tbaleData);
	$jq('#table_id').DataTable({
        bFilter: false,
        serverSide: false,
        processing: true,
	    "searching": true,
        bInfo: true,
        destroy: true,
        ajax: {
            url: "/openmrs/ws/rest/v1/ubs-report/getSelectedReport",
            timeout : 300000,
            data: function(data){
	
					data.startDate = "",
					data.endDate = "",
					data.reportName = "SP_ubs_acute_health_condition"
					
            },
            dataSrc: function(json){
            	$jq('#loading_prov').hide();
                if(json){
                	
                    return json;
                }
                else {
                    return [];
                }
            },
            complete: function() {
            },
            type: 'GET'
        },
        "language": {
        	   "loadingRecords": "Please wait - loading..."
        	}
	});
} );


var requisitionList;
$jq("#patienterrorvisualize").on("submit",function(event){
	    event.preventDefault();
	    $jq('#loading_prov').show();
	    var theadData = window[$jq('#reportName').val()]();
	    $jq("#thead_id").html(theadData);
	    var title = $jq('#reportName').find('option:selected').text();
	    $jq('#reportTitle').html(title);
		var reportName = $jq('#reportName').val();
		var startDate = $jq('#startDate').val();
		var endDate = $jq('#endDate').val();
 		requisitionList = $jq('#table_id').DataTable({
	        bFilter: false,
	        serverSide: false,
	        processing: true,
		    "searching": true,
	        bInfo: true,
	        destroy: true,
        ajax: {
            url: "/openmrs/ws/rest/v1/ubs-report/getSelectedReport",
            timeout : 300000,
            data: function(data){
	
					data.startDate = startDate,
					data.endDate = endDate,
					data.reportName = reportName
					
            },
            dataSrc: function(json){
            	$jq('#loading_prov').hide();
                if(json){
                	
                    return json;
                }
                else {
                    return [];
                }
            },
            complete: function() {
            },
            type: 'GET'
        },
        "language": {
        	   "loadingRecords": "Please wait - loading..."
        	}
    });  
});

SP_ubs_acute_health_condition = () => {
	var destinedThead = ""
		+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
		+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
		+ "				<thead> "
		+ "					<tr> "
		+ "						<th rowspan=\"2\">Acute Health Condition</th> "
		+ "						<th colspan=\"2\">Under-5(M)</th> "
		+ "						<th colspan=\"2\">Under-5(F)</th> "
		+ "						<th colspan=\"2\">6-17(M)</th> "
		+ "                        <th colspan=\"2\">6-17(F)</th> "
		+ "                        <th colspan=\"2\">18-49(M)</th> "
		+ "                        <th colspan=\"2\">18-49(F)</th> "
		+ "                        <th colspan=\"2\">50 and above(M)</th> "
		+ "                        <th colspan=\"2\">50 and above(F)</th> "
		+ "					</tr> "
		+ "					<tr> "
		+ "					 	<th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "					</tr> "
		+ "				</thead> "
		+ "				 "
		+ "			</table> "
		+ "			</div>";
	
	return destinedThead;
}


SP_ubs_medical_and_other_distribute = () =>{
	var destinedThead = ""
		+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
		+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
		+ "				<thead> "
		+ "					<tr> "
		+ "						<th rowspan=\"2\">Acute Health Condition</th> "
		+ "						<th colspan=\"2\">Under-5(M)</th> "
		+ "						<th colspan=\"2\">Under-5(F)</th> "
		+ "						<th colspan=\"2\">6-17(M)</th> "
		+ "                        <th colspan=\"2\">6-17(F)</th> "
		+ "                        <th colspan=\"2\">18-49(M)</th> "
		+ "                        <th colspan=\"2\">18-49(F)</th> "
		+ "                        <th colspan=\"2\">50 and above(M)</th> "
		+ "                        <th colspan=\"2\">50 and above(F)</th> "
		+ "					</tr> "
		+ "					<tr> "
		+ "					 	<th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "					</tr> "
		+ "				</thead> "
		+ "				 "
		+ "			</table> "
		+ "			</div>";
	
	return destinedThead;
	
}

 SP_ubs_sexual_reproductive_health = ()=>{
	var destinedThead = ""
		+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
		+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
		+ "				<thead> "
		+ "					<tr> "
		+ "						<th rowspan=\"2\">Sexual Reproductive Healt(SRH)</th> "
		+ "						<th colspan=\"2\">Adolescent(10-17)</th> "
		+ "						<th colspan=\"2\">Adult(18 and above)</th> "
		+ "		 "
		+ "					</tr> "
		+ "					<tr> "
		+ "					 	<th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                         "
		+ "					</tr> "
		+ "				</thead> "
		+ "				 "
		+ "			</table> "
		+ "			</div>";
	
	return destinedThead;
	
}

 SP_ubs_non_communicable_disease = ()=>{
	var destinedThead = ""
		+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
		+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
		+ "				<thead> "
		+ "					<tr> "
		+ "						<th rowspan=\"2\">Non-communicable diseases</th> "
		+ "						<th colspan=\"2\">Under-5(M)</th> "
		+ "						<th colspan=\"2\">Under-5(F)</th> "
		+ "						<th colspan=\"2\">6-17(M)</th> "
		+ "                        <th colspan=\"2\">6-17(F)</th> "
		+ "                        <th colspan=\"2\">18-49(M)</th> "
		+ "                        <th colspan=\"2\">18-49(F)</th> "
		+ "                        <th colspan=\"2\">50 and above(M)</th> "
		+ "                        <th colspan=\"2\">50 and above(F)</th> "
		+ "					</tr> "
		+ "					<tr> "
		+ "					 	<th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "					</tr> "
		+ "				</thead> "
		+ "				 "
		+ "			</table> "
		+ "			</div>";
	
	return destinedThead;
}

 SP_ubs_injuries = ()=>{
	var destinedThead = ""
		+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
		+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
		+ "				<thead> "
		+ "					<tr> "
		+ "						<th rowspan=\"2\">Injuries</th> "
		+ "						<th colspan=\"2\">Under-5(M)</th> "
		+ "						<th colspan=\"2\">Under-5(F)</th> "
		+ "						<th colspan=\"2\">6-17(M)</th> "
		+ "                        <th colspan=\"2\">6-17(F)</th> "
		+ "                        <th colspan=\"2\">18-49(M)</th> "
		+ "                        <th colspan=\"2\">18-49(F)</th> "
		+ "                        <th colspan=\"2\">50 and above(M)</th> "
		+ "                        <th colspan=\"2\">50 and above(F)</th> "
		+ "					</tr> "
		+ "					<tr> "
		+ "					 	<th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "					</tr> "
		+ "				</thead> "
		+ "				 "
		+ "			</table> "
		+ "			</div>";
	
	return destinedThead;
}

 SP_ubs_nutrition = ()=>{
	var destinedThead = ""
		+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
		+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
		+ "				<thead> "
		+ "					<tr> "
		+ "						<th rowspan=\"2\">Nutrition Services</th> "
		+ "						<th colspan=\"2\">Under-5(M)</th> "
		+ "						<th colspan=\"2\">Under-5(F)</th> "
		+ "						<th colspan=\"2\">6-17(M)</th> "
		+ "                        <th colspan=\"2\">6-17(F)</th> "
		+ "                        <th colspan=\"2\">18-49(M)</th> "
		+ "                        <th colspan=\"2\">18-49(F)</th> "
		+ "                        <th colspan=\"2\">50 and above(M)</th> "
		+ "                        <th colspan=\"2\">50 and above(F)</th> "
		+ "					</tr> "
		+ "					<tr> "
		+ "					 	<th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "					</tr> "
		+ "				</thead> "
		+ "				 "
		+ "			</table> "
		+ "			</div>";
	
	return destinedThead;
}


 SP_ubs_mental_health = ()=>{
	var destinedThead = ""
		+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
		+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
		+ "				<thead> "
		+ "					<tr> "
		+ "						<th rowspan=\"2\">Mental Health</th> "
		+ "						<th colspan=\"2\">Under-5(M)</th> "
		+ "						<th colspan=\"2\">Under-5(F)</th> "
		+ "						<th colspan=\"2\">6-17(M)</th> "
		+ "                        <th colspan=\"2\">6-17(F)</th> "
		+ "                        <th colspan=\"2\">18-49(M)</th> "
		+ "                        <th colspan=\"2\">18-49(F)</th> "
		+ "                        <th colspan=\"2\">50 and above(M)</th> "
		+ "                        <th colspan=\"2\">50 and above(F)</th> "
		+ "					</tr> "
		+ "					<tr> "
		+ "					 	<th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "					</tr> "
		+ "				</thead> "
		+ "				 "
		+ "			</table> "
		+ "			</div>";
	
	return destinedThead;
}

 SP_ubs_communicable_disease = ()=>{
	var destinedThead = ""
		+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
		+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
		+ "				<thead> "
		+ "					<tr> "
		+ "						<th rowspan=\"2\">Communicable Diseases</th> "
		+ "						<th colspan=\"2\">Under-5(M)</th> "
		+ "						<th colspan=\"2\">Under-5(F)</th> "
		+ "						<th colspan=\"2\">6-17(M)</th> "
		+ "                        <th colspan=\"2\">6-17(F)</th> "
		+ "                        <th colspan=\"2\">18-49(M)</th> "
		+ "                        <th colspan=\"2\">18-49(F)</th> "
		+ "                        <th colspan=\"2\">50 and above(M)</th> "
		+ "                        <th colspan=\"2\">50 and above(F)</th> "
		+ "					</tr> "
		+ "					<tr> "
		+ "					 	<th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                         "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "                        <th>FDMN</th> "
		+ "					 	<th>HOST</th> "
		+ "					</tr> "
		+ "				</thead> "
		+ "				 "
		+ "			</table> "
		+ "			</div>";
	
	return destinedThead;
}
 
 
 SP_ubs_referrals = ()=>{
		var destinedThead = ""
			+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
			+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
			+ "				<thead> "
			+ "					<tr> "
			+ "						<th rowspan=\"2\">Referrals</th> "
			+ "						<th colspan=\"2\">Under-5(M)</th> "
			+ "						<th colspan=\"2\">Under-5(F)</th> "
			+ "						<th colspan=\"2\">6-17(M)</th> "
			+ "                        <th colspan=\"2\">6-17(F)</th> "
			+ "                        <th colspan=\"2\">18-49(M)</th> "
			+ "                        <th colspan=\"2\">18-49(F)</th> "
			+ "                        <th colspan=\"2\">50 and above(M)</th> "
			+ "                        <th colspan=\"2\">50 and above(F)</th> "
			+ "					</tr> "
			+ "					<tr> "
			+ "					 	<th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "					</tr> "
			+ "				</thead> "
			+ "				 "
			+ "			</table> "
			+ "			</div>";
		
		return destinedThead;
	}
 
 
 SP_ubs_inPatient_service = ()=>{
		var destinedThead = ""
			+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
			+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
			+ "				<thead> "
			+ "					<tr> "
			+ "						<th rowspan=\"2\">In Patient Services</th> "
			+ "						<th colspan=\"2\">Under-5(M)</th> "
			+ "						<th colspan=\"2\">Under-5(F)</th> "
			+ "						<th colspan=\"2\">6-17(M)</th> "
			+ "                        <th colspan=\"2\">6-17(F)</th> "
			+ "                        <th colspan=\"2\">18-49(M)</th> "
			+ "                        <th colspan=\"2\">18-49(F)</th> "
			+ "                        <th colspan=\"2\">50 and above(M)</th> "
			+ "                        <th colspan=\"2\">50 and above(F)</th> "
			+ "					</tr> "
			+ "					<tr> "
			+ "					 	<th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "					</tr> "
			+ "				</thead> "
			+ "				 "
			+ "			</table> "
			+ "			</div>";
		
		return destinedThead;
	}
 
 
 SP_ubs_medical_investigation = ()=>{
		var destinedThead = ""
			+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
			+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
			+ "				<thead> "
			+ "					<tr> "
			+ "						<th rowspan=\"2\">Medical Investigation</th> "
			+ "						<th colspan=\"2\">Under-5(M)</th> "
			+ "						<th colspan=\"2\">Under-5(F)</th> "
			+ "						<th colspan=\"2\">6-17(M)</th> "
			+ "                        <th colspan=\"2\">6-17(F)</th> "
			+ "                        <th colspan=\"2\">18-49(M)</th> "
			+ "                        <th colspan=\"2\">18-49(F)</th> "
			+ "                        <th colspan=\"2\">50 and above(M)</th> "
			+ "                        <th colspan=\"2\">50 and above(F)</th> "
			+ "					</tr> "
			+ "					<tr> "
			+ "					 	<th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "					</tr> "
			+ "				</thead> "
			+ "				 "
			+ "			</table> "
			+ "			</div>";
		
		return destinedThead;
	}
 
 
 SP_ubs_child_vaccination = ()=>{
		var destinedThead = ""
			+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
			+ "<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
			+ "				<thead> "
			+ "					<tr> "
			+ "						<th rowspan=\"2\">EPI and Vaccination</th> "
			+ "						<th colspan=\"2\">Under-5(M)</th> "
			+ "						<th colspan=\"2\">Under-5(F)</th> "
			+ "						<th colspan=\"2\">Adolescent girls(10-17)</th> "
			+ "                        <th colspan=\"2\">Women(18 and above)</th> "
			+ "					</tr> "
			+ "					<tr> "
			+ "					 	<th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                          "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                          "
			+ "                         "
			+ "					</tr> "
			+ "				</thead> "
			+ "				  "
			+ "			</table>"
			+ "			</div>";
		
		return destinedThead;
	}
 
 SP_ubs_mortality = ()=>{
		var destinedThead = ""
			+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
			+ "<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
			+ "				<thead> "
			+ "					<tr> "
			+ "						<th rowspan=\"2\">Mortality</th> "
			+ "						<th colspan=\"2\">Male</th> "
			+ "						<th colspan=\"2\">Female</th> "
			+ "						<th colspan=\"2\">Total</th> "
			+ "					</tr> "
			+ "					<tr> "
			+ "					 	<th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "					</tr> "
			+ "				</thead> "
			+ "				  "
			+ "			</table>"
			+ "			</div>";
		
		return destinedThead;
	}
 
 SP_ubs_newborn_child_health = ()=>{
		var destinedThead = ""
			+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
			+ "<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
			+ "				<thead> "
			+ "					<tr> "
			+ "						<th rowspan=\"2\">Newborn and Child Health</th> "
			+ "						<th colspan=\"2\">Male</th> "
			+ "						<th colspan=\"2\">Female</th> "
			+ "					</tr> "
			+ "					<tr> "
			+ "					 	<th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "					</tr> "
			+ "				</thead> "
			+ "				  "
			+ "			</table>"
			+ "			</div>";
		
		return destinedThead;
	}
 
 SP_outpatient_medical_consultation = ()=>{
		var destinedThead = ""
			+ "<div id=\"thead_id\" style=\"overflow:auto;\"> "
			+ "			<table id=\"table_id\" class=\"table table-striped table-bordered\"> "
			+ "				<thead> "
			+ "					<tr> "
			+ "						<th rowspan=\"2\">Outpatient Medical Consultation</th> "
			+ "						<th colspan=\"2\">Under-5(M)</th> "
			+ "						<th colspan=\"2\">Under-5(F)</th> "
			+ "						<th colspan=\"2\">6-17(M)</th> "
			+ "                        <th colspan=\"2\">6-17(F)</th> "
			+ "                        <th colspan=\"2\">18-49(M)</th> "
			+ "                        <th colspan=\"2\">18-49(F)</th> "
			+ "                        <th colspan=\"2\">50 and above(M)</th> "
			+ "                        <th colspan=\"2\">50 and above(F)</th> "
			+ "					</tr> "
			+ "					<tr> "
			+ "					 	<th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                         "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "                        <th>FDMN</th> "
			+ "					 	<th>HOST</th> "
			+ "					</tr> "
			+ "				</thead> "
			+ "				 "
			+ "			</table> "
			+ "			</div>";
		
		return destinedThead;
	}
 
 
 /* $jq(document).ready( function () {
	$jq('#reportTitle').html("Acute Health Conditions");
	$jq('#table_id').DataTable({
     bFilter: false,
     serverSide: false,
     processing: true,
	    "searching": true,
		dom: 'Bfrtip',
     bInfo: true,
     destroy: true,
     ajax: {
         url: "/openmrs/ws/rest/v1/ubs-report/getSelectedReport",
         timeout : 300000,
         data: function(data){
	
					data.startDate = "",
					data.endDate = "",
					data.reportName = "SP_ubs_acute_health_condition"
					
         },
         dataSrc: function(json){
             if(json){
                 return json		;
             }
             else {
                 return [];
             }
         },
         complete: function() {
         },
         type: 'GET'
     },
		buttons: [
		             {
		                 extend: 'excelHtml5',
		                 title: "Acute Health Conditions",
		                 text: 'Export as .xlxs',
		                 customize:function(win){
		                	 var sheet = win.xl.worksheets['sheet1.xml'];
		                	 console.log(sheet);
		                	 $jq('c[r=B2 ] t', sheet).text( 'FDMN Under-5(M)' );
		                	 $jq('c[r=C2 ] t', sheet).text( 'HOST Under-5(M)' );
		                	 $jq('c[r=D2 ] t', sheet).text( 'FDMN Under-5(F)' );
		                	 $jq('c[r=E2 ] t', sheet).text( 'HOST Under-5(F)' );
		                	 $jq('c[r=F2 ] t', sheet).text( 'FDMN 6-17(M)' );
		                	 $jq('c[r=G2 ] t', sheet).text( 'HOST 6-17(M)' );
		                	 $jq('c[r=H2 ] t', sheet).text( 'FDMN 6-17(F)' );
		                	 $jq('c[r=I2 ] t', sheet).text( 'HOST 6-17(F)' );
		                	 $jq('c[r=J2 ] t', sheet).text( 'FDMN 18-49(M)' );
		                	 $jq('c[r=K2 ] t', sheet).text( 'HOST 18-49(M)' );
		                	 $jq('c[r=L2 ] t', sheet).text( 'FDMN 18-49(F)' );
		                	 $jq('c[r=M2 ] t', sheet).text( 'HOST 18-49(F)');
		                	 $jq('c[r=N2 ] t', sheet).text( 'FDMN 50 and above(M)' );
		                	 $jq('c[r=O2 ] t', sheet).text( 'HOST 50 and above(M)' );
		                	 $jq('c[r=P2 ] t', sheet).text( 'FDMN 50 and above(F)' );
		                	 $jq('c[r=Q2 ] t', sheet).text( 'HOST 50 and above(F)' );
		                	
		                	  }
		             },
		             {
			         		extend: 'pdfHtml5',
			                title: "Acute Health Conditions",
			         		text: 'Export as .pdf',
			         		orientation: 'landscape',
			         		pageSize: 'LEGAL',
			         		customize:function(pdfDocument){
			         			pdfDocument.content[1].table.headerRows = 2;
			                    var firstHeaderRow = [];
			                    $jq('#table_id').find("thead>tr:first-child>th").each(
			                            function(index, element) {
			                              var colSpan = element.getAttribute("colSpan");
			                              firstHeaderRow.push({
			                                text: element.innerHTML,
			                                style: "tableHeader",
			                                colSpan: colSpan
			                              });
			                              for (var i = 0; i < colSpan - 1; i++) {
			                                firstHeaderRow.push({});
			                              }
	                            });
			                    pdfDocument.content[1].table.body.unshift(firstHeaderRow);
			                    pdfDocument.content[1].table.body[1][0].text = "";
			                    pdfDocument.content[1].layout = "";
			                    
			         		}
				     }
		         ]
	});
} );
*/