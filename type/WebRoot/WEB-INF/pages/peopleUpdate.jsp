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
  <link rel="stylesheet" type="text/css" media="screen" href="css/screen.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/js/jquery-easyui/themes/icon.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/js/jquery-easyui/demo/demo.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath }/js/jquery-easyui/themes/default/easyui.css">
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-easyui/jquery-1.7.2.min.js" ></script>
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-easyui/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath }/js/jquery-easyui/locale/easyui-lang-zh_CN.js"></script>	
	</head>
    
  <body>
	<s:form action="action_update" namespace="/people">
		<p>姓名：<input type="text" name="people.name" value="${people.name }"/></p>
		<p>年龄：<input type="text" name="people.age" value="${people.age }"/></p>
		<p>性别：<s:radio  list="#{'男':'男','女':'女'}" listKey="key" listValue="value" value="'男'" name="people.sex"/></p>
		<label for="name">所属城市：</label>
		<input id="type" name="city.cityName" type="text" readonly="readonly" value="${citys.cityName}"/><br/>
		<input type="hidden" name="people.id" value="${people.id }"/>
		<input type="hidden" id="typeid" name="city.id" value="" />
		<p>
		<input type="submit" value="修改"/>
		</p>
	</s:form>
	<div id="treepannel" iconCls="icon-edit" class="easyui-dialog" modal="true" title="选择地区" style="width:300px;height:600px;padding:0px;background: #fafafa;top:0px"  closed="true" >
		<div style="margin:5px;margin-top5px;border:1px outset #ccc;width:275px;height:550px">
			<ul id="tree">
			</ul>
		</div>
	</div>
  </body>
   <script language="javascript">
  $(function(){
		$('#tree').tree({
			loader:function(param,success,error){
				if(param==null||param.id==null) param.id="";
				$.ajax({					
					type:"POST",
					url:"${pageContext.request.contextPath}/people/action_childrens.both?r="+Math.random(),
					data:param,					
					dataType:"json",
					success:function(data){
						success(data);
						//$('#treepannel').dialog('open');
					},
					error:function(){
						error.apply(this,arguments);
					}
				});
			},
			onClick:function(node){
				if(node.attributes.isLeaf!=true) return false;
				$('#type').val(node.text);
				$('#typeid').val(node.id);
				$("#treepannel").dialog('close');
			},
			onDblClick:function(node){
				if(node.attributes.isLeaf!=true){
					$('#tree').tree('expand',node.target);
				}
			}
		});

		$("#type").click(function(){
 			$("#treepannel").dialog('open');
		});
 });
   </script>
</html>
