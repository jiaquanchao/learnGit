package com.bothtimes.service;

import java.io.Serializable;
import java.util.LinkedHashMap;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bothtimes.dao.DAO;
import com.bothtimes.domain.Page;

@Transactional
@Scope("prototype")
public abstract class ServiceSupport<T> implements BaseService<T> {
	protected DAO<T> dao;
	@Resource 
	public void setDao(DAO<T> dao) {
		this.dao = dao;
	}

	public void clear() {
		dao.clear();
	}

	public void delete(Serializable... entityids) {
		dao.delete(entityids);
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public T find(Serializable entityId) {
		return dao.find(entityId);
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public T findResultByUniqueField(String propertyName, Object propertyValue) {
		return dao.findResultByUniqueField(propertyName, propertyValue);
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public long getCount() {
		return dao.getCount();
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public long getCount(String wherejpql, Object[] queryParams) {
		return dao.getCount(wherejpql, queryParams);
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public Page<T> getResult() {
		return dao.getResult();
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public Page<T> getResult(int pageNo, int pageSize,
			LinkedHashMap<String, String> orderby) {
		return dao.getResult(pageNo, pageSize, orderby);
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public Page<T> getResult(int pageNo, int pageSize, String wherejpql,
			Object[] queryParams, LinkedHashMap<String, String> orderby) {
		return dao.getResult(pageNo, pageSize, wherejpql, queryParams, orderby);
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public Page<T> getResult(int pageNo, int pageSize, String wherejpql,
			Object[] queryParams) {
		return dao.getResult(pageNo, pageSize, wherejpql, queryParams);
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public Page<T> getResult(int pageNo, int pageSize) {
		return dao.getResult(pageNo, pageSize);
	}

	@Transactional(readOnly=true,propagation=Propagation.NOT_SUPPORTED)
	public void refresh(T entity) {
		dao.refresh(entity);
	}

	public void add(T entity) {
		dao.save(entity);
	}

	public void update(T entity) {
		dao.update(entity);
	}
	
	public Page<Object> getResultWithJPQL(int pageNo, int pageSize,
			String jpql,String countjpql, Object[] queryParams){
		return dao.getResultWithJPQL(pageNo, pageSize, jpql, countjpql, queryParams);
	}
	
	public long getCountWithJPQL(String countjpql, Object[] queryParams){
		return dao.getCountWithJPQL(countjpql, queryParams);
	}
}
