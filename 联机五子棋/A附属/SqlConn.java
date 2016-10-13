/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class SqlConn										//连接oracle数据库
{
	private String ip;
	private String username;
	private String password;
	private ResultSet rset;
	private Connection conn;
	private Statement stmt;
	public void setSql(String serverip,String orclUsername,String orclPassword)
	{
		ip = serverip;
		username = orclUsername;
		password = orclPassword;
	}
	public void tryConn()
	{
		try{
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			conn = DriverManager.getConnection(ip,username,password);
			stmt = conn.createStatement();
			System.out.println("连接成功");
		}
		catch (Exception e)
		{	e.printStackTrace();}
	}
	public ResultSet getResult(String sql)
	{
		try{
			rset=stmt.executeQuery(sql);
			return rset;
		}
		catch(SQLException sqle){
			System.out.println(sqle.toString());
			return null;
		}
	}
	public boolean updateSql(String strSQL)
	{
		try{
			stmt.executeUpdate(strSQL);
			conn.commit();
			return true;
		}
		catch(SQLException sqle){
			System.out.println(sqle.toString());
			return false;
		}
	}
	public void closeConnection()
	{
		try{
			stmt.close();
			conn.close();
		}
		catch(SQLException sqle){
			System.out.println(sqle.toString());
		}
	}
}