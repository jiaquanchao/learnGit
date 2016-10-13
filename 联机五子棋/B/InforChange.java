/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.io.File;

class InforChange implements Runnable								//客户端主线程，运行到用户下线为止
{
	private Constant c;
	private SocketChannel clientChannel;
	private Selector selector;
	private SocketAddress address;
	private boolean openLogin = false;								//设置只弹出一次登录窗口
	private boolean isLogin=false;									//判断用户是否已经登录
	private boolean isOpenWin=false;								//设置用户只能打开一个游戏窗口
	private String userId;											//用户账户
	private String userName;										//用户昵称
	private int score;												//用户分数
	private LoginJFrame login = null;								//登陆界面 指针
	private RegistJFrame regist;									//注册界面 指针
	private Wuziqi wuziqi;											//游戏界面 指针
	private GameHall gameHall;										//游戏大厅 指针
	private ViewFriends viewFriends = null;							//好友界面 指针
	private boolean isHaveOppoImage = false;						//是否已有对家形象图片
	private boolean isPlayerImage = false;							//是否正在接受图片
	private byte []newPlayerFiles;											
	private int playerTempLength = 0;								//图片长度
	private boolean isMyFileSended = false;							//图片是否传完
	private boolean isOverBufferSizeLength = false;					//语句是否超过c.BUFFER_SIZE容量
	private byte [] overLengthStatementBytes;						//用于保存超长语句的字节数组//overLengthStatementBytes

	public void setRegistJFrame(RegistJFrame rjf){
		regist = rjf;
	}

	public void setViewFriends(ViewFriends vf){
		viewFriends = vf;
	}

	public boolean getIsMyFileSended()
	{	return isMyFileSended;}

	public void setIsMyFileSended(boolean isfes)
	{	isMyFileSended = isfes; }

	public void setNewPlayerFiles(byte[] readByte, int length){		//保存图片,图片分块传来,组装
		try{
			for (int i= playerTempLength; i< playerTempLength + length; i++ ){
				newPlayerFiles[i] = readByte[i - playerTempLength];
			}
			playerTempLength += length;
		}
		catch(Exception e){
			System.out.println("图片保存失败" + e);
		}
	}

	public byte[] getNewPlayerFiles(){
		return newPlayerFiles;
	}
	
	public void setUser(String uId,String uName,int sc)
	{
		userId=uId;
		userName=uName;
		score=sc;
	}

	public String getUserId()
	{	return userId ; }

	public String getUserName()
	{	return userName ; }

	public int getScore()
	{	return score ; }

	public void setOpenLogin(boolean open)
	{ openLogin = open; }

	InforChange(Constant cc)										//建立与服务器的连接
	{
		c = cc;
		overLengthStatementBytes = new byte[c.MaxByteLength];		//用于保存超长语句的字节数组//overLengthStatementBytes
		newPlayerFiles = new byte[c.MaxImageLength];
		openLogin = true;
		try{
			selector = Selector.open();								//定义一个记录套接字通道事件的对象
			address = new InetSocketAddress(c.serverIp, c.serverPort);		//定义一个服务器地址的对象
			clientChannel = SocketChannel.open(address);					//定义异步客户端
			clientChannel.configureBlocking(false);					//将客户端设定为异步
			clientChannel.register(selector, SelectionKey.OP_READ);	//在轮讯对象中注册此客户端的读取事件
			System.out.println("成功连接到服务器");
			run();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	int pictsum = 0;

	public void newLoginJFrame(String id){							//打开登录界面
		login = new LoginJFrame(c);
		login.setChannel(clientChannel);
		login.setInforChange(this);
		openLogin = false;
		login.startClient();
		login.setIdtext(id);
	}

	public void run()												//用户客户端长时间监听服务器是否有消息发来，该SocketChannel保持一直连接状态
	{
		try{
			if(openLogin){		
				newLoginJFrame("");
			}
			ByteBuffer readBuffer = ByteBuffer.allocate(c.BUFFER_SIZE);
			int readInt = 0;
			while (true) {
				//System.out.println("监听开始");
				if (!clientChannel.isOpen())	{					//如果客户端连接没有打开就退出循环
					System.out.println("监听失败");
					break;
				}
				int shijian = selector.select();					//此方法为查询是否有事件发生如果没有就阻塞,有的话返回事件数量
				//if (shijian==0) {	continue;	}					//如果没有事件返回循环			//会出错，千万不要用
				SocketChannel sc;									//定义一个临时的客户端socket对象
				for (final SelectionKey key : selector.selectedKeys())	//遍例所有的事件
				{
					if (key.isReadable())	{						//如果本事件的类型为read时,表示服务器向本客户端发送了数据
						sc = (SocketChannel) key.channel();			//将临时客户端对象实例为本事件的socket对象
						readBuffer.clear();							//将缓冲区清空以备下次读取
						try {
							while ((readInt = sc.read(readBuffer)) > 0)	{	//此循环从本事件的客户端对象读取服务器发送来的数据到缓冲区中
								//System.out.println("监听");
								byte[] readByte = new byte[readInt];	//建立一个临时byte数组,将齐长度设为获取的数据的长度
								for(int i = 0;i < readInt;i++)			//循环向此临时数组中添加数据
								{	readByte[i] = readBuffer.get(i);	}
								prepareParse(readByte);
								readBuffer.clear();						//将缓冲区清空以备下次读取
								readBuffer.flip();
							}
							if(readInt < 0){
								System.out.println("服务器连接关闭!");
							}
						}
						catch(Exception ee){
							System.out.println("服务器连接关闭!");
							System.exit(0);								//根据服务器关闭,是否同时关闭客服端,不需要可注释掉
						}
					}
				}
				selector.selectedKeys().remove(selector.selectedKeys());
				//System.out.println("监听结束");  
			}
		}
		catch (Exception e)
		{	System.out.println(e);	} 
	}	

	public void prepareParse(byte[] readByte)						//服务器发来的语句进行预处理,防止两次不同的语句同时发来
	{	
		String dirtyResult = new String(readByte);
		imageSavePlayerControl(readByte);							//全部 图片传输 控制模块
		
		String results[];
		try{
			results = dirtyResult.split(c.STATEND);
		}
		catch(Exception e)
		{	return; }

		if(!isPlayerImage) {										//(新用户登陆信息)和(已知用户非图片信息)(即数据不是图片)
			if(overLengthStatementBytesLength != 0&&isRemain){
				//System.out.println("剩余并入: " + OLSBtoString() +"与"+new String(readByte));
				readByte = preAdd(overLengthStatementBytes,overLengthStatementBytesLength,readByte);
				dirtyResult = new String(readByte);
				results = dirtyResult.split(c.STATEND);
				overLengthStatementBytesLength = 0;
				isRemain = false;
			}

			if(dirtyResult.endsWith(c.STATEND)){					//未超长的语句
				for(int i = 0;i < results.length;i++){				//语句正常结束
					System.out.println("解析: " + results[i]);
					Parse(results[i]);	
				}
			}
			else if(!dirtyResult.endsWith(c.STATEND)){				//语句超过c.BUFFER_SIZE容量,语句超长特殊处理
				isOverBufferSizeLength = true;
			}

			if(isOverBufferSizeLength){								//超长累加过程中
				overLengthStatementBytesAdd(readByte);
				//System.out.println("累加" + OLSBtoString());		//分片语句累加(直接把byte数组转化为String输出,末尾可能失真乱码)
				if(OLSBtoString().contains(c.STATEND)){
					overLengthStatementParse();
				}
			}
		}
	}

	/*************	语句超长处理 模块	*************/
	public void overLengthStatementParse(){							//语句超长解析
		String results[];
		try{
			results = OLSBtoString().split(c.STATEND);
		}
		catch(Exception e)
		{	return; }
		if(OLSBtoString().endsWith(c.STATEND)){
			for(int i = 0;i < results.length;i++){					//语句正常结束
				System.out.println("超长解析: " + results[i]);
				Parse(results[i]);	
			}
			overLengthStatementBytesLength = 0;						//超长语句处理结束,清空
		}
		else if(!OLSBtoString().endsWith(c.STATEND)){				//(已然是加和后的语句)语句包含下一条语句,该语句未结束(即前一部分剩余)
			for(int i = 0;i < results.length - 1;i++){				//只处理完整语句,留下最后一个不完整的
				Parse(results[i]);	
			}
			isRemain = true;
			String aa = results[results.length - 1];
			int position = OLSBtoISOString().lastIndexOf(c.STATEND);
			byte remainBytes [] = new byte[overLengthStatementBytesLength - position - c.STATEND.length()];
			for(int i = 0 ; i< remainBytes.length ; i ++){
				remainBytes [i] = overLengthStatementBytes[position + c.STATEND.length() + i];
			}
			//System.out.println("超长剩余:" + new String(remainBytes));

			overLengthStatementBytesLength = 0;						//超长语句可识别部分处理结束,
			overLengthStatementBytesAdd(remainBytes);				//超长剩余留给后面,和后面语句加和(以上保证剩余部分不失真)
		}
		isOverBufferSizeLength = false;
	}

	public boolean isRemain = false;
	int overLengthStatementBytesLength = 0;							//数组长度

	public void overLengthStatementBytesAdd(byte[] addBytes){		//将分片语句的byte[]组装加和.防止语句加和失真,不用String而用原始byte[]
		for(int i = overLengthStatementBytesLength ;i< overLengthStatementBytesLength + addBytes.length; i++ ){
			overLengthStatementBytes[i] = addBytes[i - overLengthStatementBytesLength];
		}
		overLengthStatementBytesLength += addBytes.length;
	}

	public String OLSBtoString(){									//将超长语句数组转化为字符串(末尾可能失真,此方法只用于判断和打印,不做语句加和)
		return new String(overLengthStatementBytes,0,overLengthStatementBytesLength);
	}

	public String OLSBtoISOString(){								//用于在byte[]中转化为String后精确定位字符位置,但产生的String不可读			
		String newString  = "";
		try{
			newString  = new String(overLengthStatementBytes,0,overLengthStatementBytesLength,"ISO-8859-1");
		}
		catch(Exception e){
			System.out.println(e);
			e.printStackTrace();
		}
		return newString;
	}

	public byte[] preAdd(byte[] addBytes,int addLength ,byte[] origBytes){			//语句有剩余(本该属于下一条语句),加到下一语句的前面
		byte newBytes[] = new byte[origBytes.length + addLength];
		for(int i = 0 ;i < addLength; i++){
			newBytes[i] = addBytes[i];
		}
		for(int i = addLength ;i < newBytes.length; i++){
			newBytes[i] = origBytes[i - addLength];
		}
		return newBytes;
	}

	/*************	图片传输 控制模块	*************/
	String from = "";
	public void imageSavePlayerControl(byte[] readByte){			//图片传输 控制模块
		//byte数据范围为-128~127，编码后，原本字节流中信息大于127的话，将其转换成String类型，
		//再转换为byte[]，会与原始字节码不一致。默认会用'?'代替，
		//如0xC9，无法表示成字符，进行byte[]->String->byte[]，就会变成0x3F('?')。
		try{
			String dirtyResult = new String(readByte,"ISO-8859-1");	
			
			if(dirtyResult.contains("DLPImg")){						//downLoadPlayersImage

				isPlayerImage  = true;								//检测到图片正在发送,开始保存图片
				int l = ("DLPImg" + c.STATEND).length();	
				int position = dirtyResult.indexOf("DLPImg");
				int StrLen = "DLPImg".length();
				String remain = dirtyResult.substring(position + l + 1);				//额外from变量占一位
				from = dirtyResult.substring(position + StrLen,position + StrLen +1);	//额外from变量占一位
				if(from.equals("0")){
					gameHall.setWaitForReply(true);					//大厅里,查看玩家形象
				}
				else if(from.equals("1")){
					wuziqi.setWaitForReply(true);					//桌子里,查看玩家形象
				}
				else if(from.equals("9")){							//开始游戏时,传送的对手图片
					wuziqi.setWaitForReply(true);					//桌子操作锁定,等待图片传送
				}
				//System.out.println("from = " + from);
				if(remain.length() > 0){
					System.out.println("触发图片容错处理-起");
					byte [] remains = remain.getBytes("ISO-8859-1");
					//System.out.println("dirtyResult ="+dirtyResult);
					setNewPlayerFiles(remains,remains.length);
					byte another[] = dirtyResult.substring(0,position).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another);
					}
				}
			}
			else if(dirtyResult.contains("LPImgD")){				//loadPlayersImageDone
				isPlayerImage = false;
				if(from.equals("0")){
					gameHall.setWaitForReply(false);				//大厅里,查看玩家形象
				}
				else if(from.equals("1")){
					wuziqi.setWaitForReply(false);					//桌子里,查看玩家形象
				}
				else if(from.equals("9")){							//开始游戏时,传送的对手图片
					wuziqi.setWaitForReply(false);
					isHaveOppoImage = true;
				}
				int l = ("LPImgD" + c.STATEND).length();
				int position = dirtyResult.indexOf("LPImgD");
				String remain = dirtyResult.substring(0,position);
				if(remain.length() > 0){
					System.out.println("触发图片容错处理-止");
					byte [] remains = remain.getBytes("ISO-8859-1");
					setNewPlayerFiles(remains,remains.length);
					byte another[] = dirtyResult.substring(position + l).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another);
					}
				}
				System.out.println("接收图片大小:" + playerTempLength);
				playerTempLength = 0;
				if(from.equals("0")){								//大厅里,查看玩家形象
					gameHall.getPlayerInfor().setImage(newPlayerFiles);
				}
				else if(from.equals("1")){							//桌子里,查看玩家形象
					wuziqi.playerInfor.setImage(newPlayerFiles);
				}
				else if(from.equals("9")){							//开始游戏时,传送的对手图片
					wuziqi.setOppoImageByte(newPlayerFiles);
					wuziqi.initOppoImage();
				}
				newPlayerFiles = new byte[c.MaxImageLength];		//清空,以免共用时错用
			}

			if(isPlayerImage){
				if((!dirtyResult.contains("DLPImg")) && (!dirtyResult.contains("LPImgD"))) {
					setNewPlayerFiles(readByte,readByte.length);
				}
			}

			if(dirtyResult.contains("ImgIllegal")){					//出错处理
				isPlayerImage  = false;		
				playerTempLength = 0;
				int l = ("ImgIllegal" + c.STATEND).length();	
				int position = dirtyResult.indexOf("ImgIllegal");
				int StrLen = "ImgIllegal".length();
				String ImgIllegal = dirtyResult.substring(position + StrLen,position + StrLen +1);	//额外ImgIllegal变量占一位
				if(from.equals("0")){
					gameHall.setWaitForReply(false);	
					gameHall.addToChatLabel("系统提示: 玩家形象获取失败");
					gameHall.getPlayerInfor().setImage(new byte[1]);
				}
				else if(from.equals("1")){
					wuziqi.setWaitForReply(false);	
					wuziqi.addToChatLabel("系统提示: 玩家形象获取失败");
					wuziqi.playerInfor.setImage(new byte[1]);
				}
				if(from.equals("9")){										
					wuziqi.setWaitForReply(false);
					wuziqi.addToChatLabel("系统提示: 玩家形象获取失败");
					wuziqi.setOppoImageByte(new byte[1]);
					wuziqi.initOppoImage();
				}
			}
		}
		catch(Exception e){
			System.out.println(e);
			e.printStackTrace();
		}
	}

	/*************	字符串解析 模块	*************/
	public void Parse(String result)								//解析字符串
	{
		if(result.equals("")||result==null) return;
		boolean isok=false;
		String [] message = null;
		try{
			message = result.split(";");
		}
		catch(Exception e){ 
			System.out.println(e);
			return; 
		}
		if(message.length>0){
			String []serverInfor = result.split(";");
			if(serverInfor[0].equals("ack")){						//同意登录,进入大厅启动锁定状态
				if(serverInfor.length < 2)	return;
				if(!isLogin){
					String []userInfor = serverInfor[1].split(",");
					login.dispose();								//正常登录，关闭登录窗口
					gameHall =new GameHall(c);
					gameHall.setChannel(clientChannel);
					//System.out.println(userInfor[0] + " " + userInfor[1] + " " + userInfor[2]);				//用户ID 用户名 用户分数
					setUser( userInfor[0],userInfor[1],Integer.parseInt(userInfor[2]));
					gameHall.setUser(userInfor[0],userInfor[1],userInfor[2]);
					isLogin=true;
				}
				gameHall.showMyportrait();							//大厅显示头像
				gameHall.initMyImageName();							//大厅上传形象到服务器
				gameHall.setIsMyFileSended(isMyFileSended);			//形象上传完成
				gameHall.setIsWaiting(false);						//大厅锁定状态解除
				c.sendMessage(clientChannel,"initGameHallOk;");
			}
			else if(serverInfor[0].equals("nak"))					
			{
				login.hint.setText("用户名密码错误");
				login.setPasstext("");
			}
			else if(serverInfor[0].equals("nak_reLogin"))					
			{
				login.hint.setText("不可重复登录");
				login.setPasstext("");
			}
			else if(serverInfor[0].equals("start"))					//游戏开始 格式:"start";color;
			{
				if(serverInfor.length < 2)	return;
				wuziqi.start(serverInfor[1]);
			}
			else if(serverInfor[0].equals("ackSit"))				//同意坐下 格式:"ackSit";tableNumber;["AreadyStart"]
			{
				if(serverInfor.length < 3)	return;
				if(isOpenWin)
				{ return; }
				wuziqi = new Wuziqi(c);								//桌子启动,无需锁定用户操作
				wuziqi.setChannel(clientChannel);
				wuziqi.setUser(getUserId(),getUserName(),getScore());
				wuziqi.setTableNumber(Integer.parseInt(serverInfor[1]));
				wuziqi.setMyImageName(gameHall.getMyImageName());
				wuziqi.init();
				wuziqi.myInfor.setText(userName + "  " + score);
				if(serverInfor[2].equals("AreadyStart")){
					wuziqi.setColor(-2);
					wuziqi.startGame.setEnabled(false);
					wuziqi.admitLose.setEnabled(false);
					wuziqi.retract.setEnabled(false);
					wuziqi.draw.setEnabled(false);
					wuziqi.initGame(result);
					wuziqi.setIsStart(true);
				}
				isOpenWin = true;
				gameHall.setIsSitted(true);
				gameHall.setWaitForReply(false);					//请求坐下时大厅等待锁定解除
			}
			else if(serverInfor[0].equals("NoSit"))					//不同意坐下 格式:"NoSit;"
			{
				if(serverInfor.length < 1)	return;
				gameHall.setWaitForReply(false);					//请求坐下时大厅等待锁定解除
				gameHall.addToChatLabel("游戏提示: 该位置已有玩家");
			}
			else if(serverInfor[0].equals("idRepeat"))				//账号重复 格式:"idRepeat;"
			{
				if(serverInfor.length < 1)	return;
				try{
					regist.hint.setText("该账号已被注册");
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else if(serverInfor[0].equals("registSuccess"))			//注册成功 格式:"registSuccess;"
			{
				if(serverInfor.length < 1)	return;
				try{
					regist.newLoginJFrame(regist.getIdtext().getText().trim());
					login.hint.setText("账号注册成功");
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else if(serverInfor[0].equals("seatState"))				//更新大厅玩家信息座位情况
			{
				if(serverInfor.length > 1){
					gameHall.refreshSeatState(serverInfor[1]);
				}
				else{
					gameHall.refreshSeatState("none");
				}
			}
			else if(serverInfor[0].equals("refreshGameHallPlayers"))//更新大厅玩家信息
			{
				if(serverInfor.length < 2)	return;
				gameHall.refreshGameHallPlayers(serverInfor[1]);
			}
			else if(serverInfor[0].equals("readyGame"))				//玩家准备游戏
			{
				if(serverInfor.length < 2)	return;
				wuziqi.addToChatLabel("游戏提示:" + serverInfor[1] + " 准备游戏");
			}
			else if(serverInfor[0].equals("initGame"))				//旁观，游戏已经开始，进行初始化
			{
				wuziqi.initGame(result);
			}
			else if(serverInfor[0].equals("located"))				//游戏开始，发送落子位置及棋子颜色
			{	
				if(serverInfor.length < 4)	return;
				int xi = Integer.parseInt(serverInfor[1]);
				int yj = Integer.parseInt(serverInfor[2]);
				int set_color = Integer.parseInt(serverInfor[3]);
				wuziqi.setDown(xi, yj , set_color);
			}
			else if(serverInfor[0].equals("gameEnd"))				//游戏结束
			{
				if(serverInfor.length < 6)	return;
				wuziqi.lblWin.setText(wuziqi.startColor(Integer.parseInt(serverInfor[1])) + "赢了!"); 
				wuziqi.addToChatLabel("游戏提示: " + wuziqi.startColor(Integer.parseInt(serverInfor[1])) + "赢了");
				wuziqi.SetWinLineRecord(serverInfor[2],serverInfor[3],serverInfor[4],serverInfor[5]);
				wuziqi.drawWinLine();
				wuziqi.gameEnd();
			}
			else if(serverInfor[0].equals("gameDraw"))				//游戏结束
			{
				wuziqi.lblWin.setText("平局!"); 
				wuziqi.addToChatLabel("游戏提示: 平局");
				wuziqi.gameEnd();
			}
			else if(serverInfor[0].equals("mycolor"))				//获取自己棋子颜色
			{
				if(serverInfor.length < 2)	return;
				int user_color = Integer.parseInt(serverInfor[1]);
				wuziqi.setColor(user_color);
			}
			else if(serverInfor[0].equals("gamerExited"))
			{
				isHaveOppoImage = false;							//重要,不然会打印出图片byte[]
				wuziqi.oppsInfor.setText("");
				wuziqi.lblWin.setText("玩家离开了游戏");
				wuziqi.setIsHaveOppoImage(false);
				wuziqi.oppoImage = null;
				wuziqi.myPaint();
				wuziqi.gameEnd();
			}
			else if(serverInfor[0].equals("userMessage"))			//桌子玩家发来信息
			{
				int index = result.indexOf(";");
				index = result.indexOf(";", index + 1);
				index = result.indexOf(";", index + 1);
				String mInfor = result.substring(index + 1);
				if(mInfor.equals(""))	return; 
				wuziqi.getMessage(serverInfor[1],serverInfor[2],mInfor);			//userMessage;userId;userName ;infor
			}
			else if(serverInfor[0].equals("userBroadcastMessage"))	//玩家发来大厅广播信息
			{
				int index = result.indexOf(";");
				index = result.indexOf(";", index + 1);
				index = result.indexOf(";", index + 1);
				index = result.indexOf(";", index + 1);
				String mInfor = result.substring(index + 1);
				if(mInfor.equals(""))	return; 
				gameHall.getBroadcastMessage(serverInfor[1],serverInfor[2],serverInfor[3],mInfor);	//userBroadcastMessage;type;userId;userName ;infor
			}
			else if(serverInfor[0].equals("reSetIsOpenWin"))
			{
				isOpenWin=false;
				gameHall.setIsSitted(false);
				isHaveOppoImage = false;							//重要,不然会打印出图片byte[]
			}
			else if(serverInfor[0].equals("rollbackForward"))		//对家悔棋请求
			{
				if(serverInfor.length < 2)	return;
				wuziqi.replyRBForward(serverInfor[1]);
			}
			else if(serverInfor[0].equals("rollbackReply"))			//服务器回退命令
			{
				if(serverInfor.length < 4)	return;
				if(serverInfor[1].equals("yes")){
					wuziqi.rollback(Integer.parseInt(serverInfor[2]),Integer.parseInt(serverInfor[3]));
				}
				else if(serverInfor[1].equals("no")){
					wuziqi.noRollback(Integer.parseInt(serverInfor[2]));
				}
			}
			else if(serverInfor[0].equals("drawRequest"))			//对家和棋请求
			{
				wuziqi.drawRequest();
			}
			else if(serverInfor[0].equals("noDraw"))				//对家拒绝和棋
			{
				wuziqi.addToChatLabel("游戏提示: 对方拒绝和棋");
				wuziqi.setWaitForDrawReply(false);
				wuziqi.draw.setEnabled(true);
			}
			else if(serverInfor[0].equals("refreshGamersInfor"))	//刷新玩家信息(分数)
			{
				if(serverInfor.length < 2)	return;
				if(serverInfor.length == 3)
				{	wuziqi.refreshGamersInfor(serverInfor[1],serverInfor[2]);}
				if(serverInfor.length == 2)							//玩家逃跑，剩一人
				{	wuziqi.refreshGamersInfor(serverInfor[1]);}
			}
			else if(serverInfor[0].equals("refreshViewersInfor"))	//刷新所有观察者信息(分数)
			{
				if(serverInfor.length < 2)	return;
				wuziqi.refreshViewersInfor(serverInfor[1]);
			}
			else if(serverInfor[0].equals("setIsFileSended"))		//游戏开始，发送落子位置及棋子颜色
			{	
				if(serverInfor.length < 2)	return;
				boolean isMyFileSended = Boolean.parseBoolean(serverInfor[1]);
				setIsMyFileSended(isMyFileSended);
			}
			else if(serverInfor[0].equals("addFriendRequest"))		//添加好友请求
			{	
				if(serverInfor.length < 3)	return;
				gameHall.addFriendRequest(serverInfor[1],serverInfor[2]);
			}
			else if(serverInfor[0].equals("addFriendAgree"))		//添加好友请求
			{	
				if(serverInfor.length < 3)	return;
				gameHall.addToChatLabel("游戏提示: 您成功添加 " + serverInfor[2] + "(" + serverInfor[1] + ") 为好友");
			}
			else if(serverInfor[0].equals("Myfriends"))				//好友信息
			{	
				if(serverInfor.length < 1)	return;
				if(viewFriends != null){
					return;
				}
				viewFriends = new  ViewFriends(c,result);
				viewFriends.setInforChange(this);
				viewFriends.setGameHall(gameHall);
			}
			else if(serverInfor[0].equals("friendExsit"))			//好友已经存在
			{
				gameHall.addToChatLabel("游戏提示: 对方已在您的好友列表中");
			}
			else if(serverInfor[0].equals("delSuccess"))			//好友删除信息
			{
				if(serverInfor.length == 1){
					gameHall.addToChatLabel("游戏提示: 好友删除成功");
				}
				if(serverInfor.length == 2){
					gameHall.addToChatLabel("游戏提示: " + serverInfor[1] +" 将您从好友中删除");
				}
			}	
			else{
				//System.out.println("无效语句");
			}
		}
	} 
}