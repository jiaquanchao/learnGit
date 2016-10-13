<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>内容显示界面</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
  </head>
  
  <body>
	<form action="${pageContext.request.contextPath }/news/action_list.both" namespace="/news" method="post">
		<p><h4>${news.title }</h4></p>
		<p>发布时间：${news.publishtime }</p>
		<p>更新时间：${news.updatetime }</p>
		<p>所属分类：${news.type.name }</p>
		<hr/>
		<p>
			<pre>
				<h5>
					${news.content }
				</h5>
			</pre>
		</p>
		<p>
		<input type="submit" value="返回"/>
		</p>
	</form>
	<s:fielderror></s:fielderror>
  </body>
</html>
