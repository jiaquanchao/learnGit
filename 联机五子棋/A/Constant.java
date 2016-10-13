/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Constant														//常量类
{
	final String serverIp = "127.0.0.1";
	//final String serverIp = "192.168.1.101";
	//final String serverIp = "42.122.224.220";
	final int serverPort = 55555;
	final boolean isUseDatabase = false;							//控制是否使用数据库
	final int maxTables  = 100;										//最大桌子数目
	final int maxUsers  = 2 * maxTables;							//最大玩家数目
	final String STATEND = "stateEnd";								//语句结束符
	final int BUFFER_SIZE = 1024;									//ByteBuffer大小
	final int MaxImageLength = (int)(1024000 * 10);
	final double multiple = 1.0;									//窗口大小基数(标准为1.0)
	final int wsizex = m(512);										//窗口水平大小
	final int wsizey = m(364);										//窗口竖直大小
	final boolean isShowUser = true;								//是否显示服务器玩家界面(示意界面)
	final String imagepath = "image/";
	final String SysImgpath = "sys_image/";
	final Color chatColor = new Color(232,232,232);
	final int ADD = 2;												//赢棋增加分数
	final int MINUS = -2;											//输棋减少分数
	final int ESCAPEMINUS = -4;										//逃跑减少分数
	final boolean debug = true;

	public int m(int i){
		return (int)(multiple * i); 
	}

	ByteBuffer sendbuffer;
	public void sendInforBack(SocketChannel client,String message)	//向指定用户发送特定信息
	{
		try{
			message += STATEND;										//加入语句结束标志
			byte[] sendBytes = message.getBytes();					//要发送的数据
			sendbuffer = ByteBuffer.allocate(sendBytes.length);		//定义用来存储发送数据的byte缓冲区
			sendbuffer.put(sendBytes);								//将数据put进缓冲区
			sendbuffer.flip();										//将缓冲区各标志复位
			client.write(sendbuffer);								//向服务器发送数据
			System.out.println("发送：" + message);
		}
		catch (Exception e) {
			System.out.println("数据发送失败");
			if(debug){	e.printStackTrace();}
		} 
	}
}