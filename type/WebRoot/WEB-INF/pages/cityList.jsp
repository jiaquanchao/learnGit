<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>城市列表</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/js/jquery-easyui/themes/default/easyui.css"/>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/js/jquery-easyui/themes/icon.css"/>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/css/news_a.css"/>
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-easyui/jquery-1.7.2.min.js" ></script>
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-easyui/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-easyui/locale/easyui-lang-zh_CN.js"></script>
	<script type="text/javascript">
	function detailPage(obj,pg){
		pg=parseInt(pg);
		$('#pageNo').val(pg<1?1:pg>${page.totalPage}?${page.totalPage}:pg);
		$('#searchform').submit();
	}

	function typeAdd(){
			self.location='${pageContext.request.contextPath }/action_cityAddUI.both?id=${city.id}';
	}
	function deleteBatch(){
		if($('.ids:checked').size()==0){
			alert("没有选中任何项");
			return;
		}
			if(confirm("该删除操作将无法恢复！是否继续？"))
		$('#deleteform').submit();
	}
	</script>
  </head>
  <body>
	<h1 align="center">城市列表</h1>
  <table border="1" width="80%" align="center">
  	<tr>
			<td colspan="3">
				<form action="" method="post" id="searchform">
				城市查询：<input type="text" name="cityName" value="${requestScope.action.cityName}"/>
				<input type="hidden" id="pageNo" name="pageNo" value="${requestScope.action.pageNo}"/>
				<input type="hidden" id="pageSize" name="pageSize" value="${requestScope.action.pageSize}"/>
				<input type="submit" onclick="$('#pageNo').val(1);" value="查询"/>
				</form>
			</td>
  		<td colspan="5" align="right">
  			第${page.pageNo}页/共${page.totalPage }页,${page.totalRecords}条 &nbsp;&nbsp;
				<s:if test="#page.totalPage==1">
				</s:if>				
				<s:else>
					<input style="width: 25" id="jump" onkeyup="this.value=this.value.replace(/\D/g,'')" value="${page.pageNo}" onafterpaste="this.value=this.value.replace(/\D/g,'')">&nbsp;&nbsp;
					<input type="button" value="跳转" onclick="detailPage(this,$('#jump').val())">
				</s:else>
				<c:if test="${page.pageNo==1}">
					<span style="cursor:default;color:#aeaeae">首  页</span>&nbsp;&nbsp;
					<span style="cursor:default;color:#aeaeae">上一页</span>&nbsp;&nbsp;
				</c:if>
				<c:if test="${page.pageNo!=1}">
					<span style="cursor:hand" onclick="detailPage(this,'1')">首  页</span>&nbsp;&nbsp;
					<span style="cursor:hand" onclick="detailPage(this,'${page.pageNo-1}')">上一页</span>&nbsp;&nbsp;
				</c:if>
				<c:forEach begin="${page.startPage}" end="${page.endPage}" var="pagenum">
					<c:if test="${pagenum==page.pageNo}">
						<span style="color:red;font-size:12px">${pagenum}</span>&nbsp;&nbsp;
					</c:if>
					<c:if test="${pagenum!=page.pageNo}">
						<span style="cursor:hand" onclick="detailPage(this,'${pagenum}')">${pagenum}</span>&nbsp;&nbsp;
					</c:if>
	  		</c:forEach>
				<c:if test="${page.pageNo<page.totalPage}">
					<span style="cursor:hand" onclick="detailPage(this,'${page.pageNo+1}')">下一页</span>&nbsp;&nbsp;
					<span style="cursor:hand" onclick="detailPage(this,'${page.totalPage}')">尾  页</span>&nbsp;&nbsp;
				</c:if>
				<c:if test="${page.pageNo>=page.totalPage}">
					<span style="cursor:default;color:#aeaeae">下一页</span>&nbsp;&nbsp;
					<span style="cursor:default;color:#aeaeae">尾  页</span>&nbsp;&nbsp;
				</c:if>
  		</td>
  	</tr>
  		<tr>
  			<td><input type="checkbox" class="ids" onclick="$('.ids').attr('checked',this.checked);"/></td>
  			<th>代号</th>
  			<th>修改</th>
  			<th>城市名称</th>
  			<th>创建下级类别</th>
  			<th>所属父类</th>
  		</tr>
  		<form id="deleteform" action="${pageContext.request.contextPath }/type/action_deleteBatch.both" method="post">
	    <s:iterator value="#page.queryResult" var="city" >
		    	<tr onmouseover="this.style.background = '#aaa'" onmouseout="this.style.background = 'white'">
		    		<td><input type="checkbox" class="ids" name="ids" value="${city.id}"/></td>
				    	<td>${city.id}</td>
				    	<td align="center"><a href='${pageContext.request.contextPath }/type/action_updateUI.both?id=${city.id}'><img alt="修改" src="${pageContext.request.contextPath }/images/edit.gif"/></a></td>
				    	<td><a href='${pageContext.request.contextPath }/action_cityList.both?id=${city.id}'>${city.cityName }</a></td>
				    	<td><a href='${pageContext.request.contextPath }/action_cityChildAddUI.both?id=${city.id}'>创建下级类别</a></td>
				    	<td>${city.parent.cityName}</td>
		    	</tr>
	    </s:iterator>
	    <tr>
	    	<td><h5><label for="check"><input type="checkbox" id="check" class="ids" onclick="$('.ids').attr('checked',this.checked);"/>全选</label></h5></td>
	    	<td colspan="2"><input type="button" onclick="deleteBatch();" value="彻底删除"/></td>
	    	<td colspan="5"><input type="button" onclick="typeAdd();" value="添加"/></td>
	    </tr>
	    <input type="hidden" name="city.id" id="city.id" value="${city.id}"/>	    
	    </form>
	</table>
  </body>
</html>
