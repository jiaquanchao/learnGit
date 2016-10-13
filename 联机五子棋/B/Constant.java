/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.awt.Color;
	
class Constant														//常量类,便于修改
{
	//final String serverIp = "192.168.1.101";
	final String serverIp = "127.0.0.1";
	//final String serverIp = "42.122.224.220";
	final int serverPort = 55555;
	final int DeskNum = 10;											//桌子总数(服务器最大接受数:100)
	final int ChairNum = DeskNum * 2;								//椅子总数
	final int perLineDesks;											//每行显示几个桌子(推荐取1或2或3)
	final double initMutiple;										//窗口默认大小基数(标准为2.0)
	final double multiple;											//当前窗口大小基数	
	final int wsizex;												//窗口水平大小
	final int wsizey;												//窗口竖直大小
	final int formerX;												//之前窗口水平大小
	final int formerY;												//之前窗口竖直大小
	final int halflength;											//半棋格大小
	final int WLen;													//棋格大小
	final int redlength;											//提醒红色十字长度
	final int maxlength;											//棋盘长度
	final int dev_x;												//棋盘距上边界位移
	final int dev_y;												//棋盘距左边界位移
	final int nowDeskPanelLength;									//桌子放置界面大小
	final String STATEND = "stateEnd";								//语句结束标志
	final int BUFFER_SIZE = 1024;									//长度至少为15(DLPImg0stateEnd)
	//final int BUFFER_SIZE = 20;									//长度至少为15(DLPImg0stateEnd)
	final int ImageBufferSize = 1024;								//发送图片buffer
	final int MaxImageLength = (int)(1024000 * 10);					//接收图片最大字节数
	final int MaxByteLength = (int)(1024000 * 10);
	final String imagepath = "image/";
	final String SysImgpath = "sys_image/";
	final String portrait_path = "sys_portrait/";
	final String TableWaitImgPath = "sys_image/a.jpg";
	final String TableStartImgPath = "sys_image/c.jpg";
	final String ChairWaitImgPath = "sys_image/b.jpg";
	final Color chatColor = new Color(232,232,232);
	final int maxChatLabelsum = 50;
	final boolean debug = true;
	final boolean isFitPosition;									//是否自动居中(最大化时不能自动居中,因为自动居中无法最大化)
	
	Constant(double IM){
		initMutiple = IM;
		multiple = initMutiple;	
		wsizex = m(512);	
		wsizey = m(364);
		formerX = wsizex;
		formerY = wsizey;
		halflength = m(10);											
		WLen = m(20);												
		redlength = m(4);											
		maxlength = m(300);											
		dev_x = m(15);												
		dev_y = m(80);		
		nowDeskPanelLength = m(340);															//m(340)为桌子放置界面初值大小,不可改动
		perLineDesks = nowDeskPanelLength / m(100);
		isFitPosition = true;
	}

	Constant(double IM,double m,int x,int y,int formerx,int formery,boolean fit,int FDL){		//用于最大化/还原 游戏大厅,修改基数、位置
		initMutiple = IM;
		multiple = m;
		wsizex = x;
		wsizey = y;
		formerX = formerx;
		formerY = formery;
		halflength = m(10);											
		WLen = m(20);												
		redlength = m(4);											
		maxlength = m(300);											
		dev_x = m(15);												
		dev_y = m(80);
		nowDeskPanelLength = wsizex - (int)(formerX*multiple/initMutiple) + (int)(FDL*multiple/initMutiple);	
		perLineDesks = nowDeskPanelLength / m(100);
		isFitPosition = fit;
	}

	public int m(int i){
		return (int)(multiple * i); 
	}

	public int extendL(int i) {
		int windowChangeLength = wsizex - (int)(formerX*multiple/initMutiple);		//窗口变化大小
		return windowChangeLength + m(i);											//现在桌子界面大小+加窗口变化大小
	}

	private ByteBuffer sbuffer;
	public void sendMessage(SocketChannel socket,String message)	//向服务器发送消息
	{
		try{
			message += STATEND;
			byte[] sendBytes = message.getBytes();					//要发送的数据
			sbuffer = ByteBuffer.allocate(sendBytes.length);		//定义用来存储发送数据的byte缓冲区
			sbuffer.put(sendBytes);									//将数据put进缓冲区
			sbuffer.flip();											//将缓冲区各标志复位
			socket.write(sbuffer);									//向服务器发送数据
			System.out.println("发送：" + message);
		}
		catch(Exception e){
			System.out.println("数据发送失败");
			if(debug){	e.printStackTrace();}
		}
	}
}