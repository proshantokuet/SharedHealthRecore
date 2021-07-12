<spring:htmlEscape defaultHtmlEscape="true" />

<link rel="stylesheet" href="/openmrs/moduleResources/sharedhealthrecord/css/style.css">
<link rel="stylesheet" href="/openmrs/moduleResources/sharedhealthrecord/css/bootstrap.min.css">
<link rel="stylesheet" href="/openmrs/moduleResources/sharedhealthrecord/css/jquery.dataTables.css">
 <link rel="stylesheet" href="/openmrs/moduleResources/sharedhealthrecord/css/select2.css"> 
<script type="text/javascript" src="/openmrs/moduleResources/sharedhealthrecord/js/bootstrap.min.js"></script>
<ul id="menu">
	<li>
		<a
		href="${pageContext.request.contextPath}/module/sharedhealthrecord/reports.form">Reports</a>
	</li>
		<li>		
		<a href="/bahmni/home/#/dashboard">Back to Main</a>
	</li>
	<!-- Add further links here -->
</ul>
<h2>
	<p>${pre }</p> <p>${report }</p>
</h2>
