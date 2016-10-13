/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.nio.channels.SocketChannel;		
import java.awt.Image;
class UAS														//UserAndSocketChannel(用户数据结构)用户和其对应的通道
{
	private String id = "";										//玩家的id
	private String name = "";									//玩家的昵称
	private SocketChannel userChannel = null;					//玩家的通道
	private int deskNumber = -1;								//玩家的桌子编号
	private int chair = -1;										//玩家的椅子编号
	private int uColor = -1;									//玩家的棋子颜色
	private int score;											//玩家的分数

	private boolean isFile = false;								//判断上传的是否为图片
	private boolean isFileEnd = false;							//判断图片是否上传完
	private boolean isFileSended = false;						//判断图片是否上传过
	
	private String portrait = "";
	public int imgTempNum = -1;
	
	//以下为get(),set()和清空方法
	public void clear(){
		id = "";
		name = "";
		userChannel = null;
		uColor = -1;
		deskNumber = -1;
		chair = -1;
		isFile = false;
		isFileEnd = false;
		isFileSended = false;
		portrait = "";
		score = -1;
	}

	public String getId()
	{	return id; }

	public void setId(String nId)
	{	id = nId; }

	public SocketChannel getUserChannel()
	{	return userChannel; }

	public void setUserChannel(SocketChannel channel)
	{	userChannel = channel; }

	public String getName()
	{	return name; }

	public void setName(String nName)
	{	name = nName; }

	public int getScore()
	{	return score; }

	public void setScore(int s)
	{	score = s; }

	public int getDeskNumber()
	{	return deskNumber; }

	public int getChairNumber()
	{	return chair; }

	public void setDeskNumber(int desk)
	{	deskNumber = desk; }

	public void setDeskNumber(int desk, int ch)
	{	
		deskNumber = desk; 
		chair = ch;
	}

	public void setPortrait(String p)
	{	portrait = p;}

	public String getPortrait()
	{	return portrait;}

	public int getUColor()
	{	return uColor; }

	public void setUColor(int color)
	{	uColor = color; }

	public void setAll(String nId,String nName,int s,SocketChannel channel){
		id = nId;
		name = nName;
		score = s;
		userChannel = channel;
	}

	public boolean getIsFile()
	{	return isFile;}

	public void setIsFile(boolean isf)
	{	isFile = isf; }

	public boolean getIsFileEnd()
	{	return isFileEnd;}

	public void setIsFileEnd(boolean isfe)
	{	isFileEnd = isfe; }

	public boolean getIsFileSended()
	{	return isFileSended;}

	public void setIsFileSended(boolean isfes)
	{	isFileSended = isfes; }
}