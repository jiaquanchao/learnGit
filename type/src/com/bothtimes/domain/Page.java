package com.bothtimes.domain;

import java.util.List;

public class Page<T> {
	private List<T> queryResult;
	private int pageNo;//当前页码   *
	private int totalPage;//总页数  *
	private int pageSize = 10;//每页显示的记录数   *
	
	private int startIndex;//每页开始的记录索引 *
	private int totalRecords;//总记录数   *
	
	private int startPage;//开始页码
	private int endPage;//结束页码
	
	private String url;//请求的url
	
	public Page(int totalRecords,int pageNo,int pageSize){
		this.totalRecords = totalRecords;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		//计算总页数
		if(totalRecords % pageSize==0){
			totalPage = totalRecords/pageSize;
		}else{
			totalPage = totalRecords/pageSize+1;
		}
		//计算每页开始记录的索引
		startIndex = (pageNo-1)*pageSize;
		
		if(totalPage<=10){
			startPage = 1;
			endPage = totalPage;
		}else{
			startPage = pageNo-5;
			endPage = pageNo+4;
			if(pageNo<=6){
				startPage = 1;
				endPage = 10;
			}
			if(pageNo>totalPage-4){
				endPage = totalPage;
				startPage = endPage-9;
			}
		}
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}

	public int getStartPage() {
		return startPage;
	}

	public void setStartPage(int startPage) {
		this.startPage = startPage;
	}

	public int getEndPage() {
		return endPage;
	}

	public void setEndPage(int endPage) {
		this.endPage = endPage;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setQueryResult(List<T> queryResult) {
		this.queryResult = queryResult;
	}

	public List<T> getQueryResult() {
		return queryResult;
	}

}
