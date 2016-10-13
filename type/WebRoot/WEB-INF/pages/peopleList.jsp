<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>人员列表</title>
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

	$(function(){
		$('#dateBegin').datebox();
		$('#dateAfter').datebox();
		$('.combo-text').attr('readonly','readonly');
	});
	
	function deleteBatch(){
		if($('.ids:checked').size()==0){
			alert("没有选中任何项");
			return;
		}
			if(confirm("该删除操作将无法恢复！是否继续？"))
		$('#deleteform').attr('action', '${pageContext.request.contextPath }/people/action_deleteBatch.both');		
		$('#deleteform').submit();
	}	
	
	function hideBatch(){
		if($('.ids:checked').size()==0){
			alert("没有选中任何项");
			return;
		}
			if(confirm("是否确定删除选中的信息"))
		$('#deleteform').attr('action', '${pageContext.request.contextPath }/people/action_hideBatch.both');
		$('#deleteform').submit();
	}
	
	function showInput(obj){
		$(obj).nextAll('input[gp=1]').val("").hide();
		$('#'+$(obj).val()).show();
	}
	</script>
  </head>
  <body>
	<h1 align="center">人员列表</h1>
	<h2><a href="${pageContext.request.contextPath }/people/action_addUI.both">人员添加</a></h2>
	<h2><a href="${pageContext.request.contextPath }/people/action_hideList.both">人员回收站</a></h2>
  <table border="1" width="80%" align="center">
  	<tr>
			<td colspan="3">
				<form action="" method="post" id="searchform">
					请选择查询方式：
					<select onchange="javascript:showInput(this);">
						<option value="">请选择...</option>
						<option value="name">姓名查询</option>
						<option value="sex">性别查询</option>
					</select>
					<input type="text" gp='1' style="display:none" id="name" name="name" value="${requestScope.action.name}"/>
					<input type="text" gp='1' style="display:none" id="sex" name="sex" value="${requestScope.action.sex}"/>
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
  			<th>姓名</th>
  			<th>性别</th>
  			<th>年龄</th>
  			<th>出生日期</th>
  			<th>所属城市</th>
  			<th colspan="2">操作</th>
  		</tr>
  		<form id="deleteform" action="" method="post">
	    <s:iterator value="#page.queryResult" var="people" >
		    	<tr onmouseover="this.style.background = '#aaa'" onmouseout="this.style.background = 'white'">
		    		<td><input type="checkbox" class="ids" name="ids" value="${people.id}"/></td>
				    	<td>${people.name }</td>
				    	<td> ${people.sex }</td>
				    	<td>${people.age }</td>
				    	<td align="right">${people.birthday }</td>
				    	<td>${city.cityName}</td>			    	
		    		<td><a href="${pageContext.request.contextPath }/people/action_updateUI.both?id=${people.id }">修改</a></td>
		    		<td><a href="${pageContext.request.contextPath }/people/action_hide.both?id=${people.id }" onClick="javascript:return confirm('是否确定删除')">删除 </a></td>
		    	</tr>
	    </s:iterator>
	    <tr>
	    	<td><h5><label for="check"><input type="checkbox" id="check" class="ids" onclick="$('.ids').attr('checked',this.checked);"/>全选</label></h5></td>
	    	<td colspan="7"><input type="button" onclick="hideBatch();" value="删除"/>&nbsp;<input type="button"/ onclick="deleteBatch();" value="彻底删除"></td>
	    </tr>
	    </form>
	</table>
	
  </body>
</html>
