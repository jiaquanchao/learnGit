package com.bothtimes.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;

import com.bothtimes.domain.city.City;
import com.bothtimes.domain.people.People;
import com.bothtimes.service.city.CityService;
import com.bothtimes.service.people.PeopleService;
import com.bothtimes.utils.ActionUtils;
import com.opensymphony.xwork2.ActionContext;

public class PeopleAction {
	@Resource CityService cityService;
	@Resource PeopleService peopleService;
	private String message;
	private String name;
	private City city;
	private Integer id;
	private People people;
	private String sex;
	private Integer pageNo=1;
	private Integer pageSize=10;
	private Integer ids[];
	
	public String peopleList(){
		StringBuffer wherejpql = new StringBuffer(" state=? ");
		List<Object> params = new ArrayList<Object>();
		params.add(true);
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		if(name!=null){
			wherejpql.append(" and name like ?");
			params.add("%"+name+"%");
		}		
//		if(age!=null){
//			wherejpql.append(" and age like ?");
//			params.add("%"+age+"%");
//		}		
		if(sex!=null){
			wherejpql.append(" and sex like ?");
			params.add("%"+sex+"%");
		}		
		orderby.put("id", "desc");
		ActionContext.getContext().put("page", peopleService.getResult(pageNo, pageSize, wherejpql.toString(), params.toArray(), orderby));
		return "peopleList";
	}
	
	/**
	 * 已删人员列表
	 * @return "hideList"
	 */
	public String hideList(){
		StringBuffer wherejpql = new StringBuffer(" state=? ");
		List<Object> params = new ArrayList<Object>();
		params.add(false);		
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		if(name!=null){
			wherejpql.append(" and name like ?");
			params.add("%"+name+"%");
		}		
//		if(age!=null){
//			wherejpql.append(" and age like ?");
//			params.add("%"+age+"%");
//		}		
		if(sex!=null){
			wherejpql.append(" and sex like ?");
			params.add("%"+sex+"%");
		}		
		orderby.put("id", "desc");
		ActionContext.getContext().put("page", peopleService.getResult(pageNo, pageSize, wherejpql.toString(), params.toArray(), orderby));
		return "hideList";
	}
	
	/**
	 * 多选删除按钮
	 * @return message
	 */
	public String deleteBatch(){
		for(Integer delid : ids){
			peopleService.delete(delid);
		}
		message="删除成功";
		return "message";
	}
	
	/**
	 * 多选隐藏按钮
	 * @return message
	 */
	public String hideBatch(){
		for(Integer hide : ids){
			People p = peopleService.find(hide);
			p.setState(false);
			peopleService.update(p);
		}
		message="删除操作成功";
		return "message";
	}
	
	/**
	 * 多选还原按钮
	 * @return message
	 */
	public String backBatch(){
		for(Integer hide : ids){
			System.out.println(hide);
			People p = peopleService.find(hide);
			p.setState(true);
			peopleService.update(p);
		}
		message="还原操作成功";
		return "message";
	}
	
	/**
	 * 添加界面
	 * @return "add"
	 */
	public String addUI(){
		ActionContext.getContext().put("types", this.cityService.getResult()); 
		return "add";
	}
	
	/**
	 * 新闻添加
	 * @return "message"
	 */
	public String add(){
		People p = new People();
		p.setName(people.getName());
		p.setAge(people.getAge());
		p.setBirthday(people.getBirthday());
		p.setSex(people.getSex());
		p.setCity(cityService.find(city.getId()));
		peopleService.add(p);
		this.message="添加成功";
		return "message";
	}
	
	/**
	 * 新闻删除
	 * @return "message"
	 */
	public String hide(){
		People p = peopleService.find(this.getId());
		p.setState(false);
		peopleService.update(p);
		this.message="删除成功";
		return "message";
	}
	
	/**
	 * 新闻还原
	 * @return "message"
	 */
	public String back(){
		People p = peopleService.find(this.getId());
		p.setState(true);
		peopleService.update(p);
		this.message="还原成功";
		return "message";
	}
	
	/**
	 * 修改界面
	 * @return "update"
	 */
	public String updateUI(){
		ActionContext.getContext().put("people", this.peopleService.find(this.getId()));
		ActionContext.getContext().put("citys", this.peopleService.find(this.getId()).getCity());
		return "update";
	}
	
	/**
	 * 新闻修改
	 * @return "message"
	 */
	public String update(){
		People p = peopleService.find(people.getId());
		City c = cityService.find(city.getId());
		p.setAge(people.getAge());
		p.setBirthday(people.getBirthday());
		p.setCity(c);
		p.setName(people.getName());
		p.setSex(people.getSex());
		this.peopleService.update(p);
		this.message="修改成功";
		return "message";
	}
	
	public String childrens(){
		List<City> list = cityService.getSubCitys(id);
		StringBuffer sb = new StringBuffer();
		buildTreeJson(sb,list);
		ActionUtils.outputString(sb.toString(), ActionUtils.JSON, ActionUtils.ENCODING);
		return null;
	}


	private void buildTreeJson(StringBuffer sb,List<City> newsTypes){
		sb.append("[");
		for(City c:newsTypes){
			sb.append("{")
			.append("\"id\":").append(c.getId()).append(",")
			.append("\"text\":").append("\"").append(c.getCityName().replaceAll("\"", "\\\"")).append("\"").append(",");
			if(c.getChildcitys()!=null&&c.getChildcitys().size()>0){
				sb.append("\"state\":").append("\"closed\"");
			}else{
				sb.append("\"state\":").append("\"open\"");
			}
			sb.append(",\"attributes\":{\"isLeaf\":"+c.getChildcitys().isEmpty()+"}");
			sb.append("},");
		}
		if(sb.length()>1) sb.deleteCharAt(sb.length()-1);
		sb.append("]");
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getPageNo() {
		return pageNo;
	}
	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}

	public Integer[] getIds() {
		return ids;
	}

	public void setIds(Integer[] ids) {
		this.ids = ids;
	}

	public People getPeople() {
		return people;
	}

	public void setPeople(People people) {
		this.people = people;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}	
}
