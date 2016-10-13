package com.bothtimes.domain.city;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.bothtimes.domain.people.People;

/**City 实体，生成一张city表**/
@Entity
public class City {

	/**City 的主键 自增长**/
	@Id
	@GeneratedValue
	private Integer id;
	
	/**City 的名字，长度为20不许为空**/
	@Column(length=20,nullable=false)
	private String cityName;
	
	/**City 的等级，不许为空**/
	@Column(nullable=false)
	private Integer depth=1;
	
	/**一对多 关系被维护端**/
	@OneToMany(cascade={CascadeType.REFRESH,CascadeType.REMOVE},fetch=FetchType.EAGER,
			mappedBy="city")
	private Set<People> people = new HashSet<People>();
	
	/**下级类别**/
    @OneToMany(cascade={CascadeType.REFRESH,CascadeType.REMOVE},mappedBy="parent")
    private Set<City> childcitys = new HashSet<City>();
    
    /**上级类别**/
    @ManyToOne(cascade=CascadeType.REFRESH)
    @JoinColumn(name="parent_id")
    private City parent;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public Integer getDepth() {
		return depth;
	}
	public void setDepth(Integer depth) {
		this.depth = depth;
	}
	public Set<People> getPeople() {
		return people;
	}
	public void setPeople(Set<People> people) {
		this.people = people;
	}
	public Set<City> getChildcitys() {
		return childcitys;
	}
	public void setChildcitys(Set<City> childcitys) {
		this.childcitys = childcitys;
	}
	public City getParent() {
		return parent;
	}
	public void setParent(City parent) {
		this.parent = parent;
	}
}
