/*
 * ����:	 �����
 * QQ:	 	 714670841
 * ����:	 714670841@qq.com
 * ��������:EditPlus
 * Copyright 2014 ����� 
 * ����Ʒֻ���ڸ���ѧϰ���о������ͣ�ת����ע��������
 */

import java.nio.channels.SocketChannel;		
import java.awt.Image;
class UAS														//UserAndSocketChannel(�û����ݽṹ)�û������Ӧ��ͨ��
{
	private String id = "";										//��ҵ�id
	private String name = "";									//��ҵ��ǳ�
	private SocketChannel userChannel = null;					//��ҵ�ͨ��
	private int deskNumber = -1;								//��ҵ����ӱ��
	private int chair = -1;										//��ҵ����ӱ��
	private int uColor = -1;									//��ҵ�������ɫ
	private int score;											//��ҵķ���

	private boolean isFile = false;								//�ж��ϴ����Ƿ�ΪͼƬ
	private boolean isFileEnd = false;							//�ж�ͼƬ�Ƿ��ϴ���
	private boolean isFileSended = false;						//�ж�ͼƬ�Ƿ��ϴ���
	
	private String portrait = "";
	public int imgTempNum = -1;
	
	//����Ϊget(),set()����շ���
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