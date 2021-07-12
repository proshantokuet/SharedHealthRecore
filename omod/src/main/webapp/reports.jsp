<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>


<style>
.dataTables_wrapper .dt-buttons {
  float:none;  
  text-align:right;
  position: static;
  top: -26px;
  margin-left: 1036px
}
.dataTables_wrapper .dataTables_filter {
    float: left;
    text-align: right;
}
table.dataTable tbody th, table.dataTable tbody td {
     padding: 0px 0px; 
}

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
	                  			<option value="SP_ubs_medical_and_other_distribute">Medical And Other Distributes</option>
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
	`			<div style="overflow:auto;">
			<table id="table_id" class="table table-striped table-bordered">
				<thead>
					<tr>
						<th rowspan="2">Acute Health Condition</th>
						<th colspan="2">Under-5(M)</th>
						<th colspan="2">Under-5(F)</th>
						<th colspan="2">6-17(M)</th>
                        <th colspan="2">6-17(F)</th>
                        <th colspan="2">18-49(M)</th>
                        <th colspan="2">18-49(F)</th>
                        <th colspan="2">50 and above(M)</th>
                        <th colspan="2">50 and above(F)</th>
					</tr>
					<tr>
					 	<th>FDMN</th>
					 	<th>HOST</th>
                        <th>FDMN</th>
					 	<th>HOST</th>
                        
                        <th>FDMN</th>
					 	<th>HOST</th>
                        <th>FDMN</th>
					 	<th>HOST</th>
                        
                        <th>FDMN</th>
					 	<th>HOST</th>
                        <th>FDMN</th>
					 	<th>HOST</th>
                        
                        <th>FDMN</th>
					 	<th>HOST</th>
                        <th>FDMN</th>
					 	<th>HOST</th>
					</tr>
				</thead>
				
			</table>
			</div>
		</div>
</div>
</div>
<script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/jquery.dataTables.js"></script>
<script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/dataTables.buttons.min.js"></script>
<script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/jszip.min.js"></script>
<script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/buttons.html5.min.js"></script>
<script defer type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/pdfmake.min.js"></script>
<script defer type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/buttons.print.min.js"></script>
<script defer type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/vfs_fonts.js"></script>

<script type="text/javascript">
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


var requisitionList;
$jq("#patienterrorvisualize").on("submit",function(event){
	    event.preventDefault();
	    
	    $jq('#reportTitle').html($jq('#reportName').val());
		var reportName = $jq('#reportName').val();
		var startDate = $jq('#startDate').val();
		var endDate = $jq('#endDate').val();
 		requisitionList = $jq('#table_id').DataTable({
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
	
					data.startDate = startDate,
					data.endDate = endDate,
					data.reportName = reportName
					
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
		                 text: 'Export as .xlxs'
		             },
		             {
			         		extend: 'pdfHtml5',
			                title: "Acute Health Conditions",
			         		text: 'Export as .pdf',
			         		orientation: 'landscape',
			         		pageSize: 'LEGAL'
				     }
		         ]
    });  
});

/* $jq("#patienterrorvisualize").on("submit",function(event){
	event.preventDefault();
	$jq("#loading_prov").show();
	var action_type = $jq("#syncType").val();
	if(action_type.val == "") {
		return;
	}
	var url = "/openmrs/module/PSI/globalServerSyncInfoByFilter.form?action_type="+action_type;
	
	$jq.ajax({
		type:"GET",
		contentType : "application/json",
	    url : url,	 
	    dataType : 'html',
	    timeout : 100000,
	    beforeSend: function() {	    
	    		
	    },
	    success:function(data){
			   $jq("#patientsyncReport").html(data);
			   //$jq('#table_id').DataTable();
				$jq('#table_id').DataTable({
					   bFilter: false,
				       bInfo: false,
				       "searching": true,
					   dom: 'Bfrtip',
					   destroy: true,
					   buttons: [
					             {
					                 extend: 'excelHtml5',
					                 title: action_type + " Data Sync Report",
					                 text: 'Export as .xlxs'
					             },
					             {
						         		extend: 'pdfHtml5',
						         		title: action_type + " Data Sync Report",
						         		text: 'Export as .pdf',
						         		orientation: 'landscape',
						         		pageSize: 'LEGAL'
							     }
					         ]
				});
			   
			   $jq("#loading_prov").hide();
	    },
	    error:function(data){
	    	$jq("#loading_prov").hide();
	    }
	    
	});
}); */

</script>