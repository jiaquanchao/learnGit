package com.bothtimes.service.city.impl;

import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.bothtimes.dao.DAO;
import com.bothtimes.domain.city.City;
import com.bothtimes.service.ServiceSupport;
import com.bothtimes.service.city.CityService;

@Service
@Scope(value="prototype")
public class CityServiceImpl extends ServiceSupport<City> implements CityService{

	@Override
	@Resource(name="cityDaoImpl")
	public void setDao(DAO<City> dao) {
		super.setDao(dao);
	}
	
	public List<City> getSubCitys(Integer parentid){
		String jpql ="";
		if(parentid==null){
			jpql = "o.parent is null";
		}else{
			jpql = "o.parent.id=?";
		}
		LinkedHashMap<String,String> orderby = new LinkedHashMap<String,String>();
		orderby.put("id","asc");
		return dao.getResult(-1, -1, jpql, (parentid==null?null:new Object[]{parentid}), orderby).getQueryResult();
	}
	
	public Boolean checkTypeName(String typename,Integer depth){
		String jpql="o.cityName=? and o.depth=?";
		List<City> list = dao.getResult(-1, -1, jpql, new Object[]{typename,depth}).getQueryResult();
		return list!=null&&list.size()>0;
	}
	
	public Boolean checkTypeName(City city){
		return checkTypeName(city.getCityName(),city.getDepth());
	}
}
