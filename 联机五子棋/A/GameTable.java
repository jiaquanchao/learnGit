/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;

class GameTable
{
	Constant c;
	private SqlConn sqlConn;
	private Server server;
	private int tableNumber = -1;
	private UAS Gamers[];											//当前正在游戏的用户  Gamers[0]为持白子一方 ， Gamers[1]为持黑子一方
	private UAS Viewers[];											//所有坐下的用户
	private UAS Users[];											//大厅玩家(目前无用)	
	private int viewerSum = 0;										//旁观者个数(包括玩家)
	private int gamerSum = 0;										//玩家个数
	private int winner = -1;										//赢棋者执子颜色
	private int color = 1;											// 旗子的颜色标识(先手) 0:白子 1:黑子 
	private int turn_around = 1;									// 交换先手
	private int step = 0;											//当前步数
	private int isStartSum = 0;										//准备开始游戏人数，判断是否开始游戏
	private boolean isStart = false;								// 游戏开始标志
	private StepRecord stepRecords[] = new StepRecord[225];			//记录玩家所有落子信息
	private int bodyArray[][] = new int[16][16];					// 设置棋盘棋子状态 0 无子 1 白子 2 黑子
	private static ByteBuffer sBuffer = ByteBuffer.allocate(1024);	//接受数据缓冲区
	public Seat seat[] = new Seat[2];								//桌子的左、右两个座位
	private int initOppoPictOkSum = 0;								//初始化对家形象成功的玩家数目

	public void setUsers(UAS[] uas){
		Users = uas;
	}

	public void setServer(Server s){
		server = s;
	}

	public int getIOPS(){
		return initOppoPictOkSum;
	}
	public void setIOPS(int IOPS){
		initOppoPictOkSum = IOPS;
	}
	public boolean getIsStart(){
		return isStart;
	}
	public int getTableNumber(){
		return tableNumber;
	}
	public void setTableNumber(int tNum){
		tableNumber = tNum;
	}
	public UAS[] getViewers(){
		return	Viewers;
	}
	public Seat[] getSeat(){
		return seat;
	}
	public int getViewerSum(){
		return viewerSum;
	}
	public void setViewerSum(int vSum){
		viewerSum = vSum;
	}
	public StepRecord[] getStepRecords(){
		return stepRecords;
	}
	public int getStep(){
		return step;
	}

	GameTable(Constant cc){
		c = cc;														//获取常量类
		Gamers = new UAS[2];
		for(int i = 0;i < Gamers.length; i++)						//创建游戏者类数组并实例化
		{	Gamers[i] = new UAS();	}
		Viewers = new UAS[5+2];
		for(int i = 0;i < Viewers.length; i++)						//创建观察者类数组并实例化
		{	Viewers[i] = new UAS();	}
		for(int i = 0; i < stepRecords.length ; i++)
		{	stepRecords[i] = new StepRecord();	}					//创建数组并实例化，该数组记录游戏过程，用于回退
		for(int i = 0 ; i <seat.length; i++){
			seat[i] = new Seat();
		}
		gameInit();
	}

	public void setSqlConn(SqlConn sqlconn){
		sqlConn = sqlconn;
	}

	public void clearColor(){										//游戏结束清楚玩家执子颜色,以待重新分配。基本颜色不变,用于显示 准备游戏
		for(int i = 0 ;i < Gamers.length; i++){
			Gamers[i].setUColor(-2);
		}
		for(int i = 0 ;i < Viewers.length; i++){
			Viewers[i].setUColor(-2);
		}
	}

	public void doReadyGame(SocketChannel client)					//玩家准备游戏
	{
		String userId = "";
		String userName = "";
		String userPortrait = "";
		int colorJudge = -1;										//0代表白子,1代表黑子
		boolean alreadExist = false ;
		int viwerId = getViewerNum(client);

		userId = Viewers[viwerId].getId();
		userName = Viewers[viwerId].getName();
		userPortrait = Viewers[viwerId].getPortrait();

		for(int i = 0 ;i < Gamers.length; i++){
			if(Gamers[i].getId().equals(userId))					//用户已经在游戏用户列表中，无需再次注册
			{
				alreadExist = true ;
				Viewers[viwerId].setUColor(i);
				colorJudge = i;
				break;
			}
		}
		if(!alreadExist) {											//新用户准备游戏	
			//for(int i = 0;i < Gamers.length; i++)					//将新用户信息注册到游戏玩家中,先开始为白子
			for(int i = Gamers.length - 1;i >= 0; i--)				//将新用户信息注册到游戏玩家中,先开始为黑子
			{
				if(Gamers[i].getId().equals("")){
					Gamers[i] = Viewers[viwerId];
					//System.out.println("viwerId:"+viwerId);
					Viewers[viwerId].setUColor(i);
					colorJudge = i;									//用户所持棋子颜色
					break;											//如果游戏已经满两人，新用户将无法注册到游戏玩家中，colorJudge返回-1
				}
			}
		}
		
		String result = "mycolor;" + colorJudge + ";";				//colorJudge  0代表白子,1代表黑子，-1代表旁观
		c.sendInforBack(client,result);								//回送信息
		if(colorJudge > -1)	isStartSum++;							//开始玩家增1
		if(isStartSum == 1){
			String message = "readyGame;" + userName ;
			broadcast(message);
		}
		if(isStartSum > 1){											//开始玩家为2个，游戏开始
			for (int i = 0; i < 16; i++) { 
				for (int j = 0; j < 16; j++)
				{	bodyArray[i][j] = -1;	} 
			} 
			isStart = true;
			winner = -1;
			if(initOppoPictOkSum < 2){								//发给玩家对手的形象,未传过才传;反之,不重复传
				String imageName0 = "";
				SocketChannel client0 = null;
				String imageName1 = "";
				SocketChannel client1 = null;
				for(int i = 0;i < Gamers.length;i++)		{
					if(!Gamers[i].getId().equals("")){
						if(i == 0){
							imageName0 =c.imagepath + Gamers[0].getId()+".PNG";
							client0 = Gamers[1].getUserChannel();
						}
						if(i == 1){
							imageName1 =c.imagepath + Gamers[1].getId()+".PNG";
							client1 = Gamers[0].getUserChannel();
						}
					}
				}
				int n = 0;
				int n1 = 0;
				try{
					c.sendInforBack(client0,"DLPImg9");				//downLoadPlayersImage

					File file =new File(imageName0);
					System.out.println(imageName0 + " 文件长度:" + file.length());
					if(file.length() > c.MaxImageLength){
						System.out.println("文件长度过长");//
						c.sendInforBack(client0,"ImgIllegal9");
						return;
					}
					FileInputStream  fr = new FileInputStream (file);
					byte[] b = new byte[1024];
					ByteBuffer sendbuffer; 
					while ((n = fr.read(b)) > 0) {	
						sendbuffer = ByteBuffer.wrap(b,0,n);  
						client0.write(sendbuffer);
						sendbuffer.flip();
						Thread.sleep(3);	//Thread.sleep(3);
					}
					fr.close();
					c.sendInforBack(client0,"LPImgD");				//loadPlayersImageDone
				}
				catch(Exception e){
					System.out.println(e);
					System.out.println("数据发送失败");
					c.sendInforBack(client0,"ImgIllegal9");
				}
				try{
					c.sendInforBack(client1,"DLPImg9");				//downLoadPlayersImage

					File file =new File(imageName1);
					System.out.println(imageName1 + " 文件长度:" + file.length());
					if(file.length() > c.MaxImageLength){
						System.out.println("文件长度过长");//
						c.sendInforBack(client1,"ImgIllegal9");
						return;
					}
					FileInputStream  fr = new FileInputStream (file);
					byte[] b = new byte[1024];
					ByteBuffer sendbuffer; 
					while ((n = fr.read(b)) > 0) {	
						sendbuffer = ByteBuffer.wrap(b,0,n);  
						client1.write(sendbuffer);
						sendbuffer.flip();
						Thread.sleep(3);	//Thread.sleep(3);
					}
					fr.close();
					c.sendInforBack(client1,"LPImgD");				//loadPlayersImageDone
				}
				catch(Exception e){
					System.out.println(e);
					System.out.println("数据发送失败");
					c.sendInforBack(client1,"ImgIllegal9");
				}
			}
			color = turn_around;
			String message = "start;" + color + ";" + c.STATEND + "start;" + color + ";";	//防数据截断特殊处理
			turn_around = (turn_around + 1) % 2;
			broadcast(message);
			refreshGamersInfor();
			refreshViewersInfor();
		}
	}


	public int getViewerNum(SocketChannel client)					//获取旁观者编号
	{
		for(int i = 0;i < Viewers.length; i++)		{
			if(!Viewers[i].getId().equals("")){
				if(Viewers[i].getUserChannel().equals(client)){
					return i;
				}
			}
		}
		return -1;
	}

	public int getGamerNum(SocketChannel client)					//获取玩家编号
	{
		for(int i = 0;i < Gamers.length; i++)		{
			if(!Gamers[i].getId().equals("")){
				if(Gamers[i].getUserChannel().equals(client)){
					return i;
				}
			}
		}
		return -1;
	}
	
	public int getUserNum(SocketChannel client)						//获取用户编号(无用方法)
	{
		for(int i = 0;i < Users.length; i++)		{
			if(!Users[i].getId().equals("")){
				if(Users[i].getUserChannel().equals(client)){
					return i;
				}
			}
		}
		return -1;
	}

	public void broadcast(String infor)								//向所有在线用户广播信息
	{
		System.out.println("桌子广播:");
		for(int i = 0;i < Viewers.length; i++)						//遍历服务器所有SocketChannel，将信息发给可用SocketChannel
		{
			if(!Viewers[i].getId().equals("")){
				//System.out.println(Viewers[i].getId());
				if(Viewers[i].getUserChannel().isConnected()){
					c.sendInforBack(Viewers[i].getUserChannel(),infor);
				}
				else{												//用户与服务器端口连接，清空用户信息
					Viewers[i] = new UAS();
				}
			}
		}
	}

	public void doChatting(String infor , SocketChannel client)		//向其他在线用户 广播信息
	{
		System.out.println("桌子聊天:");
		String userName = "";
		String userId = "";
		for(int i = 0;i < Viewers.length; i++)						//查找用户，获得用户名
		{
			if(!Viewers[i].getId().equals("")){
				if(Viewers[i].getUserChannel().isConnected()){		//连接状态
					if(Viewers[i].getUserChannel().equals(client)){
						userName = Viewers[i].getName();
						userId = Viewers[i].getId();
					}
				}
			}
		}
		String userMessage = "userMessage;"+ userId + ";" + userName + ";" +infor;
		for(int i = 0;i < Viewers.length; i++)						//遍历服务器所有SocketChannel，将信息发给可用SocketChannel
		{
			if(!Viewers[i].getId().equals("")){
				//System.out.println(Viewers[i].getId());
				//System.out.println(client);
				if(Viewers[i].getUserChannel().isConnected()){		//向全部用户发送信息，包括自己
					//if(Viewers[i].getUserChannel().equals(client))
					//{	continue;}
					c.sendInforBack(Viewers[i].getUserChannel(),userMessage);
				}
				else{												//用户与服务器端口连接，置空用户信息
					Viewers[i] = new UAS();
				}
			}
		}
	}

	public void rollbackForward(String rstep ,SocketChannel client)	//向另一玩家转发”悔棋“请求
	{
		int userC = -1;												//另一玩家的ID
		int i = getGamerNum(client);

		if(i == 0)	userC = 1;
		if(i == 1)	userC = 0;

		if(userC > -1){
			String result = "rollbackForward;" + rstep + ";";	
			c.sendInforBack(Gamers[userC].getUserChannel(),result);
		}
	}
	public void doRollback(int isAgree,int rstep,SocketChannel client)	//处理用户对"悔棋"请求的应答
	{
		int rbcolor = -3;
		int userC = -1;
		int i = getGamerNum(client);

		if(i == 0)	userC = 1;
		if(i == 1)	userC = 0;
		rbcolor = Gamers[userC].getUColor();
		if(isAgree == 1)											//用户同意悔棋
		{
			System.out.println("当前步数" + step + " 悔棋方颜色" + rbcolor + " 当前颜色" + color);

			int St = 0;												//回退步数
			if(color != rbcolor)									//另一玩家没落子，退一步
			{
				if(step <= 0)	return;
				St = 1;
				int x = stepRecords[step - 1].getX();
				int y = stepRecords[step - 1].getY();
				bodyArray[x][y]= -1;								//回退棋盘
				stepRecords[step - 1].setStepRecord(-1,-1,-1);		//回退记录
				step = step - 1;									//回退步数
			}
			else if(color == rbcolor)								//另一玩家已经落子，退两步
			{
				if(step <= 1)	return;
				St = 2;
				int x = stepRecords[step - 1].getX();
				int y = stepRecords[step - 1].getY();
				bodyArray[x][y]= -1;
				stepRecords[step - 1].setStepRecord(-1,-1,-1);
				x = stepRecords[step - 2].getX();
				y = stepRecords[step - 2].getY();
				bodyArray[x][y]= -1;								//回退棋盘
				stepRecords[step - 2].setStepRecord(-1,-1,-1);		//回退记录
				step = step - 2;									//回退步数
			}
			color = rbcolor;										//让发起悔棋请求的用户，拥有落子权
			String message = "rollbackReply;" + "yes;" + rstep + ";" + St + ";";
			broadcast(message);
		}
		else if(isAgree == 0){
			String message = "rollbackReply;no;" + rbcolor + ";0;";
			broadcast(message);
		}
	}

	public void admitLose(String color)
	{
		System.out.println("玩家认输！");
		int loserColor = Integer.parseInt(color);
		int winnerColor = (loserColor + 1) % 2;
		doWin(winnerColor, -1, -1);
	}

	public boolean checkSetDown(String []message,SocketChannel client)
	{
		if(isStart == false)	return false;
		final int x = Integer.parseInt(message[2]);					//x,y代表落子位置的横、纵坐标
		final int y = Integer.parseInt(message[3]);
		final int userColor = Integer.parseInt(message[4]);
		boolean fback = false;
		if(isStart){
			fback = setDown(x,y,userColor);
		}
		if(fback){
			System.out.println("落子成功");
			/*
			for (int i = 1; i < 16; i++) { 
				for (int j = 1; j < 16; j++){	
					if(bodyArray[i][j]!=-1){
						System.out.print(bodyArray[i][j]+" ");
					}
					else
						System.out.print("  ");
				} 
				System.out.println();
			} */
			String located = "located;" + x + ";"+ y + ";" + userColor + ";";
			try
			{	broadcast(located);	}								//广播
			catch (Exception e)
			{	System.out.println(e);	}

			if (gameWin1(x,y) >= 0){								// 判断输赢
				doWin(userColor, 1 ,gameWin1(x,y));
				return true; 
			}
			if (gameWin2(x,y) >= 0){								// 判断输赢
				doWin(userColor, 2 ,gameWin2(x,y));
				return true;
			}
			if (gameWin3(x,y) >= 0){								// 判断输赢
				doWin(userColor, 3 ,gameWin3(x,y));
				return true;
			}
			if (gameWin4(x,y) >= 0){								// 判断输赢
				doWin(userColor, 4 ,gameWin4(x,y));
				return true;
			}
			if(step == 225){										//步数达到上限,游戏结束
				System.out.println("步数达到上限,游戏结束");
				doDraw(null , 1);
				return true;
			}
		}
		else{
			System.out.println("拒绝落子");
		}
		return false;
	}

	public void doWin(int color, int direction,int plusSum)
	{
		winner = color;
		int loser = -1;
		if(winner == 0) loser = 1;
		if(winner == 1) loser = 0;
		System.out.println(startColor(winner) + "赢了!");

		int newWinScore = Gamers[winner].getScore() + c.ADD;
		int newLoseScore = Gamers[loser].getScore() + c.MINUS;

		Gamers[winner].setScore(newWinScore);
		Gamers[loser].setScore(newLoseScore);

		String winneruid = Gamers[winner].getId();
		String loseruid = Gamers[loser].getId();

		if(c.isUseDatabase){											//使用数据库
			sqlConn.tryConn();
			String sql = "update login l set l.scores = l.scores + " + c.ADD + " where userid=" + winneruid;
			boolean addScore = sqlConn.updateSql(sql);
			
			
			String sql2 = "update login l set l.scores = l.scores " + c.MINUS + " where userid=" + loseruid;
			boolean minusScore = sqlConn.updateSql(sql2);
			sqlConn.closeConnection();
		}
		else{
			writeScore(winneruid,c.ADD);
			writeScore(loseruid,c.MINUS);
		}

		server.refreshGameHallPlayers();
		String gameEnd = "";
		if(step > 1 && direction >= 0){
			gameEnd = "gameEnd;" + color + ";" + stepRecords[step - 1].getX()+";"+stepRecords[step - 1].getY()+";"+ direction +";" + plusSum+";";
		}
		else{
			gameEnd = "gameEnd;" + color + ";-1;-1;-1;-1;";
		}
		broadcast(gameEnd);

		isStart = false;											//游戏结束
		isStartSum = 0;												//游戏结束，开始人数置0
		clearRecords();
		step = 0;
		clearColor();
	}
	
	public void drawRequest(SocketChannel client)					//向另一玩家转发”和棋“请求
	{
		int userC = -1;												//另一玩家的ID
		int i = getGamerNum(client);

		if(i == 0)	userC = 1;
		if(i == 1)	userC = 0;

		if(userC > -1){
			String result = "drawRequest;";	
			c.sendInforBack(Gamers[userC].getUserChannel(),result);
		}
	}

	public boolean doDraw(SocketChannel client , int isAgree)
	{
		if(isAgree == 1)											//用户同意和棋
		{
			isStart = false;										//游戏结束
			isStartSum = 0;											//游戏结束，开始人数置0
			clearRecords();
			step = 0;
			//for(int i=0;i<Gamers.length;i++)						//若开启，则旁观玩家可抢占游戏者座位(已废14.9.27)
			//{	Gamers[i].clear();	}
			server.refreshGameHallPlayers();
			refreshGamersInfor();
			refreshViewersInfor();
			String gameEnd = "gameDraw;";
			broadcast(gameEnd);
			clearColor();
			return true;
		}
		else{
			int userC = -1;											//另一玩家的ID
			int i = getGamerNum(client);

			if(i == 0)	userC = 1;
			if(i == 1)	userC = 0;
			String result = "noDraw;";	
			c.sendInforBack(Gamers[userC].getUserChannel(),result);
		}
		return false;
	}

	public void refreshViewersInfor()
	{
		broadcast(createViewersInfor());
	}

	public void refreshViewersInforPartly(SocketChannel client)
	{
		c.sendInforBack(client,createViewersInfor());
	}

	public String createViewersInfor(){							//生成观察者信息 userId,userName,color
		//System.out.println("生成观察者信息");
		String uId = "";
		String uName = "";
		String infor = "refreshViewersInfor;";
		int ucolor = -1;
		String userPortrait = "";
		int score = -1;
		for(int i = 0;i < Viewers.length; i++){
			if(!Viewers[i].getId().equals("")){
				uId = Viewers[i].getId();
				uName = Viewers[i].getName();
				ucolor = Viewers[i].getUColor();
				score = Viewers[i].getScore();
				userPortrait = Viewers[i].getPortrait();
				//System.out.println(uId+";"+uName+""+score+"#");
				infor += uId + "↓" + uName + "↓" + ucolor + "↓" + score + "↓" + userPortrait + "↑";
			}
		}
		return infor;
	}
	

	public void refreshGamersInfor()								//广播玩家信息
	{
		broadcast(createGamersInfor());
	} 

	public void refreshGamersInforPartly(SocketChannel client)		//单独回送玩家信息
	{
		c.sendInforBack(client,createGamersInfor());
	} 

	public String createGamersInfor(){								//生成玩家信息 userId,userName,color
		//System.out.println("生成玩家信息");
		String userId = "";
		String userName = "";
		String infor = "refreshGamersInfor;";
		int color = -1;
		String userPortrait = "";
		int score = -1;
		for(int i = 0;i < Gamers.length; i++){
			if(!Gamers[i].getId().equals("")){
				userId = Gamers[i].getId();
				userName = Gamers[i].getName();
				color = Gamers[i].getUColor();
				score = Gamers[i].getScore();
				userPortrait = Gamers[i].getPortrait();
			}
			else continue;
			//int score = getScoresByUserId(userId);
			//System.out.println(userId+";"+score);
			infor += userId + "," + userName + "," + color + "," + score + "," + userPortrait + ";";
		}
		return infor;
	}

	public int getScoresBySC(SocketChannel client)					//通过SocketChannel获取用户分数
	{
		String userId = "";											//获取SocketChannel对应用户的ID
		int i = getViewerNum(client);

		userId = Viewers[i].getId();

		int userScores = 0;

		if(c.isUseDatabase){											//使用数据库
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
			}
		}
		else {														//不使用数据库
			userScores = Viewers[i].getScore();
		}


		return userScores;
	}

	public void clearRecords()
	{
		for(int i = 0; i < stepRecords.length ; i++)
			{	stepRecords[i].setStepRecord(-1,-1,-1);	}
	}

	public int getScoresByUserId(String Id)							//通过Id获取用户分数
	{
		int userId = Integer.parseInt(Id);
		int userScores = 0;


		if(c.isUseDatabase){											//使用数据库
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
			}
		}
		return userScores;
	}
	
	
	public void gameInit()											// 游戏开始初始化
	{
		for (int i = 0; i < 16; i++) { 
			for (int j = 0; j < 16; j++)
			{	bodyArray[i][j] = -1;	} 
		} 
		for(int i = 0; i<stepRecords.length ; i++)
			{	stepRecords[i].setStepRecord(-1,-1,-1);	}
	}

	public boolean setDown(int x, int y,int usercolor)				// 落子.x代表横坐标,y代表纵坐标,usercolor玩家所持棋子颜色
	{
		if (!isStart)												// 判断游戏未开始
		{	return false; } 
		if (bodyArray[x][y] != -1)									//已经放过棋子
		{	return false; } 
		if(color != usercolor)
		{	return false; } 
		if(x < 1 || x > 15 || y < 1 || y > 15){
			return false;
		}
		bodyArray[x][y] = color;									//矩阵赋值
		stepRecords[step].setStepRecord(x,y,color);					//记录落子信息以备回退
		step++;														//步数增1
		if (color == 1&&color == usercolor)							// 判断黑子还是白子，变色，让另一玩家执棋 
		{	color = 0;	}
		else if(color == 0&&color == usercolor)
		{	color = 1;	}
		return true;
	}

	public void viewerExit(SocketChannel client)					//处理用户退出客户端
	{
		//System.out.println("执行用户退出");

		for(int i = 0;i < Viewers.length; i++)		{
			if(!Viewers[i].getId().equals("")){
				if(Viewers[i].getUserChannel().equals(client)){
					System.out.println("观察者 " + Viewers[i].getName() + " 离开本桌");
					if(client.isConnected())						//当前用户只退出游戏，并未退出房间，让其可打开一个新的游戏窗口
					{
						String message = "reSetIsOpenWin;";
						c.sendInforBack(client,message);
					}
					Viewers[i] = new UAS();
					viewerSum--;
					break;
				}
				else												//非退出游戏用户，告之其有玩家退出
				{
					if(client.isConnected())						//当前用户只退出游戏，并未退出房间，让其可打开一个新的游戏窗口
					{
						String message = "reSetIsOpenWin;";
						c.sendInforBack(client,message);
					}
					//String message = "viewerExited;";	//暂时是无用信息
					//c.sendInforBack(Viewers[i].getUserChannel(),message);
				}
			}
		}	
		for(int i = 0;i < Gamers.length; i++)
		{
			if(!Gamers[i].getId().equals("")){
				if(Gamers[i].getUserChannel().equals(client))		//游戏玩家退出游戏
				{
					System.out.println("玩家 " + Gamers[i].getName() + " 离开本桌");
					String gamerExited = "gamerExited;";
					try
					{	broadcast(gamerExited);  }					//广播
					catch (Exception e)	{}
					if(isStart)										//游戏未结束，玩家逃跑
					{

						int loser = i;
						int winner = -1;
						if(loser == 0) winner = 1;
						if(loser == 1) winner = 0;
						System.out.println(startColor(winner) + "赢了!");

						int newWinScore = Gamers[winner].getScore() + c.ADD;
						int newLoseScore = Gamers[loser].getScore() + c.ESCAPEMINUS;

						Gamers[winner].setScore(newWinScore);
						Gamers[loser].setScore(newLoseScore);

						String escapeId = Gamers[i].getId();
						System.out.println("玩家"+Gamers[i].getName() + "逃跑扣4分");
						String sql = "";

						if(c.isUseDatabase){							//使用数据库
							sql = "update login l set l.scores = l.scores - 4 where userid=" + escapeId;
							sqlConn.tryConn();
							boolean minusScore = sqlConn.updateSql(sql);	//逃跑玩家扣4分
						}
						else{
							writeScore(escapeId,c.ESCAPEMINUS);
						}


						int adder = -1;
						if(i == 0) adder =1;
						if(i == 1) adder =0;
						String adderIp = Gamers[adder].getId();
						System.out.println("玩家"+Gamers[adder].getName()+"得2分");


						if(c.isUseDatabase){							//使用数据库
							sql="update login l set l.scores = l.scores + 2 where userid="+adderIp;
							boolean addScore = sqlConn.updateSql(sql);		//对家加2分
							sqlConn.closeConnection();
						}
						else{
							writeScore(adderIp,c.ADD);
						}

						isStart = false;
						clearColor();
					}
					Gamers[i] = new UAS();
					isStartSum = 0;
					initOppoPictOkSum = 0;
					break;
				}
			}
		}
		refreshGamersInfor();
		refreshViewersInfor();
		server.refreshGameHallPlayers();
	}

	public void writeScore(String id , int scorechange){
		try{

				File txt = new File("src/users.txt");				//向配置文件写入头像信息,新信息顶置
				FileReader filein = new FileReader("src/users.txt");
				BufferedReader br = new BufferedReader(filein);
				String temp = "";
				String record = "";
				while((temp = br.readLine()) != null) {
					//System.out.println(temp);
					String dlls[] = temp.split(";");
					for(int i = 0 ; i < dlls.length ; i++){
						dlls[i] = dlls[i].trim();
					}
					if(!dlls[0].equals(id)){
						record = record + temp + "\r\n";
					}
					else{
						String Score = dlls[3];
						String userName = dlls[1];
						String password = dlls[2];
						String userId = dlls[0];
						int newScore = Integer.parseInt(Score) + scorechange;
						String newTemp = userId + ";" + userName + ";" + password + ";" + newScore +";";
						record = record + newTemp + "\r\n";
					}
				}
				byte []contents = (record).getBytes();
				try{
					FileOutputStream out = new FileOutputStream(txt,false);		//重写
					out.write(contents);
					out.close();
				}
				catch (IOException ee)
				{	System.out.println(ee);}
		}
		catch(Exception e3)
		{	System.out.println(e3);}
	}

	public int gameWin1(int x, int y)								// 判断输赢 竖
	{ 
		int t = 1;
		int plusSum = 0;
		
		for (int i = 1; i < 5; i++) { 
			if(x + i <= 15){
				if (bodyArray[x + i][y] == bodyArray[x][y])
				{	t += 1; plusSum ++;}
				else {	break;}
			}
		} 
		for (int i = 1; i < 5; i++){
			if (x - i >= 1){
				if (bodyArray[x - i][y] == bodyArray[x][y])
				{	t += 1;}
				else {	break;} 
			}
		}
		
		if (t > 4) {	return plusSum;}							//五子连珠
		else {	return -1;} 
	}

	public int gameWin2(int x, int y)								// 判断输赢 横
	{ 
		int t = 1; 
		int plusSum = 0;
		for (int i = 1; i < 5; i++){ 
			if(y + i <= 15){
				if (bodyArray[x][y + i] == bodyArray[x][y])
				{	t += 1; plusSum ++;}
				else {	break;}
			}
		}
		for (int i = 1; i < 5; i++){ 
			if(y - i >= 0){
				if (bodyArray[x][y - i] == bodyArray[x][y])
				{	t += 1;}
				else {	break;}
			}
		} 
		if (t > 4) {	return plusSum;}							//五子连珠
		else {	return -1;}
	}

	public int gameWin3(int x, int y)								// 判断输赢 左斜
	{
		int t = 1;
		int plusSum = 0;
		for (int i = 1; i < 5; i++){
			if(x + i <= 15&&y - i >= 1){
				if (bodyArray[x + i][y - i] == bodyArray[x][y])
				{	t += 1; plusSum ++;}
				else {	break;}
			}
			
		}
		for (int i = 1; i < 5; i++){ 
			if(y + i <= 15&&x - i >= 1){
				if (bodyArray[x - i][y + i] == bodyArray[x][y]) 
				{	t += 1;}
				else {	break;}
			}
		}
		if (t > 4) {	return plusSum;}							//五子连珠
		else {	return -1;}
	}

	public int gameWin4(int x, int y)								// 判断输赢 左斜
	{
		int t = 1;
		int plusSum = 0;
		for (int i = 1; i < 5; i++){ 
			if(x + i <= 15&&y + i <= 15){
				if (bodyArray[x + i][y + i] == bodyArray[x][y])
				{	t += 1;  plusSum ++;}
				else {	break; }
			}
		}
		for (int i = 1; i < 5; i++){
			if(x - i >= 1&&y - i >= 1)
			{
				if (bodyArray[x - i][y - i] == bodyArray[x][y])
				{	t += 1;} 
				else {	break;}
			}
		}
		if (t > 4) {	return plusSum;}							//五子连珠
		else {	return -1;}
	}
	public String startColor(int x)
	{
		if (x == 0) {	return "白子";} 
		else {	return "黑子";} 
	}
}

class StepRecord
{
	private int x = -1; 
	private int y = -1;
	private int color = -1;
	public void setStepRecord(int rx,int ry,int rcolor)
	{
		x = rx;
		y = ry;
		color = rcolor;
	}
	public int getX()
	{	return x;}
	public int getY()
	{	return y;}
	public int getColor()
	{	return color;}
}

class Seat
{
	String userId = "";
	String userName = "";
	String pictName = "";
	public void set(String i,String n,String p){
		userId = i;
		userName = n;
		pictName = p;
	}
	public void clear(){
		userId = "";
		userName = "";
		pictName = "";
	}
}