<%@ page session="true" %>
<%@page import="uk.ac.ebi.thor.model.SessionUser"%>
<%@page import="uk.ac.ebi.thor.service.DataClaimingService"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title></title>
</head>
<body>
	<script>
		var serverTarget = "<%=(String)session.getAttribute(DataClaimingService.PARAM_CLIENTADD) %>"
		window.opener.postMessage("refresh", serverTarget);
		window.close();
	</script>
</body>
</html>