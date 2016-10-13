package com.bothtimes.service.people.impl;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.bothtimes.dao.DAO;
import com.bothtimes.domain.people.People;
import com.bothtimes.service.ServiceSupport;
import com.bothtimes.service.people.PeopleService;

@Service
@Scope(value="prototype")
public class PeopleServiceImpl extends ServiceSupport<People> implements PeopleService{

	@Override
	@Resource(name="peopleDaoImpl")
	public void setDao(DAO<People> dao) {
		super.setDao(dao);
	}
}
