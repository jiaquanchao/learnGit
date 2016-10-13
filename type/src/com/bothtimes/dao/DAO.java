package com.bothtimes.dao;

import java.io.Serializable;
import java.util.LinkedHashMap;

import com.bothtimes.domain.Page;

public interface DAO<T> {

	public abstract void clear();

	public abstract void refresh(T entity);

	public abstract void delete(Serializable... entityids);

	public abstract T find(Serializable entityId);

	public abstract void save(T entity);

	public abstract long getCount();
	
	public abstract long getCount(String wherejpql, Object[] queryParams);

	public abstract void update(T entity);

	public abstract T findResultByUniqueField(String propertyName,Object propertyValue);
	
	public abstract Page<T> getResult(int pageNo, int pageSize,
			LinkedHashMap<String, String> orderby);

	public abstract Page<T> getResult(int pageNo, int pageSize,
			String wherejpql, Object[] queryParams);

	public abstract Page<T> getResult(int pageNo, int pageSize);

	public abstract Page<T> getResult();

	public abstract Page<T> getResult(int pageNo, int pageSize,
			String wherejpql, Object[] queryParams,
			LinkedHashMap<String, String> orderby);


	public abstract Page<Object> getResultWithJPQL(int pageNo, int pageSize,
			String jpql,String countjpql, Object[] queryParams);
	
	public abstract long getCountWithJPQL(String countjpql, Object[] queryParams);
}