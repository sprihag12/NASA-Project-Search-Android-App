<%@page language="java" import="java.util.*" %>
<%@ page import="com.google.gson.JsonObject" %>
<html>
<head>
	<title>Dashboard Page</title>
</head>
<body>
<h1>ANDROID LOGS</h1>
<table border="1" width="303">
	<tr>
		<td width="119"><b>ANDROID_ID</b></td>
		<td width="119"><b>PROJECT</b></td>
		<td width="168"><b>URL</b></td>
		<td width="168"><b>DEV</b></td>
		<td width="168"><b>DATE</b></td>
		<td width="168"><b>LATENCY</b></td>

	</tr>
	<%Iterator<JsonObject> itr;%>
	<% ArrayList<JsonObject> data= (ArrayList<JsonObject>)request.getAttribute("db");
	for (itr=data.iterator(); itr.hasNext();)
		{   System.out.println("ur");
			JsonObject tableJsonObj= new JsonObject();
			tableJsonObj=itr.next();
			String android = tableJsonObj.get("android_id").toString();
			String id = tableJsonObj.get("proj_id").toString();
			String url = tableJsonObj.get("url").toString();
			String dev = tableJsonObj.get("device").toString();
			String date = tableJsonObj.get("createdDate").toString();
			String latency = tableJsonObj.get("FetchTime").toString();

	%>
	<tr>
		<td width="119"><%=android%></td>
		<td width="119"><%=id%></td>
		<td width="168"><%=url%></td>
		<td width="168"><%=dev%></td>
		<td width="168"><%=date%></td>
		<td width="168"><%=latency%></td>
	</tr>
	<%}%>
</table>
<br>
<h1>KEY METRICS</h1>
<h3>Average NASA Search Latency(in milliseconds): <%= request.getAttribute("avgLatency")%></h3><br>
<h3>Most Viewed Project: <%= request.getAttribute("mostViewed")%></h3><br>
<h3>No. Of Unique Hits: <%= request.getAttribute("uniqHits")%></h3><br>

</body>
</html>