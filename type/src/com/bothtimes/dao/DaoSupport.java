package com.bothtimes.dao;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.bothtimes.domain.Page;
import com.bothtimes.utils.GenericsUtil;


@SuppressWarnings("unchecked")
public abstract class DaoSupport<T> implements DAO<T>{
	protected Class<T> entityClass = GenericsUtil.getSuperClassGenricType(this.getClass());
	@PersistenceContext protected EntityManager em;
	
	public void clear(){
		em.clear();
	}
	
	public void refresh(T entity){
		em.refresh(entity);
	}

	public void delete(Serializable ... entityids) {
		for(Object id : entityids){
			em.remove(em.getReference(this.entityClass, id));
		}
	}
	
	public T find(Serializable entityId) {
		if(entityId==null) throw new RuntimeException(this.entityClass.getName()+ ":传入的实体id不能为空");
		return em.find(this.entityClass, entityId);
	}
	
	public void save(T entity) {
		em.persist(entity);
	}
	
	public long getCount() {
		return (Long)em.createQuery("select count("+ getCountField(this.entityClass) +") from "+ getEntityName(this.entityClass)+ " o").getSingleResult();
	}
	
	public long getCount(String wherejpql, Object[] queryParams) {
		String jpql = "select count("+ getCountField(this.entityClass) +") from "+ getEntityName(this.entityClass)+ " o "+(wherejpql==null || "".equals(wherejpql.trim())? "": "where "+ wherejpql);
		Query query = em.createQuery(jpql);
		setQueryParams(query,queryParams);
		return (Long) query.getSingleResult();
	}
	
	public void update(T entity) {
		em.merge(entity);
	}
	
	public T findResultByUniqueField(String propertyName,Object propertyValue){
		Page<T> page = getResult(-1,-1,propertyName+"=?",new Object[]{propertyValue});
		return page.getQueryResult().size()>0?page.getQueryResult().get(0):null;
	}

	public Page<T> getResult(int pageNo, int pageSize, LinkedHashMap<String, String> orderby) {
		return getResult(pageNo,pageSize,null,null,orderby);
	}
	
	public Page<T> getResult(int pageNo, int pageSize, String wherejpql, Object[] queryParams) {
		return getResult(pageNo,pageSize,wherejpql,queryParams,null);
	}
	
	public Page<T> getResult(int pageNo, int pageSize) {
		return getResult(pageNo,pageSize,null,null,null);
	}
	
	public Page<T> getResult() {
		return getResult(-1, -1);
	}

	public Page<T> getResult(int pageNo, int pageSize,
			String wherejpql, Object[] queryParams,LinkedHashMap<String, String> orderby) {
		String entityname = getEntityName(this.entityClass);
		Query query = em.createQuery("select count("+ getCountField(this.entityClass)+ ") from "+ entityname+ " o "+(wherejpql==null || "".equals(wherejpql.trim())? "": "where "+ wherejpql));
		setQueryParams(query, queryParams);
		int totalCount =((Long) query.getSingleResult()).intValue();
		Page pg = new Page<T>(totalCount, pageNo, pageSize);
		
		query = em.createQuery("select o from "+ entityname+ " o "+(wherejpql==null || "".equals(wherejpql.trim())? "": "where "+ wherejpql)+ buildOrderby(orderby));
		setQueryParams(query, queryParams);
		if(pageNo > 0 && pageSize > 0) query.setFirstResult((pageNo - 1) * pageSize).setMaxResults(pageSize);
		pg.setQueryResult(query.getResultList());
		return pg;
	}
	
	public Page<Object> getResultWithJPQL(int pageNo, int pageSize,
			String jpql,String countjpql, Object[] queryParams) {
		int totalCount = (int) getCountWithJPQL(countjpql,queryParams);
		Page<Object> pg = new Page<Object>(totalCount, pageNo, pageSize);

		Query query = em.createQuery(jpql);
		setQueryParams(query, queryParams);
		if(pageNo > 0 && pageSize > 0) query.setFirstResult((pageNo - 1) * pageSize).setMaxResults(pageSize);
		pg.setQueryResult((List<Object>) query.getResultList());
		return pg;
	}
	
	public long getCountWithJPQL(String countjpql, Object[] queryParams){
		Query query = em.createQuery(countjpql);
		setQueryParams(query, queryParams);
		List list = query.getResultList();
		if(list==null||list.size()==0) return 0;
		return (Long) list.get(0);
	}

	protected static void setQueryParams(Query query, Object[] queryParams){
		if(queryParams!=null && queryParams.length>0){
			for(int i=0; i<queryParams.length; i++){
				query.setParameter(i+1, queryParams[i]);
			}
		}
	}
	/**
	 * 组装order by语句
	 * @param orderby
	 * @return
	 */
	protected static String buildOrderby(LinkedHashMap<String, String> orderby){
		StringBuffer orderbyql = new StringBuffer("");
		if(orderby!=null && orderby.size()>0){
			orderbyql.append(" order by ");
			for(String key : orderby.keySet()){
				orderbyql.append("o.").append(key).append(" ").append(orderby.get(key)).append(",");
			}
			orderbyql.deleteCharAt(orderbyql.length()-1);
		}
		return orderbyql.toString();
	}
	/**
	 * 获取实体的名称
	 * @param <E>
	 * @param clazz 实体类
	 * @return
	 */
	protected static <E> String getEntityName(Class<E> clazz){
		String entityname = clazz.getSimpleName();
		Entity entity = clazz.getAnnotation(Entity.class);
		if(entity.name()!=null && !"".equals(entity.name())){
			entityname = entity.name();
		}
		return entityname;
	}
	/**
	 * 获取统计属性,该方法是为了解决hibernate解析联合主键select count(o) from Xxx o语句BUG而增加,hibernate对此jpql解析后的sql为select count(field1,field2,...),显示使用count()统计多个字段是错误的
	 * @param <E>
	 * @param clazz
	 * @return
	 */
	protected static <E> String getCountField(Class<E> clazz){
		String out = "o";
		try {
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			for(PropertyDescriptor propertydesc : propertyDescriptors){
				Method method = propertydesc.getReadMethod();
				if(method!=null && method.isAnnotationPresent(EmbeddedId.class)){					
					PropertyDescriptor[] ps = Introspector.getBeanInfo(propertydesc.getPropertyType()).getPropertyDescriptors();
					out = "o."+ propertydesc.getName()+ "." + (!ps[1].getName().equals("class")? ps[1].getName(): ps[0].getName());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return out;
	}
}
