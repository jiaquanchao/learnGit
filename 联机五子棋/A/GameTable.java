/*
 * ����:	 �����
 * QQ:	 	 714670841
 * ����:	 714670841@qq.com
 * ��������:EditPlus
 * Copyright 2014 ����� 
 * ����Ʒֻ���ڸ���ѧϰ���о������ͣ�ת����ע��������
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
	private UAS Gamers[];											//��ǰ������Ϸ���û�  Gamers[0]Ϊ�ְ���һ�� �� Gamers[1]Ϊ�ֺ���һ��
	private UAS Viewers[];											//�������µ��û�
	private UAS Users[];											//�������(Ŀǰ����)	
	private int viewerSum = 0;										//�Թ��߸���(�������)
	private int gamerSum = 0;										//��Ҹ���
	private int winner = -1;										//Ӯ����ִ����ɫ
	private int color = 1;											// ���ӵ���ɫ��ʶ(����) 0:���� 1:���� 
	private int turn_around = 1;									// ��������
	private int step = 0;											//��ǰ����
	private int isStartSum = 0;										//׼����ʼ��Ϸ�������ж��Ƿ�ʼ��Ϸ
	private boolean isStart = false;								// ��Ϸ��ʼ��־
	private StepRecord stepRecords[] = new StepRecord[225];			//��¼�������������Ϣ
	private int bodyArray[][] = new int[16][16];					// ������������״̬ 0 ���� 1 ���� 2 ����
	private static ByteBuffer sBuffer = ByteBuffer.allocate(1024);	//�������ݻ�����
	public Seat seat[] = new Seat[2];								//���ӵ�����������λ
	private int initOppoPictOkSum = 0;								//��ʼ���Լ�����ɹ��������Ŀ

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
		c = cc;														//��ȡ������
		Gamers = new UAS[2];
		for(int i = 0;i < Gamers.length; i++)						//������Ϸ�������鲢ʵ����
		{	Gamers[i] = new UAS();	}
		Viewers = new UAS[5+2];
		for(int i = 0;i < Viewers.length; i++)						//�����۲��������鲢ʵ����
		{	Viewers[i] = new UAS();	}
		for(int i = 0; i < stepRecords.length ; i++)
		{	stepRecords[i] = new StepRecord();	}					//�������鲢ʵ�������������¼��Ϸ���̣����ڻ���
		for(int i = 0 ; i <seat.length; i++){
			seat[i] = new Seat();
		}
		gameInit();
	}

	public void setSqlConn(SqlConn sqlconn){
		sqlConn = sqlconn;
	}

	public void clearColor(){										//��Ϸ����������ִ����ɫ,�Դ����·��䡣������ɫ����,������ʾ ׼����Ϸ
		for(int i = 0 ;i < Gamers.length; i++){
			Gamers[i].setUColor(-2);
		}
		for(int i = 0 ;i < Viewers.length; i++){
			Viewers[i].setUColor(-2);
		}
	}

	public void doReadyGame(SocketChannel client)					//���׼����Ϸ
	{
		String userId = "";
		String userName = "";
		String userPortrait = "";
		int colorJudge = -1;										//0�������,1�������
		boolean alreadExist = false ;
		int viwerId = getViewerNum(client);

		userId = Viewers[viwerId].getId();
		userName = Viewers[viwerId].getName();
		userPortrait = Viewers[viwerId].getPortrait();

		for(int i = 0 ;i < Gamers.length; i++){
			if(Gamers[i].getId().equals(userId))					//�û��Ѿ�����Ϸ�û��б��У������ٴ�ע��
			{
				alreadExist = true ;
				Viewers[viwerId].setUColor(i);
				colorJudge = i;
				break;
			}
		}
		if(!alreadExist) {											//���û�׼����Ϸ	
			//for(int i = 0;i < Gamers.length; i++)					//�����û���Ϣע�ᵽ��Ϸ�����,�ȿ�ʼΪ����
			for(int i = Gamers.length - 1;i >= 0; i--)				//�����û���Ϣע�ᵽ��Ϸ�����,�ȿ�ʼΪ����
			{
				if(Gamers[i].getId().equals("")){
					Gamers[i] = Viewers[viwerId];
					//System.out.println("viwerId:"+viwerId);
					Viewers[viwerId].setUColor(i);
					colorJudge = i;									//�û�����������ɫ
					break;											//�����Ϸ�Ѿ������ˣ����û����޷�ע�ᵽ��Ϸ����У�colorJudge����-1
				}
			}
		}
		
		String result = "mycolor;" + colorJudge + ";";				//colorJudge  0�������,1������ӣ�-1�����Թ�
		c.sendInforBack(client,result);								//������Ϣ
		if(colorJudge > -1)	isStartSum++;							//��ʼ�����1
		if(isStartSum == 1){
			String message = "readyGame;" + userName ;
			broadcast(message);
		}
		if(isStartSum > 1){											//��ʼ���Ϊ2������Ϸ��ʼ
			for (int i = 0; i < 16; i++) { 
				for (int j = 0; j < 16; j++)
				{	bodyArray[i][j] = -1;	} 
			} 
			isStart = true;
			winner = -1;
			if(initOppoPictOkSum < 2){								//������Ҷ��ֵ�����,δ�����Ŵ�;��֮,���ظ���
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
					System.out.println(imageName0 + " �ļ�����:" + file.length());
					if(file.length() > c.MaxImageLength){
						System.out.println("�ļ����ȹ���");//
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
					System.out.println("���ݷ���ʧ��");
					c.sendInforBack(client0,"ImgIllegal9");
				}
				try{
					c.sendInforBack(client1,"DLPImg9");				//downLoadPlayersImage

					File file =new File(imageName1);
					System.out.println(imageName1 + " �ļ�����:" + file.length());
					if(file.length() > c.MaxImageLength){
						System.out.println("�ļ����ȹ���");//
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
					System.out.println("���ݷ���ʧ��");
					c.sendInforBack(client1,"ImgIllegal9");
				}
			}
			color = turn_around;
			String message = "start;" + color + ";" + c.STATEND + "start;" + color + ";";	//�����ݽض����⴦��
			turn_around = (turn_around + 1) % 2;
			broadcast(message);
			refreshGamersInfor();
			refreshViewersInfor();
		}
	}


	public int getViewerNum(SocketChannel client)					//��ȡ�Թ��߱��
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

	public int getGamerNum(SocketChannel client)					//��ȡ��ұ��
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
	
	public int getUserNum(SocketChannel client)						//��ȡ�û����(���÷���)
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

	public void broadcast(String infor)								//�����������û��㲥��Ϣ
	{
		System.out.println("���ӹ㲥:");
		for(int i = 0;i < Viewers.length; i++)						//��������������SocketChannel������Ϣ��������SocketChannel
		{
			if(!Viewers[i].getId().equals("")){
				//System.out.println(Viewers[i].getId());
				if(Viewers[i].getUserChannel().isConnected()){
					c.sendInforBack(Viewers[i].getUserChannel(),infor);
				}
				else{												//�û���������˿����ӣ�����û���Ϣ
					Viewers[i] = new UAS();
				}
			}
		}
	}

	public void doChatting(String infor , SocketChannel client)		//�����������û� �㲥��Ϣ
	{
		System.out.println("��������:");
		String userName = "";
		String userId = "";
		for(int i = 0;i < Viewers.length; i++)						//�����û�������û���
		{
			if(!Viewers[i].getId().equals("")){
				if(Viewers[i].getUserChannel().isConnected()){		//����״̬
					if(Viewers[i].getUserChannel().equals(client)){
						userName = Viewers[i].getName();
						userId = Viewers[i].getId();
					}
				}
			}
		}
		String userMessage = "userMessage;"+ userId + ";" + userName + ";" +infor;
		for(int i = 0;i < Viewers.length; i++)						//��������������SocketChannel������Ϣ��������SocketChannel
		{
			if(!Viewers[i].getId().equals("")){
				//System.out.println(Viewers[i].getId());
				//System.out.println(client);
				if(Viewers[i].getUserChannel().isConnected()){		//��ȫ���û�������Ϣ�������Լ�
					//if(Viewers[i].getUserChannel().equals(client))
					//{	continue;}
					c.sendInforBack(Viewers[i].getUserChannel(),userMessage);
				}
				else{												//�û���������˿����ӣ��ÿ��û���Ϣ
					Viewers[i] = new UAS();
				}
			}
		}
	}

	public void rollbackForward(String rstep ,SocketChannel client)	//����һ���ת�������塰����
	{
		int userC = -1;												//��һ��ҵ�ID
		int i = getGamerNum(client);

		if(i == 0)	userC = 1;
		if(i == 1)	userC = 0;

		if(userC > -1){
			String result = "rollbackForward;" + rstep + ";";	
			c.sendInforBack(Gamers[userC].getUserChannel(),result);
		}
	}
	public void doRollback(int isAgree,int rstep,SocketChannel client)	//�����û���"����"�����Ӧ��
	{
		int rbcolor = -3;
		int userC = -1;
		int i = getGamerNum(client);

		if(i == 0)	userC = 1;
		if(i == 1)	userC = 0;
		rbcolor = Gamers[userC].getUColor();
		if(isAgree == 1)											//�û�ͬ�����
		{
			System.out.println("��ǰ����" + step + " ���巽��ɫ" + rbcolor + " ��ǰ��ɫ" + color);

			int St = 0;												//���˲���
			if(color != rbcolor)									//��һ���û���ӣ���һ��
			{
				if(step <= 0)	return;
				St = 1;
				int x = stepRecords[step - 1].getX();
				int y = stepRecords[step - 1].getY();
				bodyArray[x][y]= -1;								//��������
				stepRecords[step - 1].setStepRecord(-1,-1,-1);		//���˼�¼
				step = step - 1;									//���˲���
			}
			else if(color == rbcolor)								//��һ����Ѿ����ӣ�������
			{
				if(step <= 1)	return;
				St = 2;
				int x = stepRecords[step - 1].getX();
				int y = stepRecords[step - 1].getY();
				bodyArray[x][y]= -1;
				stepRecords[step - 1].setStepRecord(-1,-1,-1);
				x = stepRecords[step - 2].getX();
				y = stepRecords[step - 2].getY();
				bodyArray[x][y]= -1;								//��������
				stepRecords[step - 2].setStepRecord(-1,-1,-1);		//���˼�¼
				step = step - 2;									//���˲���
			}
			color = rbcolor;										//�÷������������û���ӵ������Ȩ
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
		System.out.println("������䣡");
		int loserColor = Integer.parseInt(color);
		int winnerColor = (loserColor + 1) % 2;
		doWin(winnerColor, -1, -1);
	}

	public boolean checkSetDown(String []message,SocketChannel client)
	{
		if(isStart == false)	return false;
		final int x = Integer.parseInt(message[2]);					//x,y��������λ�õĺᡢ������
		final int y = Integer.parseInt(message[3]);
		final int userColor = Integer.parseInt(message[4]);
		boolean fback = false;
		if(isStart){
			fback = setDown(x,y,userColor);
		}
		if(fback){
			System.out.println("���ӳɹ�");
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
			{	broadcast(located);	}								//�㲥
			catch (Exception e)
			{	System.out.println(e);	}

			if (gameWin1(x,y) >= 0){								// �ж���Ӯ
				doWin(userColor, 1 ,gameWin1(x,y));
				return true; 
			}
			if (gameWin2(x,y) >= 0){								// �ж���Ӯ
				doWin(userColor, 2 ,gameWin2(x,y));
				return true;
			}
			if (gameWin3(x,y) >= 0){								// �ж���Ӯ
				doWin(userColor, 3 ,gameWin3(x,y));
				return true;
			}
			if (gameWin4(x,y) >= 0){								// �ж���Ӯ
				doWin(userColor, 4 ,gameWin4(x,y));
				return true;
			}
			if(step == 225){										//�����ﵽ����,��Ϸ����
				System.out.println("�����ﵽ����,��Ϸ����");
				doDraw(null , 1);
				return true;
			}
		}
		else{
			System.out.println("�ܾ�����");
		}
		return false;
	}

	public void doWin(int color, int direction,int plusSum)
	{
		winner = color;
		int loser = -1;
		if(winner == 0) loser = 1;
		if(winner == 1) loser = 0;
		System.out.println(startColor(winner) + "Ӯ��!");

		int newWinScore = Gamers[winner].getScore() + c.ADD;
		int newLoseScore = Gamers[loser].getScore() + c.MINUS;

		Gamers[winner].setScore(newWinScore);
		Gamers[loser].setScore(newLoseScore);

		String winneruid = Gamers[winner].getId();
		String loseruid = Gamers[loser].getId();

		if(c.isUseDatabase){											//ʹ�����ݿ�
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

		isStart = false;											//��Ϸ����
		isStartSum = 0;												//��Ϸ��������ʼ������0
		clearRecords();
		step = 0;
		clearColor();
	}
	
	public void drawRequest(SocketChannel client)					//����һ���ת�������塰����
	{
		int userC = -1;												//��һ��ҵ�ID
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
		if(isAgree == 1)											//�û�ͬ�����
		{
			isStart = false;										//��Ϸ����
			isStartSum = 0;											//��Ϸ��������ʼ������0
			clearRecords();
			step = 0;
			//for(int i=0;i<Gamers.length;i++)						//�����������Թ���ҿ���ռ��Ϸ����λ(�ѷ�14.9.27)
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
			int userC = -1;											//��һ��ҵ�ID
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

	public String createViewersInfor(){							//���ɹ۲�����Ϣ userId,userName,color
		//System.out.println("���ɹ۲�����Ϣ");
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
				infor += uId + "��" + uName + "��" + ucolor + "��" + score + "��" + userPortrait + "��";
			}
		}
		return infor;
	}
	

	public void refreshGamersInfor()								//�㲥�����Ϣ
	{
		broadcast(createGamersInfor());
	} 

	public void refreshGamersInforPartly(SocketChannel client)		//�������������Ϣ
	{
		c.sendInforBack(client,createGamersInfor());
	} 

	public String createGamersInfor(){								//���������Ϣ userId,userName,color
		//System.out.println("���������Ϣ");
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

	public int getScoresBySC(SocketChannel client)					//ͨ��SocketChannel��ȡ�û�����
	{
		String userId = "";											//��ȡSocketChannel��Ӧ�û���ID
		int i = getViewerNum(client);

		userId = Viewers[i].getId();

		int userScores = 0;

		if(c.isUseDatabase){											//ʹ�����ݿ�
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
		else {														//��ʹ�����ݿ�
			userScores = Viewers[i].getScore();
		}


		return userScores;
	}

	public void clearRecords()
	{
		for(int i = 0; i < stepRecords.length ; i++)
			{	stepRecords[i].setStepRecord(-1,-1,-1);	}
	}

	public int getScoresByUserId(String Id)							//ͨ��Id��ȡ�û�����
	{
		int userId = Integer.parseInt(Id);
		int userScores = 0;


		if(c.isUseDatabase){											//ʹ�����ݿ�
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
	
	
	public void gameInit()											// ��Ϸ��ʼ��ʼ��
	{
		for (int i = 0; i < 16; i++) { 
			for (int j = 0; j < 16; j++)
			{	bodyArray[i][j] = -1;	} 
		} 
		for(int i = 0; i<stepRecords.length ; i++)
			{	stepRecords[i].setStepRecord(-1,-1,-1);	}
	}

	public boolean setDown(int x, int y,int usercolor)				// ����.x���������,y����������,usercolor�������������ɫ
	{
		if (!isStart)												// �ж���Ϸδ��ʼ
		{	return false; } 
		if (bodyArray[x][y] != -1)									//�Ѿ��Ź�����
		{	return false; } 
		if(color != usercolor)
		{	return false; } 
		if(x < 1 || x > 15 || y < 1 || y > 15){
			return false;
		}
		bodyArray[x][y] = color;									//����ֵ
		stepRecords[step].setStepRecord(x,y,color);					//��¼������Ϣ�Ա�����
		step++;														//������1
		if (color == 1&&color == usercolor)							// �жϺ��ӻ��ǰ��ӣ���ɫ������һ���ִ�� 
		{	color = 0;	}
		else if(color == 0&&color == usercolor)
		{	color = 1;	}
		return true;
	}

	public void viewerExit(SocketChannel client)					//�����û��˳��ͻ���
	{
		//System.out.println("ִ���û��˳�");

		for(int i = 0;i < Viewers.length; i++)		{
			if(!Viewers[i].getId().equals("")){
				if(Viewers[i].getUserChannel().equals(client)){
					System.out.println("�۲��� " + Viewers[i].getName() + " �뿪����");
					if(client.isConnected())						//��ǰ�û�ֻ�˳���Ϸ����δ�˳����䣬����ɴ�һ���µ���Ϸ����
					{
						String message = "reSetIsOpenWin;";
						c.sendInforBack(client,message);
					}
					Viewers[i] = new UAS();
					viewerSum--;
					break;
				}
				else												//���˳���Ϸ�û�����֮��������˳�
				{
					if(client.isConnected())						//��ǰ�û�ֻ�˳���Ϸ����δ�˳����䣬����ɴ�һ���µ���Ϸ����
					{
						String message = "reSetIsOpenWin;";
						c.sendInforBack(client,message);
					}
					//String message = "viewerExited;";	//��ʱ��������Ϣ
					//c.sendInforBack(Viewers[i].getUserChannel(),message);
				}
			}
		}	
		for(int i = 0;i < Gamers.length; i++)
		{
			if(!Gamers[i].getId().equals("")){
				if(Gamers[i].getUserChannel().equals(client))		//��Ϸ����˳���Ϸ
				{
					System.out.println("��� " + Gamers[i].getName() + " �뿪����");
					String gamerExited = "gamerExited;";
					try
					{	broadcast(gamerExited);  }					//�㲥
					catch (Exception e)	{}
					if(isStart)										//��Ϸδ�������������
					{

						int loser = i;
						int winner = -1;
						if(loser == 0) winner = 1;
						if(loser == 1) winner = 0;
						System.out.println(startColor(winner) + "Ӯ��!");

						int newWinScore = Gamers[winner].getScore() + c.ADD;
						int newLoseScore = Gamers[loser].getScore() + c.ESCAPEMINUS;

						Gamers[winner].setScore(newWinScore);
						Gamers[loser].setScore(newLoseScore);

						String escapeId = Gamers[i].getId();
						System.out.println("���"+Gamers[i].getName() + "���ܿ�4��");
						String sql = "";

						if(c.isUseDatabase){							//ʹ�����ݿ�
							sql = "update login l set l.scores = l.scores - 4 where userid=" + escapeId;
							sqlConn.tryConn();
							boolean minusScore = sqlConn.updateSql(sql);	//������ҿ�4��
						}
						else{
							writeScore(escapeId,c.ESCAPEMINUS);
						}


						int adder = -1;
						if(i == 0) adder =1;
						if(i == 1) adder =0;
						String adderIp = Gamers[adder].getId();
						System.out.println("���"+Gamers[adder].getName()+"��2��");


						if(c.isUseDatabase){							//ʹ�����ݿ�
							sql="update login l set l.scores = l.scores + 2 where userid="+adderIp;
							boolean addScore = sqlConn.updateSql(sql);		//�ԼҼ�2��
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

				File txt = new File("src/users.txt");				//�������ļ�д��ͷ����Ϣ,����Ϣ����
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
					FileOutputStream out = new FileOutputStream(txt,false);		//��д
					out.write(contents);
					out.close();
				}
				catch (IOException ee)
				{	System.out.println(ee);}
		}
		catch(Exception e3)
		{	System.out.println(e3);}
	}

	public int gameWin1(int x, int y)								// �ж���Ӯ ��
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
		
		if (t > 4) {	return plusSum;}							//��������
		else {	return -1;} 
	}

	public int gameWin2(int x, int y)								// �ж���Ӯ ��
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
		if (t > 4) {	return plusSum;}							//��������
		else {	return -1;}
	}

	public int gameWin3(int x, int y)								// �ж���Ӯ ��б
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
		if (t > 4) {	return plusSum;}							//��������
		else {	return -1;}
	}

	public int gameWin4(int x, int y)								// �ж���Ӯ ��б
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
		if (t > 4) {	return plusSum;}							//��������
		else {	return -1;}
	}
	public String startColor(int x)
	{
		if (x == 0) {	return "����";} 
		else {	return "����";} 
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