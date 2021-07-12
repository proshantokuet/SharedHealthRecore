var $JQuery = jQuery.noConflict();
$JQuery("#serviceForm").submit(function(event) { 
	$JQuery("#loading").show();
	$JQuery("#usernameUniqueSuccessMessage").hide();
	$JQuery("#usernameUniqueErrorMessage").hide();
			var url = "/openmrs/ws/rest/v1/service-management/save";			
			var token = $JQuery("meta[name='_csrf']").attr("content");
			var header = $JQuery("meta[name='_csrf_header']").attr("content");			
			
			var e = document.getElementById("category");
			var category = e.options[e.selectedIndex].value;

			//var clinic = document.getElementById("psiClinicManagement");
			var clinicValue = $JQuery('input[name=psiClinicManagement]').val();

			var voided = null;
			var disableConfirmation = $JQuery('input[name=disableService]:checked').val();
			var enableConfirmation = $JQuery('input[name=enableService]:checked').val();
			if (disableConfirmation == "yes") {
				voided = true;
			}
			if (disableConfirmation == "no") {
				voided = false;
			}
			if (enableConfirmation == "yes") {
				voided = false;
			}

			var formData;			
				formData = {
			            'name': $JQuery('input[name=name]').val(),			           
			            'code': $JQuery('input[name=code]').val(),
			            'category': category,
			            'provider': '',
			            'sid':  $JQuery('input[name=sid]').val(),
			            'unitCost': $JQuery('input[name=unitCost]').val(),
			            'psiClinicManagement': clinicValue,
			            'yearTo': 0,
			            'monthTo': 0,
			            'daysTo': 0,
			            'yearFrom': 0,
			            'monthFrom': 0,
			            'gender': '',
			            'daysFrom': 0,
			            'voided': voided,
			            'type': 'PRODUCT',
			            'brandName': $JQuery('input[name=brandName]').val(),
			            'purchasePrice': $JQuery('input[name=purchasePrice]').val(),
			            'discountPop': $JQuery('input[name=discountPop]').val(),
			            'discountPoor': $JQuery('input[name=discountPoor]').val(),
			            'discountAblePay': $JQuery('input[name=discountAblePay]').val()
			           
			        };			
			
			console.log(formData);
			event.preventDefault();
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
				   $JQuery("#loading").hide();
				   if(data == ""){	
						$JQuery("#usernameUniqueSuccessMessage").html("Product Successfully Saved");
						$JQuery("#usernameUniqueSuccessMessage").show();
					   window.location.replace("/openmrs/module/PSI/product-list.form?id="+clinicValue);
				   }
				   else {
						$JQuery("#usernameUniqueErrorMessage").html(data);
						$JQuery("#usernameUniqueErrorMessage").show();
				   }
				   
				},
				error : function(e) {
				   
				},
				done : function(e) {				    
				    console.log("DONE");				    
				}
			}); 
		});