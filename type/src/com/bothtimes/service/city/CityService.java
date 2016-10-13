package com.bothtimes.service.city;

import java.util.List;

import com.bothtimes.domain.city.City;
import com.bothtimes.service.BaseService;

public interface CityService extends BaseService<City>{
	/**
	 * 返回下级节点
	 * @param parentid 上级节点id，为空返回根节点
	 * @return
	 */
	public List<City> getSubCitys(Integer parentid);
	
	/**
	 * 判断同级下是否存在同名分类
	 * @param typename 分类名
	 * @param depth 分类深度
	 * @return 存在返回true否则返回false
	 */
	public Boolean checkTypeName(String typename,Integer depth);
	
	/**
	 * 判断同级下是否存在同名分类
	 * @param type 分类对象
	 * @return 存在返回true否则返回false
	 */
	public Boolean checkTypeName(City city);
}
