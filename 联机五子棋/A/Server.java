/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;  
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JLabel;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.FileReader;

class Server extends JFrame	implements ActionListener, MouseListener, MouseMotionListener 	//游戏服务器
{
	Constant c;
	private ImgTemp imgTemps[];										//临时保存玩家形象
	private SqlConn sqlConn;										//连接数据库类
	private GameTable Tables[];										//游戏桌个数
	private UAS Users[];											//玩家					
	private int userSum = 0;										//当前与服务器连接的用户
	private Selector selector;										//定义一个事件选择器对象记录套接字通道的事件
	private ServerSocketChannel ssc;								//定义一个异步服务器socket对象
	private ServerSocket ss;										//定义服务器socket对象-用来指定异步socket的监听端口等信息
	private InetSocketAddress address;								//定义存放监听端口的对象
	JTextArea showUsers = new JTextArea("");
	JScrollPane	showUsersScroll = new JScrollPane(showUsers);
	Image playerImage;
	JLabel viewersInfors[];
	String viewersInforsID[];

	Server(Constant cc)												//初始化服务器
	{
		super("用户");												//设置标题
		c = cc;														//获取常量类
		imgTemps = new ImgTemp[c.maxUsers];
		viewersInfors = new JLabel[c.maxUsers];
		viewersInforsID = new String[c.maxUsers];

		Tables = new GameTable[c.maxTables];
		for(int i = 0;i < Tables.length;i++){						//创建游戏者类数组并实例化
			Tables[i] = new GameTable(c);
			Tables[i].setUsers(Users);
			Tables[i].setServer(this);
			if(c.isUseDatabase){									//使用数据库
				Tables[i].setSqlConn(sqlConn);
			}
		}

		Users = new UAS[c.maxUsers];								//创建用户类数组并实例化
		for(int i = 0;i < Users.length;i++){
			Users[i] = new UAS();	
		}		

		for(int i = 0;i < imgTemps.length;i++){
			imgTemps[i] = new ImgTemp();
		}

		if(c.isShowUser){
			showUser();
		}

		if(c.isUseDatabase){											//使用数据库
			String connectString = "jdbc:oracle:thin:@"+ c.serverIp +":1521:orcl";
			String orclUsername = "scott";
			String orclPassword = "scott";
			sqlConn = new SqlConn();								//创建数据库交互类
			sqlConn.setSql(connectString,orclUsername,orclPassword);
		}

		try {
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);							//将此socket对象设置为异步
            ss = ssc.socket();
            address = new InetSocketAddress(c.serverPort);
            ss.bind(address);										//将服务器与这个端口绑定
            ssc.register(selector, SelectionKey.OP_ACCEPT);			//将异步的服务器socket对象的接受客户端连接事件注册到selector对象内
			System.out.println("********* 本程序作者 韩旭滨  ********");
            System.out.println("服务端端口注册完毕!");
		}
		catch (Exception e){	
			System.out.println("服务器无法启动");
			if(c.debug){	e.printStackTrace();}
			System.exit(0);
		}
		try {
			int userNumber = -1;									//获取每次循环对应用户的编号
            while(true)												//通过此循环来遍例事件
			{
                int shijian = selector.select();					//查询事件如果一个事件都没有就阻塞
                if(shijian == 0)	{	continue;	}
                ByteBuffer echoBuffer = ByteBuffer.allocate(c.BUFFER_SIZE);			//定义一个byte缓冲区来存储收发的数据
                for (SelectionKey key : selector.selectedKeys())					//此循环遍例所有产生的事件
				{
                    if(key.isAcceptable())							//如果产生的事件为接受客户端连接(当有客户端连接服务器的时候产生)
					{
                        ServerSocketChannel server = (ServerSocketChannel)key.channel();	//定义一个服务器socket通道
                        SocketChannel client = server.accept();						//将临时socket对象实例化为接收到的客户端的socket
                        client.configureBlocking(false);							//将客户端的socket设置为异步
                        client.register(selector, SelectionKey.OP_READ);			//将客户端的socket的读取事件注册到事件选择器中
						System.out.println("服务端有新连接");
                        //System.out.println("服务端有新连接:" + client);
                    }
                    else if(key.isReadable())						//如果产生的事件为读取数据(当已连接的客户端向服务器发送数据的时候产生)
					{
                        SocketChannel client = (SocketChannel)key.channel();		//客户端请求新的SocketChannel
                        echoBuffer.clear();											//先将客户端的数据清空
						int readInt = 0;											//readInt为读取到数据的长度
                        try {
                            while ((readInt = client.read(echoBuffer)) > 0){
								//if(readInt == 0 )	{ continue;}					//不会 == 0
								byte[] readByte = new byte[readInt];				//建立一个临时byte数组,将齐长度设为获取的数据的长度
								for(int i = 0;i < readInt;i++){						//循环向此临时数组中添加数据
									readByte[i]=echoBuffer.get(i);
								}
								echoBuffer.clear();									//将缓冲区清空，以便进行下一次存储数据
								client.register(selector, SelectionKey.OP_READ);
								userNumber = getUserNum(client);
								prepareParse(readByte,client,userNumber);
                            }
							if(readInt < 0)	{						//客户端关闭SocketChannel
								//System.out.println("客户端中断: " + client);
								System.out.println("客户端中断");
								userExit(client);
								client.close();
							}
                        }
						catch(Exception e){							//当客户端在读取数据操作执行之前断开连接会产生异常信息
							//System.out.println("客户端异常: " + client);
							System.out.println("客户端异常");
							if(c.debug){	e.printStackTrace();}
							userExit(client);
							key.cancel();
                            break;
                        }
                    }
                }
				selector.selectedKeys().removeAll(selector.selectedKeys());	//将全部已处理事件删除
            }
        }
        catch (Exception e){	
			System.out.println(e);
			if(c.debug){	e.printStackTrace();}
		}
	}

	public void prepareParse(byte[] readByte, SocketChannel client,int userNumber)		//预处理收到的字符串,因为可能会重叠
	{	
		String dirtyResult = new String(readByte);
		if(userNumber >= 0){
			if(!Users[userNumber].getIsFileSended()){
				imageSaveControl(readByte,client,userNumber);
			}
		}

		String results[];
		try{
			results = dirtyResult.split(c.STATEND);
		}
		catch(Exception e){	
			if(c.debug){	e.printStackTrace();}
			return; 
		}

		if(userNumber < 0 || (userNumber >= 0 && !Users[userNumber].getIsFile())) {	//(新用户登陆信息)和(已知用户非图片信息)
			for(int i = 0;i < results.length;i++){
				System.out.println("解析: " + results[i]);
				Parse(results[i],client,userNumber);	
			}
		}
	}

	public void imageSaveControl(byte[] readByte, SocketChannel client,int userNumber){	//保存玩家形象控制模块
		try{
			String dirtyResult = new String(readByte,"ISO-8859-1");
			if(dirtyResult.contains("ULMImg")){						//图片保存开始,有可能与前面的无关信息一同发过来,拆开分别处理
				System.out.println("userNumber = " + userNumber);
				Users[userNumber].setIsFile(true);
				int l = ("ULMImg" + c.STATEND).length();
				int position = dirtyResult.indexOf("ULMImg");
				String remain = dirtyResult.substring(position + l);
				if(remain.length() > 0){
					System.out.println("触发图片容错处理-起");
					byte [] remains = remain.getBytes("ISO-8859-1");
					saveImage(userNumber,remains);
					byte another[] = dirtyResult.substring(0,position).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another,client,userNumber);
					}
				}
			}
			else if(dirtyResult.contains("SMImgD")){				//图片保存结束,有可能与后面的无关信息一同发过来,拆开分别处理
				Users[userNumber].setIsFile(false);
				Users[userNumber].setIsFileSended(true);
				int l = ("SMImgD" + c.STATEND).length();
				int position = dirtyResult.indexOf("SMImgD");
				String remain = dirtyResult.substring(0,position);
				if(remain.length() > 0){
					System.out.println("触发图片容错处理-止");
					byte [] remains = remain.getBytes("ISO-8859-1");
					saveImage(userNumber,remains);
					byte another[] = dirtyResult.substring(position + l).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another,client,userNumber);
					}
				}
				if(Users[userNumber].imgTempNum != -1){						//玩家形象不为空
					System.out.println("接收图片大小:" + imgTemps[Users[userNumber].imgTempNum].getTempLength());
					try{													//保存玩家形象到硬盘
						File fWrite = new File(c.imagepath + Users[userNumber].getId() + ".PNG");
						FileOutputStream out = new FileOutputStream(fWrite);
						out.write(imgTemps[Users[userNumber].imgTempNum].getNewFiles(),0,imgTemps[Users[userNumber].imgTempNum].getTempLength());
						out.close();
					}
					catch(Exception e){
						System.out.println(e);
						if(c.debug){	e.printStackTrace();}
					}
					imgTemps[Users[userNumber].imgTempNum].userNumber = -1;
					imgTemps[Users[userNumber].imgTempNum].newFiles = null;	//清空ImgTemps
					imgTemps[Users[userNumber].imgTempNum].setTempLength(0);//清零长度计数器
				}
				Users[userNumber].setIsFile(false);
				Users[userNumber].setIsFileSended(true);
				String result = "setIsFileSended;true";
				c.sendInforBack(client,result);
			}

			if(Users[userNumber].getIsFile()){								//图片保存过程中
				if((!dirtyResult.contains("ULMImg")) && (!dirtyResult.contains("SMImgD"))) {
					saveImage(userNumber,readByte);
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
			if(c.debug){	e.printStackTrace();}
		}
	}

	public void saveImage(int userNumber, byte[] readByte){			//保存玩家形象图片readByte片到临时ImgTemps
		if(readByte.length <= 0)return;
		for(int j = 0; j < c.maxUsers; j++){
			if(Users[userNumber].imgTempNum == -1){					//没有对应临时ImgTemps,分配一个新的
				for(int k = 0; k < c.maxUsers; k++){
					if(imgTemps[k].userNumber == -1){
						imgTemps[k].userNumber = userNumber;
						Users[userNumber].imgTempNum = k;
						imgTemps[k].newFiles = new byte[c.MaxImageLength];
						System.out.println("k = "+k);
						break;
					}
				}
			}
		}
		if(imgTemps[Users[userNumber].imgTempNum].newFiles == null){//临时ImgTemps未初始化,初始化
			imgTemps[Users[userNumber].imgTempNum].newFiles = new byte[c.MaxImageLength];
		}
		imgTemps[Users[userNumber].imgTempNum].setNewFiles(readByte,readByte.length);
	}
	
	public void Parse(String str, SocketChannel client ,int userNumber)		//解析客户端发来的请求
	{
		if(str.equals("") || str == null) return;
		boolean isok = false;
		String [] message = null;
		try{
			message = str.split(";");
		}
		catch(Exception e){	
			if(c.debug){	e.printStackTrace();}
			return; 
		}
		if(message.length > 0){
			//分析字符串，并做相应处理
			if(message[0].equals("login")&&message.length == 3)		//用户登录消息
			{
				//格式："login;userName;password"
				doUserLogin(message,client);
			}
			else if(message[0].equals("regist"))					//用户注册消息
			{
				//格式："regist;id;name;password"
				regist(client,message[1],message[2],message[3]);
			}
			else if(message[0].equals("sitdown"))					//用户发出坐下请求(已废弃)
			{
				//格式："sitdown;桌子编号"
				int deskNumber = Integer.parseInt(message[1]);
				int i = getUserNum(client);
				Users[i].setDeskNumber(deskNumber);
				System.out.println("isStart = " + Tables[deskNumber].getIsStart());
				if(Tables[deskNumber].getIsStart()){
					String result = "AreadyStart";
					c.sendInforBack(client,result);
				}
			}
			else if(message[0].equals("sitdown2"))					//用户发出坐下请求
			{
				//格式："sitdown;桌子编号;座位编号;头像名"
				int deskNumber = Integer.parseInt(message[1]);
				int chairNumber = Integer.parseInt(message[2]);

				GameTable t = Tables[deskNumber];

				if(!t.seat[chairNumber].userId.equals("")&&!t.getIsStart()){
					String result = "NoSit;";
					c.sendInforBack(client,result);
					return;											//已经有人，且游戏未开始，不可坐下，不可旁观
				}
				
				int i = getUserNum(client);

				if(deskNumber < 0)									//第一个用户坐下时，给桌号赋值
				{	t.setTableNumber(deskNumber);}
				UAS	Viewers[] = t.getViewers();
				Seat[] seat = t.getSeat();

				if(!t.getIsStart()){								//游戏玩家
					for(int j = 0;j < Viewers.length; j++)			//记录在线用户id,name和SocketChannel
					{
						if(Viewers[j].getId().equals("")){			
							t.seat[chairNumber].set(Users[i].getId(),Users[i].getName(),message[3]); 	
							broadcast(seatState());					//刷新大厅座位情况
							Viewers[j] = Users[i];
							String result = "ackSit;" + deskNumber + ";" + "NotStart";
							StepRecord stepRecords[] = t.getStepRecords();
							c.sendInforBack(client,result);			//回送信息
							System.out.println("用户 "+ Viewers[j].getName() + "进入" + deskNumber + "号房间");
							break;
						}
					}
				}
				else if(t.getIsStart()){							//旁观玩家
					for(int j = 0;j < Viewers.length; j++)			//记录在线用户id,name和SocketChannel
					{
						if(Viewers[j].getId().equals("")){			
							Viewers[j] = Users[i];
							String result = "ackSit;" + deskNumber + ";";
							StepRecord stepRecords[] = t.getStepRecords();	//获取房间落子情况
							int step = t.getStep();
							result += "AreadyStart;" + step + ";";
							for(int k = 0;k < step;k++){
								result += stepRecords[k].getX() + "," + stepRecords[k].getY() + "," + stepRecords[k].getColor() + ";";
							}
							c.sendInforBack(client,result);			//回送信息

							System.out.println("用户 "+Viewers[j].getName()+"进入"+deskNumber+"号房间");
							break;
						}
					}
				}

				t.setViewerSum(t.getViewerSum() + 1);				//桌子观察人数增1
				t.refreshViewersInfor();							//桌子观察者刷新提醒
				Users[i].setDeskNumber(deskNumber,chairNumber);
			}
			else if(message[0].equals("initGameHallOk"))			//玩家初始化成功
			{
				refreshGameHallPlayers();
				c.sendInforBack(client,seatState());	
			}
			else if(message[0].equals("viewPlayersInfor"))			//获取指定玩家信息
			{
				String imageName =c.imagepath + message[1]+".PNG";
				String from = "";
				if(message[2].equals("GameHall")){
					from = "0";
				}
				else{
					from = "1";
				}
				int n = 0;
				try{
					c.sendInforBack(client,"DLPImg" + from);

					File file =new File(imageName);
					System.out.println(imageName + " 文件长度:" + file.length());
					if(file.length() > c.MaxImageLength){
						System.out.println("文件长度过长");//
						c.sendInforBack(client,"ImgIllegal" + from);
						return;
					}
					FileInputStream  fr = new FileInputStream (file);
					byte[] b = new byte[1024];
					ByteBuffer sendbuffer; 
					while ((n = fr.read(b)) > 0) {	
						sendbuffer = ByteBuffer.wrap(b,0,n);  
						client.write(sendbuffer);
						sendbuffer.flip();
						Thread.sleep(3);	//Thread.sleep(3);
					}
					fr.close();
					c.sendInforBack(client,"LPImgD");
				}
				catch(Exception e){
					System.out.println(e);
					if(c.debug){	e.printStackTrace();}
					System.out.println("数据发送失败");
					c.sendInforBack(client,"ImgIllegal" + from);
				}
			}
			else if(message[0].equals("setMyPortrait"))				//设置玩家头像
			{
				int i = getUserNum(client);
				Users[i].setPortrait(message[1]);
			}
			else if(message[0].equals("saveDone"))					//保存复盘完成
			{
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].refreshGamersInforPartly(client);	
				Tables[deskNumber].refreshViewersInforPartly(client);
				c.sendInforBack(client,seatState());
			}
			else if(message[0].equals("initOppoPictOk"))			//玩家初始化对手图片成功
			{
				refreshGameHallPlayersPartly(client);
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].setIOPS(Tables[deskNumber].getIOPS() + 1);	//控制尽量不重复上传图片
			}
			else if(message[0].equals("getSeatState"))				//玩家独自请求刷新大厅座位情况
			{
				c.sendInforBack(client,seatState());	
			}
			else if(message[0].equals("started"))					//游戏已经开始，落子信息
			{
				//格式："started;桌子编号;棋盘横坐标;棋盘纵坐标;棋子颜色"
				int deskNumber = Integer.parseInt(message[1]);
				int i = getUserNum(client);
				Users[i].setDeskNumber(deskNumber);
				//System.out.println(Users[i].getDeskNumber());
				if(Tables[deskNumber].checkSetDown(message,client)){	
					broadcast(seatState());	
				}
			}
			else if(message[0].equals("ready"))						//用户准备游戏信息
			{
				//格式："ready;桌子编号"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].doReadyGame(client);
				if(Tables[deskNumber].getIsStart()){				//游戏开始会更新桌子图片
					broadcast(seatState());	
				}
			}
			else if(message[0].equals("userMessage"))				//用户向其他用户们发送消息
			{
				//格式："userMessage;桌子编号;要发送的内容"
				int deskNumber = Integer.parseInt(message[1]);
				int index = str.indexOf(";");
				index = str.indexOf(";", index + 1);
				String mInfor = str.substring(index + 1);
				if(mInfor.equals(""))	return;
				Tables[deskNumber].doChatting(mInfor,client);
			}
			else if(message[0].equals("userBroadcastMessage"))		//用户向其他所有大厅用户发送消息
			{
				//格式："userBroadcastMessage;要发送的内容"
				int index = str.indexOf(";");
				String mInfor = str.substring(index + 1);
				if(mInfor.equals(""))	return; 
				doBroadcastChatting(mInfor,client);
			}
			else if(message[0].equals("userSeparateChatMessage"))	//用户向单独用户发送消息
			{
				//格式："userBroadcastMessage;userId;要发送的内容"
				String userId = message[1];
				int index = str.indexOf(";");
				index = str.indexOf(";", index + 1);
				String mInfor = str.substring(index + 1);
				if(mInfor.equals(""))	return; 
				doBroadcastChatting(mInfor,client,userId);
			}
			else if(message[0].equals("closeTable"))				//用户向其他用户们发送消息
			{
				//格式："closeTable;桌子编号"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].viewerExit(client);
				resetUserSeat(client);
				broadcast(seatState());								//刷新大厅座位情况
			}
			else if(message[0].equals("rollbackRequest"))			//用户悔棋请求
			{
				//格式："rollbackRequest;桌子编号;step"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].rollbackForward(message[2],client);
			}
			else if(message[0].equals("replyRBForward"))			//用户应答悔棋请求
			{
				//格式："replyRBForward;桌子编号;reply;step"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].doRollback(Integer.parseInt(message[2]),Integer.parseInt(message[3]),client);
			}
			else if(message[0].equals("refreshGamersInforPartly"))	//用户获取更新信息
			{
				//格式："refreshGamersInforPartly;桌子编号"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].refreshGamersInforPartly(client);
			}
			else if(message[0].equals("refreshViewersInforPartly"))	//用户获取更新信息
			{
				//格式："refreshViewersInforPartly;桌子编号"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].refreshViewersInforPartly(client);
			}
			else if(message[0].equals("admitLose"))					//用户认输
			{
				//格式："admitLose;桌子编号;执棋颜色"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].admitLose(message[2]);
				broadcast(seatState());
			}
			else if(message[0].equals("drawRequest"))				//用户和棋请求
			{
				//格式："drawRequest;桌子编号;"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].drawRequest(client);
			}
			else if(message[0].equals("drawRequestReply"))			//用户应答和棋请求
			{
				//格式："drawRequestReply;桌子编号;reply"
				int deskNumber = Integer.parseInt(message[1]);
				if(Tables[deskNumber].doDraw(client , Integer.parseInt(message[2]))){
					broadcast(seatState());
				}
			}
			else if(message[0].equals("addFriendRequest"))			//用户添加好友请求
			{
				//格式："addFriendRequest;userId"
				int i = getUserNum(client);
				int j = getUserNumByUserId(message[1]);
				if(isExsitFriend(Users[i].getId(),Users[i].getName(),Users[j].getId(),Users[j].getName())){
					c.sendInforBack(Users[i].getUserChannel(),"friendExsit;");
					return;
				}
				c.sendInforBack(Users[j].getUserChannel(),"addFriendRequest;" +Users[i].getId() + ";"+ Users[i].getName());
			}
			else if(message[0].equals("agreeAddFriend"))			//同意用户添加好友
			{
				//格式："agreeAddFriend;userId"
				int j = getUserNum(client);
				int i = getUserNumByUserId(message[1]);
				addFriend(Users[i].getId(),Users[i].getName(),Users[j].getId(),Users[j].getName());
				c.sendInforBack(Users[j].getUserChannel(),"addFriendAgree;" +Users[i].getId() + ";"+ Users[i].getName());
				c.sendInforBack(Users[i].getUserChannel(),"addFriendAgree;" +Users[j].getId() + ";"+ Users[j].getName());
			}
			else if(message[0].equals("viewFriends"))				//查看好友
			{
				//格式："viewFriends;"
				int i = getUserNum(client);
				c.sendInforBack(client,getFriends(Users[i].getId()));
			}
			else if(message[0].equals("delFriend"))					//删除好友
			{
				//格式："delFriend;userId;userName"
				int i = getUserNum(client);
				int j = getUserNumByUserId(message[1]);
				delFriend(Users[i].getId(),Users[i].getName(),message[1],message[2]);
				c.sendInforBack(client,"delSuccess;");
				if(j >= 0){
					c.sendInforBack(Users[j].getUserChannel(),"delSuccess;" + Users[i].getName());
				}
			}
		}
	}

	public void doUserLogin(String []message,SocketChannel client)	//用户登陆，查询数据库，获取用户信息
	{
		boolean isTrueUser = false;
		boolean isAlreadyLogin = false;
		String userId = message[1];
		String pswd = message[2];
		String userName = "";
		String Score = "0";

		if(c.isUseDatabase){	//使用数据库
			sqlConn.tryConn();
			String sql = "select * from login where userId='" + userId + "'and upassword='" + pswd + "'";
			ResultSet rs=sqlConn.getResult(sql);
			try{
				while (rs.next()){
					userId = rs.getString(1);
					userName = rs.getString(2);
					Score = rs.getInt(4) + "";
					isTrueUser = true;
				}
				sqlConn.closeConnection();
			}
			catch(Exception e){
				System.out.println(e);
				if(c.debug){	e.printStackTrace();}
			}
		}

		else{														//不使用数据库
			try{
				FileReader filein = new FileReader("src/users.txt");
				BufferedReader br = new BufferedReader(filein);
				String temp = "";
				while((temp = br.readLine()) != null) {
					try{
						//System.out.println("配置文件内容:" + temp);
						String dlls[] = temp.split(";");
						for(int i = 0 ; i < dlls.length ; i++){
							dlls[i] = dlls[i].trim();
						}
						//id;name;password;socre;
						if(dlls[0].equals(userId)){
							if(dlls[2].equals(pswd)){
								Score = dlls[3];
								userName = dlls[1];
								isTrueUser = true;
								break;
							}
						}
					}
					catch(Exception e){
						continue;
					}
				}
			}
			catch (Exception ee){	
				System.out.println(ee);
				if(c.debug){	ee.printStackTrace();}
			}

			String strRegex = "[\u4e00-\u9fa5a-zA-Z0-9]*";			//汉字字母数字
			Pattern p = Pattern.compile(strRegex);
			Matcher m = p.matcher(userId); 
			if(!userId.matches(strRegex)){
				isTrueUser = false;
			} 

		
		}

		if(userId != null){
			String result = "";
			if(isTrueUser){ 
				result = "ack;" + userId + "," + userName + "," + Score;
				for(int i = 0;i < Users.length;i++){
					if(Users[i].getId().equals(userId)){
						result="nak_reLogin";
						isAlreadyLogin = true;
						System.out.println("不可重复登录");
					}
				}
				if(!isAlreadyLogin){
					for(int i = 0; i < Users.length;i++){
						if(Users[i].getId().equals("")){
							Users[i].setAll(userId,userName,Integer.parseInt(Score),client);	//记录在线用户id,name和SocketChannel
							System.out.println("用户 "+Users[i].getName() + " 登录" + "  Id:" + userId + " 分数:" + Score);
							break;
						}
					}
					userSum++;
				}
				if(c.isShowUser){
					refreshShowUser();
				}
			}
			else{													//用户认证失败
				result = "nak";
				System.out.println("认证失败");
			}
			c.sendInforBack(client,result);			
		}
	}

	public void regist(SocketChannel client,String id, String name ,String password){
		if(c.isUseDatabase){}
		else{
			try{
				File txt = new File("src/users.txt");				
				FileReader filein = new FileReader("src/users.txt");
				BufferedReader br = new BufferedReader(filein);
				String temp = null;
				
				while((temp = br.readLine()) != null) {
					String dlls[] = temp.split(";");
						for(int i = 0 ; i < dlls.length ; i++){
							dlls[i] = dlls[i].trim();
						}
						//id;password;socre;name
						if(dlls[0].equals(id)){
							c.sendInforBack(client,"idRepeat;");
							return;
						}
				}
				String record = id + ";" + name + ";" + password + ";0;" + "\r\n";
				byte []contents = (record).getBytes();
			
				FileOutputStream out = new FileOutputStream(txt,true);		//添加
				out.write(contents);
				out.close();
				System.out.println("新用户注册");
				c.sendInforBack(client,"registSuccess;");
			}
			catch (Exception e){	
				System.out.println(e);
				if(c.debug){	e.printStackTrace();}
			}
		}
	}

	public String getFriends(String userId){
		String friends = "Myfriends;";
		if(c.isUseDatabase){}
		else{
			try{
				File txt = new File("src/friends.txt");				
				FileReader filein = new FileReader("src/friends.txt");
				BufferedReader br = new BufferedReader(filein);
				String temp = null;
				
				while((temp = br.readLine()) != null) {
					String dlls[] = temp.split(";");
					for(int i = 0 ; i < dlls.length ; i++){
						dlls[i] = dlls[i].trim();
					}
					if(dlls[0].equals(userId)){
						friends += dlls[1]+";";
					}
				}
			}
			catch (Exception e){	
				System.out.println(e);
				if(c.debug){	e.printStackTrace();}
			}
		}
		return friends;
	}

	public void delFriend(String iId,String iName, String jId ,String jName){
		if(c.isUseDatabase){}
		else{
			String record = "";
			try{
				FileReader filein = new FileReader("src/friends.txt");
				BufferedReader br = new BufferedReader(filein);
				String temp = null;
				
				while((temp = br.readLine()) != null) {
					if(temp.equals(iId + ";" + jId +","+ jName)||temp.equals(jId + ";" + iId  +","+ iName)){
					}
					else{
						record += temp + "\r\n";
					}
				}
				File txt = new File("src/friends.txt");				//向配置文件写入头像信息,新信息顶置
				FileOutputStream out = new FileOutputStream(txt);	//重写
				byte []contents = (record).getBytes();
				out.write(contents);
				out.close();
				System.out.println("删除好友");
			}
			catch (IOException ee)
			{	System.out.println(ee);}
		}
	}


	public boolean isExsitFriend(String iId,String iName, String jId ,String jName){
		try{
			FileReader filein = new FileReader("src/friends.txt");
			BufferedReader br = new BufferedReader(filein);
			String temp = null;
			while((temp = br.readLine()) != null) {
				String dlls[] = temp.split(";");
				for(int i = 0 ; i < dlls.length ; i++){
					dlls[i] = dlls[i].trim();
				}
				if(dlls[0].equals(iId)&&dlls[1].equals(jId + "," + jName)){
					return true;
				}
			}
		}
		catch (IOException ee)
		{	System.out.println(ee);}
		return false;
	}

	public void addFriend(String iId,String iName, String jId ,String jName){
		if(c.isUseDatabase){}
		else{

			if(isExsitFriend(iId, iName, jId, jName)){
				return ;
			}
			try{
				File txt = new File("src/friends.txt");				//向配置文件写入头像信息,新信息顶置
				FileOutputStream out = new FileOutputStream(txt,true);		//添加
				String record = iId + ";" + jId +","+ jName + "\r\n" + jId + ";" + iId  +","+ iName + "\r\n";
				byte []contents = (record).getBytes();
				out.write(contents);
				out.close();
				System.out.println("添加好友");
			}
			catch (IOException ee)
			{	System.out.println(ee);}
		}
	}

	public void userExit(SocketChannel client)						//处理用户退出客户端
	{
		int i = getUserNum(client);
		int deskNumber = -1;
		int chairNumber = -1;
		if(i >= 0){
			deskNumber = Users[i].getDeskNumber();
			chairNumber = Users[i].getChairNumber();
		}
		else{
			System.out.println("以游客身份离开");
			return;
		}
		if(deskNumber >= 0){	
			System.out.println("逃跑房间号:"+deskNumber);
			Tables[deskNumber].viewerExit(client);
		}
		if(chairNumber >= 0){
			if(Tables[deskNumber].seat[chairNumber].userId.equals(Users[i].getId())){
				Tables[deskNumber].seat[chairNumber].clear();
			}
		}

		System.out.println("用户"+Users[i].getName()+"下线");
		Users[i].clear();
		userSum--;
		broadcast(seatState());										//刷新大厅座位情况
		refreshGameHallPlayers();
		if(c.isShowUser){
			refreshShowUser();
		}
	}

	public void refreshShowUser(){									//刷新玩家显示情况
		showUsers.setText("");
		showUsers.removeAll();
		for(int i = 0,sum = 0; i < Users.length;i++){
			if(!Users[i].getId().equals("")){
				int perLength = 10;									//ID格式化长度
				String userFormatId = Users[i].getId();
				int idLenth = userFormatId.getBytes().length;
				if(idLenth > 8){
					userFormatId = userFormatId.substring(0,3)+"…";
					idLenth = userFormatId.getBytes().length;
				}
				String idPos = "";
				for(int j= 0;j < perLength - idLenth;j++){
					idPos += "  ";
				}
				String infor = "    ID " + userFormatId + idPos  + "昵称 " + Users[i].getName() ;
				viewersInforsID[sum] = userFormatId;
				viewersInfors[sum] = new JLabel(infor);
				viewersInfors[sum].setBounds(0,18 * sum,217,18);
				showUsers.add(viewersInfors[sum]);
				showUsers.append("\r\n");
				viewersInfors[sum].addMouseListener(this);
				sum ++;
			}
		}
		JScrollBar bar = showUsersScroll.getVerticalScrollBar();
		bar.setValue(bar.getMaximum());
	}

	public void resetUserSeat(SocketChannel client){				//清空座位中的玩家信息
		int i = getUserNum(client);
		int deskNumber = -1;
		int chairNumber = -1;
		if(i >= 0){
			deskNumber = Users[i].getDeskNumber();
			chairNumber = Users[i].getChairNumber();
		}
		if(deskNumber >= 0&&chairNumber >= 0){	
			if(Tables[deskNumber].seat[chairNumber].userId.equals(Users[i].getId())){
				Tables[deskNumber].seat[chairNumber].clear();
			}
		}
	}

	public String seatState(){										//生成座位信息
		//System.out.println("生成座位信息");
		String seatState = "seatState;";
		for(int j = 0; j < Tables.length; j ++){
			for(int k = 0 ; k < 2 ; k++)
			if(!Tables[j].seat[k].userId.equals("")){
				seatState = seatState + j + "↓";
				seatState = seatState + k + "↓";
				seatState = seatState + Tables[j].seat[k].userId + "↓";
				seatState = seatState + Tables[j].seat[k].userName + "↓";
				seatState = seatState + Tables[j].seat[k].pictName + "↓";
				seatState = seatState + Tables[j].getIsStart() + "↑";
			}
		}
		return seatState;
	}

	public int getUserNum(SocketChannel client)						//根据client获取用户在用户数组Users[]中的编号
	{
		for(int i = 0;i < Users.length;i++)		{
			if(!Users[i].getId().equals("")){
				if(Users[i].getUserChannel().isConnected()){
					if(Users[i].getUserChannel().equals(client)){
						return i;
					}
				}
			}
		}
		return -1;
	}

	public void doBroadcastChatting(String infor , SocketChannel client){	//大厅聊天
		System.out.println("大厅聊天:");
		String userName = "";
		String userId = "";
		int userNum = getUserNum(client);							//获取用户编号
		userName = Users[userNum].getName();
		userId = Users[userNum].getId();
		String userMessage = "userBroadcastMessage;1;"+ userId + ";" + userName + ";" +infor;
		for(int i = 0;i < Users.length; i++) {						//遍历服务器所有SocketChannel，将信息发给可用SocketChannel
			if(!Users[i].getId().equals("")){
				if(Users[i].getUserChannel().isConnected()){		//向全部用户发送信息，包括自己
					c.sendInforBack(Users[i].getUserChannel(),userMessage);
				}
				else												//用户与服务器端口连接，置空用户信息						
				{	Users[i].clear();	}
			}
		}
	}

	public void doBroadcastChatting(String infor , SocketChannel client, String targetUserId){	//用户私聊
		System.out.println("用户私聊:");
		String userName = "";
		String userId = "";
		int userNum = getUserNum(client);							//获取用户编号
		userName = Users[userNum].getName();
		userId = Users[userNum].getId();
		String userMessage = "userBroadcastMessage;2;"+ userId + ";" + userName + ";" +infor;
		for(int i = 0;i < Users.length; i++) {						//遍历服务器所有SocketChannel，将信息发给确定SocketChannel
			if(Users[i].getId().equals(targetUserId)){
				if(Users[i].getUserChannel().isConnected()){		//向指定用户发送信息
					c.sendInforBack(Users[i].getUserChannel(),userMessage);
					break;
				}
				else												//用户与服务器端口连接，置空用户信息						
				{	Users[i].clear();	}
			}
		}
		c.sendInforBack(client,userMessage);							//发给 消息的发送者
	}

	public void refreshGameHallPlayers()							//向所有用户发送大厅玩家信息		
	{
		broadcast(createGameHallPlayersInfor());
	}

	public void refreshGameHallPlayersPartly(SocketChannel client)	//向特定用户发送大厅玩家信息
	{
		c.sendInforBack(client,createGameHallPlayersInfor());
	}

	public String createGameHallPlayersInfor(){						//生成大厅玩家信息 userId,usreName,color,score
		String uId = "";
		String uName = "";
		String infor = "refreshGameHallPlayers;";
		int ucolor = -1;
		String userPortrait = "";
		for(int i = 0;i < Users.length; i++){
			if(!Users[i].getId().equals("")){
				uId = Users[i].getId();
				uName = Users[i].getName();
				ucolor = Users[i].getUColor();
				int score = Users[i].getScore();
				userPortrait = Users[i].getPortrait();
				infor += uId + "↓" + uName + "↓" + ucolor + "↓" + score + "↓" + userPortrait + "↑";
			}
		}
		return infor;
	}

	public int getScoresByUserId(String Id)							//通过Id获取用户分数
	{
		int userId = Integer.parseInt(Id);
		int userScores = 0;


		if(c.isUseDatabase){										//使用数据库
			sqlConn.tryConn();
			String sql = "select scores from login where userid='" + userId + "'";
			ResultSet rs = sqlConn.getResult(sql);
			try{
				while (rs.next())
				{	userScores = rs.getInt(1);	}
				sqlConn.closeConnection();
			}
			catch(Exception e){
				System.out.println(e);
				if(c.debug){	e.printStackTrace();}
			}
		}

		return userScores;
	}

	public int getUserNumByUserId(String Id)						
	{
		for(int i = 0;i < Users.length; i++){
			if(Users[i].getId().equals(Id)){
				return i;
			}
		}
		return -1;
	}

	public void broadcast(String infor)								//向所有在线用户广播信息，全服务器广播
	{
		System.out.println("大厅广播:");
		for(int i = 0;i < Users.length; i++)						//遍历服务器所有SocketChannel，将信息发给可用SocketChannel
		{
			if(!Users[i].getId().equals("")){
				//System.out.println(Users[i].getId());
				if(Users[i].getUserChannel().isConnected()){
					c.sendInforBack(Users[i].getUserChannel(),infor);
				}
				else												//用户与服务器端口连接，清空用户信息
				{	Users[i].clear();	}
			}
		}
	}

	public void showUser(){											//打开 显示所有玩家 界面

		try {
			String src = c.SysImgpath + "default.png";		
			Image image=ImageIO.read(this.getClass().getResource(src));
			this.setIconImage(image);								//设置图标
		}
		catch (Exception e) {
			System.out.println(e);
			if(c.debug){	e.printStackTrace();}
		}

		ImageIcon img = new ImageIcon(c.SysImgpath + "bg5.jpg");
		JLabel bgLabel = new JLabel(img);
		bgLabel.setBounds(0,0,c.wsizex,c.wsizey);
		this.getLayeredPane().add(bgLabel, new Integer(Integer.MIN_VALUE));
		((JPanel)getContentPane()).setOpaque(false);

		setLayout(null);
		setResizable(false);
		setVisible(true);

		showUsersScroll.setBounds(c.m(30), c.m(50), c.m(220), c.m(260));	//"用户显示框"
		add(showUsersScroll);
		showUsers.setOpaque(true);
		showUsers.setBackground(c.chatColor);
		showUsers.setEditable(false); 

		this.setBounds(160,0,c.wsizex,c.wsizey);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				try{
					dispose();
				}
				catch(Exception ee){
				}
			}
		});
	}

	public void setImage(String userId){							//在 所有玩家 显示界面,画出所选玩家 形象
		ImageIcon icon = new ImageIcon("image/" + userId + ".png");
		icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(),
		icon.getIconHeight(), Image.SCALE_DEFAULT));
		playerImage = icon.getImage();
		paint(this.getGraphics());
	}

	public void actionPerformed(ActionEvent e){}

	public void mouseClicked(MouseEvent e)				
	{
		if(e.getModifiers() != 16) return;							//只接收左击事件
		for(int i = 0 ; i < viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){
				setImage(viewersInforsID[i]);
			}
		}
	}
	public void mouseEntered(MouseEvent e){ 
		for(int i=0 ; i<viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){
				viewersInfors[i].setOpaque(true);
				viewersInfors[i].setBackground(new Color(48,117,174)); 
			}
		}
	}
	public void mouseExited(MouseEvent e) {
		for(int i=0 ; i<viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){
				 viewersInfors[i].setBackground(c.chatColor);		//灰色
			}
		}
	} 
	public void mouseReleased(MouseEvent e){ }
	public void mouseDragged(MouseEvent e){ }
	public void mouseMoved(MouseEvent e){ }
	public void mousePressed(MouseEvent e) { } 
	public void paint(Graphics g)									//绘制容器,画棋盘,窗口变化会重新执行
	{
		super.paintComponents(g);
		g.drawImage(playerImage,300 ,80 ,90 ,130 ,this); 
	}
}

class ImgTemp														//临时保存玩家形象
{
	int userNumber = -1;
	byte []newFiles;												//玩家形象图片,最大1.8406M
	int tempLength = 0;												//图片上传过程中分段,段长度

	public void setNewFiles(byte[] readByte, int length){
		for (int i= tempLength; i< tempLength + length; i++ ){
			newFiles[i] = readByte[i - tempLength];
		}
		tempLength += length;
	}

	public byte[] getNewFiles(){
		return newFiles;
	}

	public int getTempLength()
	{	return tempLength; }

	public void setTempLength(int templ)
	{	tempLength = templ; }
}