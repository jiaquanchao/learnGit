<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>修改界面</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
  </head>
    
  <body>
	<s:form action="action_update" namespace="/city">
		<p>城市名称：<input type="text" name="city.cityName" value="${city.cityName }"/></p>
		<input type="hidden" name="city.id" value="${city.id }"/>
		<input type="submit" value="修改"/>
	</s:form>
  </body>
</html>
