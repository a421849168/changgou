package com.etc.pojo;
import javax.persistence.*;
import java.io.Serializable;
import java.lang.String;
/****
 * @Author:sz.itheima
 * @Description:Excel构建
 * @Date 2019/6/14 19:13
 *****/
@Table(name="excel")
public class Excel implements Serializable{

    @Column(name = "id")
	private double id;//

    @Column(name = "headline")
	private String headline;//

    @Column(name = "state")
	private String state;//

    @Column(name = "starttime")
	private String starttime;//

    @Column(name = "endtime")
	private String endtime;//

    @Column(name = "with_historical")
	private String withHistorical;//

    @Column(name = "nameid")
	private String nameid;//

    @Column(name = "name")
	private String name;//

    @Column(name = "staff")
	private String staff;//

    @Column(name = "section")
	private String section;//

    @Column(name = "handlername")
	private String handlername;//

    @Column(name = "currenttime")
	private String currenttime;//

    @Column(name = "owner")
	private String owner;//

    @Column(name = "phone")
	private String phone;//

    @Column(name = "paltnumber")
	private String paltnumber;//

    @Column(name = "ditch")
	private String ditch;//

    @Column(name = "branch")
	private String branch;//

    @Column(name = "marketing")
	private String marketing;//



	//get方法
	public double getId() {
		return id;
	}

	//set方法
	public void setId(double id) {
		this.id = id;
	}
	//get方法
	public String getHeadline() {
		return headline;
	}

	//set方法
	public void setHeadline(String headline) {
		this.headline = headline;
	}
	//get方法
	public String getState() {
		return state;
	}

	//set方法
	public void setState(String state) {
		this.state = state;
	}
	//get方法
	public String getStarttime() {
		return starttime;
	}

	//set方法
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	//get方法
	public String getEndtime() {
		return endtime;
	}

	//set方法
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	//get方法
	public String getWithHistorical() {
		return withHistorical;
	}

	//set方法
	public void setWithHistorical(String withHistorical) {
		this.withHistorical = withHistorical;
	}
	//get方法
	public String getNameid() {
		return nameid;
	}

	//set方法
	public void setNameid(String nameid) {
		this.nameid = nameid;
	}
	//get方法
	public String getName() {
		return name;
	}

	//set方法
	public void setName(String name) {
		this.name = name;
	}
	//get方法
	public String getStaff() {
		return staff;
	}

	//set方法
	public void setStaff(String staff) {
		this.staff = staff;
	}
	//get方法
	public String getSection() {
		return section;
	}

	//set方法
	public void setSection(String section) {
		this.section = section;
	}
	//get方法
	public String getHandlername() {
		return handlername;
	}

	//set方法
	public void setHandlername(String handlername) {
		this.handlername = handlername;
	}
	//get方法
	public String getCurrenttime() {
		return currenttime;
	}

	//set方法
	public void setCurrenttime(String currenttime) {
		this.currenttime = currenttime;
	}
	//get方法
	public String getOwner() {
		return owner;
	}

	//set方法
	public void setOwner(String owner) {
		this.owner = owner;
	}
	//get方法
	public String getPhone() {
		return phone;
	}

	//set方法
	public void setPhone(String phone) {
		this.phone = phone;
	}
	//get方法
	public String getPaltnumber() {
		return paltnumber;
	}

	//set方法
	public void setPaltnumber(String paltnumber) {
		this.paltnumber = paltnumber;
	}
	//get方法
	public String getDitch() {
		return ditch;
	}

	//set方法
	public void setDitch(String ditch) {
		this.ditch = ditch;
	}
	//get方法
	public String getBranch() {
		return branch;
	}

	//set方法
	public void setBranch(String branch) {
		this.branch = branch;
	}
	//get方法
	public String getMarketing() {
		return marketing;
	}

	//set方法
	public void setMarketing(String marketing) {
		this.marketing = marketing;
	}


}
