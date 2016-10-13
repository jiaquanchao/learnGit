package com.bothtimes.domain.people;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.bothtimes.domain.city.City;

/**People 实体生成一个People表**/
@Entity
public class People {
	/**People 的主键 自增长**/
	@Id
	@GeneratedValue
	private Integer id;
	
	/**People 的姓名，长度为20不许为空**/
	@Column(length=20,nullable=false)
	private String name;
	
	/**People 的年龄，长度为10不许为空**/
	@Column(length=10,nullable=false)
	private Integer age;
	
	/**People 的性别，长度为20不许为空**/
	@Column(length=20,nullable=false)
	private String sex;
	
	/**People 的生日，长度为20不许为空,默认值为当前的时间**/
	@Temporal(TemporalType.DATE)
	@Column(length=20,nullable=false)
	private Date birthday=new Date();
	
	/**People 当前的状态，长度为5不许为空，默认值是true**/
	@Column(length=5,nullable=false)
	private boolean state=true;
	
	/**People 的City People是多的一方 是维护方**/
	@ManyToOne(cascade={CascadeType.REFRESH},optional=false)
	@JoinColumn(name="city_id")
	private City city;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	public boolean isState() {
		return state;
	}
	public void setState(boolean state) {
		this.state = state;
	}
	public City getCity() {
		return city;
	}
	public void setCity(City city) {
		this.city = city;
	}
}
