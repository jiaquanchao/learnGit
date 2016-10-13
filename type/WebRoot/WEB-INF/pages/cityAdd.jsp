<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>城市添加界面</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link rel="stylesheet" type="text/css" media="screen" href="css/screen.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/js/jquery-easyui/themes/icon.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/js/jquery-easyui/demo/demo.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/js/jquery-easyui/themes/default/easyui.css">
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-easyui/jquery-1.7.2.min.js" ></script>
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-easyui/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-easyui/locale/easyui-lang-zh_CN.js"></script>	
	</head>
  <body>
		<s:form action="action_cityAdd" onsubmit="return $(this).form('validate');" namespace="/city" >
			<p>城市名：<input id="name" required="true" class="easyui-validatebox" type="text" name="city.cityName" value="${city.cityName }"/></p>	
			<input type="hidden" name="city.id" value="${citys.id }"/>
			<input type="hidden" name="city.depth" value="${citys.depth }"/>
			<p>
			<input type="submit" value="提交" />
			</p>
	</s:form>
		</div>
	</div>
  </body> 
</html>
