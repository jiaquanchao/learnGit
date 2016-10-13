package com.bothtimes.test;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bothtimes.domain.city.City;
import com.bothtimes.domain.people.People;
import com.bothtimes.service.city.CityService;
import com.bothtimes.service.people.PeopleService;


public class PeopleAndCityTest {
	private ApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
	CityService cityService = (CityService)ctx.getBean("cityServiceImpl");
	PeopleService peopleService = (PeopleService) ctx.getBean("peopleServiceImpl");
	@Test
	public void testCityAdd(){
		City city = new City();
		city.setCityName("北京");
		cityService.add(city);
	}
	@Test
	public void testPeopleAdd(){
		City city = cityService.find(1);
		People people = new People();
		people.setName("庾雍");
		people.setAge(20);
		people.setSex("男");
		people.setCity(city);
		peopleService.add(people);
	}
	@Test
	public void testCityUpdate(){
		City city = cityService.find(1);
		city.setCityName("河北");
		cityService.update(city);
	}
	@Test
	public void testPeopleUpdate(){
		People people = peopleService.find(1);
		people.setAge(21);
		peopleService.update(people);
	}
	@Test
	public void testCityDelete(){
		cityService.delete(1);
	}
	public void testPeopleDelete(){
		peopleService.delete(1);
	}
}
