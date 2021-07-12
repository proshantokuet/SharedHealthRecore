var $JQuery = jQuery.noConflict();
$JQuery("#clinicInfo").submit(function(event) { 
	$JQuery("#loading").show();
			
			event.preventDefault();
			
			var url = "/openmrs/ws/rest/v1/clinic/save";			
			var token = $JQuery("meta[name='_csrf']").attr("content");
			var header = $JQuery("meta[name='_csrf_header']").attr("content");
			
			var e = document.getElementById("category");
			var category = e.options[e.selectedIndex].value;
			
			var division = document.getElementById("divisionId");
			var divisionId = division.options[division.selectedIndex].value;
			
			var district = document.getElementById("districtId");
			var districtId = district.options[district.selectedIndex].value;
			
			var upazila = document.getElementById("upazilaId");
			var upazilaId = upazila.options[upazila.selectedIndex].value;
			
			var formData;			
			formData = {
			            'name': $JQuery('input[name=name]').val(),			           
			            'clinicId': $JQuery('input[name=clinicId]').val(),
			            'category': category,
			            'address': $JQuery('input[name=address]').val(),
			            'dhisId': $JQuery('input[name=dhisId]').val(),
			            'cid': $JQuery('input[name=cid]').val(),
			            'division': divisionId,
			            'district': districtId,
			            'upazila': upazilaId
			           
			 };
			
			$JQuery.ajax({
				contentType : "application/json",
				type: "POST",
		        url: url,
		        data: JSON.stringify(formData), 
		        dataType : 'json',
		        
				timeout : 100000,
				beforeSend: function(xhr) {				    
					 xhr.setRequestHeader(header, token);
				},
				success : function(data) {
					$JQuery("#usernameUniqueErrorMessage").html(data);
					$JQuery("#loading").hide();
				   if(data == ""){					   
					   window.location.replace("/openmrs/module/PSI/PSIClinicList.form");
					   
				   }
				   
				},
				error : function(e) {
				   
				},
				done : function(e) {				    
				    console.log("DONE");				    
				}
			}); 
		});


$JQuery("#divisionId").change(function(event) { 
	var e = document.getElementById("divisionId");
	var divisionId = e.options[e.selectedIndex].value;	
	var url = "/openmrs/module/PSI/locations.form?locationId="+divisionId;

	if(divisionId ==""){
		 $JQuery("#districtId").html("");
		 $JQuery("#upazilaId").html("");			
		
	} else {
		event.preventDefault();
		$JQuery.ajax({
			   type : "GET",
			   contentType : "application/json",
			   url : url,	 
			   dataType : 'html',		   
			   beforeSend: function() {    
			   
			   },
			   success : function(data) {		   
				   $JQuery("#districtId").html(data);			  
			   },
			   error : function(e) {
			    console.log("ERROR: ", e);
			    display(e);
			   },
			   done : function(e) {	    
			    console.log("DONE");
			    //enableSearchButton(true);
			   }
			}); 
	 	}

	});

	$JQuery("#districtId").change(function(event) { 
		var e = document.getElementById("districtId");
		var districtId = e.options[e.selectedIndex].value;	
		var url = "/openmrs/module/PSI/locations.form?locationId="+districtId;			
		if(districtId ==""){			 
			 $JQuery("#upazilaId").html("");
		} else {
			event.preventDefault();	
			$JQuery.ajax({
				   type : "GET",
				   contentType : "application/json",
				   url : url,	 
				   dataType : 'html',		   
				   beforeSend: function() {	    
				   
				   },
				   success : function(data) {		   
					   $JQuery("#upazilaId").html(data);			  
				   },
				   error : function(e) {
				    console.log("ERROR: ", e);
				    display(e);
				   },
				   done : function(e) {	    
				    console.log("DONE");
				    //enableSearchButton(true);
				   }
				}); 
			}
		}); 