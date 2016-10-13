package com.bothtimes.utils;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

public class ActionUtils {
	public static final String ENCODING = "UTF-8";
	public static final String JSON = "text/json"; 
	public static final String HTML = "text/html"; 

	public static void outputString(String outstr,String contentType,String encoding){
		HttpServletResponse response = getResponse();
		response.setHeader("Cache-Control", "no-cache");
		response.setContentType(contentType+";charset="+encoding);
		response.setCharacterEncoding(encoding);
		PrintWriter out;
		try {
			out = response.getWriter();
			try {
				out.print(outstr);
			}catch (Exception e1) {
				e1.printStackTrace();
			}finally{
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static HttpServletRequest getRequest(){
    	return ServletActionContext.getRequest();
    }
    
    public static HttpSession getSession(){
    	return getRequest().getSession();
    }
    
    public static HttpServletResponse getResponse(){
    	return ServletActionContext.getResponse();
    }
    
    public static Cookie[] getCookies(){
    	return getRequest().getCookies();
    }
    
    public static ServletContext getApplication(){
    	return ServletActionContext.getServletContext();
    }
}
