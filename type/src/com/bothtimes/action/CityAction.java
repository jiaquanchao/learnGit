package com.bothtimes.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;

import com.bothtimes.domain.city.City;
import com.bothtimes.service.city.CityService;
import com.bothtimes.service.people.PeopleService;
import com.opensymphony.xwork2.ActionContext;

public class CityAction {
	@Resource CityService cityService;
	@Resource PeopleService peopleService;
	private Integer pageNo=1;
	private Integer pageSize=10;
	private Integer id;
	private Integer ids[];
	private City city;
	private String cityName;
	private String messages;
	
	/**
	 * 城市列表
	 * @return "cityList"
	 */
	public String cityList(){
		StringBuffer wherejpql = new StringBuffer();
		if(getId()!=null){
			ActionContext.getContext().put("city", this.cityService.find(this.getId()));
			wherejpql = new StringBuffer(" o.parent.id="+getId());
		}else{
			wherejpql = new StringBuffer(" o.parent=null ");
		}
		List<Object> params = new ArrayList<Object>();
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		if(cityName!=null){
			wherejpql.append(" and cityName like ?");
			params.add("%"+cityName+"%");
		}		
		orderby.put("id", "desc");
		ActionContext.getContext().put("page", cityService.getResult(pageNo, pageSize, wherejpql.toString(), params.toArray(), orderby));
		return "cityList";
	}
	
	/**
	 * 城市添加界面
	 * @return "cityAdd"
	 */	
	public String cityAddUI(){
		if(this.getId()!=null)
			ActionContext.getContext().put("citys", this.cityService.find(this.getId()));
		return "cityAdd";
	}
	/**
	 * 类型添加
	 * @return "messages"
	 */
	public String cityAdd(){
		City c = new City();
		c.setCityName(city.getCityName());
		if(city.getDepth()!=null)
			c.setDepth(city.getDepth()+1);
		if(cityService.checkTypeName(c)){
			messages="同级下有重名分类，提交失败";
			return "messages";
		}
		if(city.getId()!=null) c.setParent(cityService.find(city.getId()));
		cityService.add(c);
		this.messages="类型添加成功";
		return "messages";
	}
	/**
	 * 子类型添加界面
	 * @return "typeAdd"
	 */
	public String cityChildAddUI(){
		ActionContext.getContext().put("citys", this.cityService.find(this.getId()));
		return "cityAdd";
	}	
	
	/**
	 * 修改界面
	 * @return "update"
	 */
	public String updateUI(){
		ActionContext.getContext().put("city", this.cityService.find(this.getId()));
		return "update";
	}
	
	/**
	 * 类型修改
	 * @return "messages"
	 */
	public String update(){
		City c = cityService.find(city.getId());
		c.setCityName(city.getCityName());
		this.cityService.update(c);
		this.messages="修改成功";
		return "messages";
	}
	
	/**
	 * 全选按钮
	 * @return message
	 */
	public String deleteBatch(){
		for(Integer delid : ids){
			cityService.delete(delid);
		}
		messages="删除成功";
		return "messages";
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

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMessages() {
		return messages;
	}

	public void setMessages(String messages) {
		this.messages = messages;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public Integer[] getIds() {
		return ids;
	}

	public void setIds(Integer[] ids) {
		this.ids = ids;
	}
}
