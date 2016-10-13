/*
 * ����:	 �����
 * QQ:	 	 714670841
 * ����:	 714670841@qq.com
 * ��������:EditPlus
 * Copyright 2014 ����� 
 * ����Ʒֻ���ڸ���ѧϰ���о������ͣ�ת����ע��������
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

class Server extends JFrame	implements ActionListener, MouseListener, MouseMotionListener 	//��Ϸ������
{
	Constant c;
	private ImgTemp imgTemps[];										//��ʱ�����������
	private SqlConn sqlConn;										//�������ݿ���
	private GameTable Tables[];										//��Ϸ������
	private UAS Users[];											//���					
	private int userSum = 0;										//��ǰ����������ӵ��û�
	private Selector selector;										//����һ���¼�ѡ���������¼�׽���ͨ�����¼�
	private ServerSocketChannel ssc;								//����һ���첽������socket����
	private ServerSocket ss;										//���������socket����-����ָ���첽socket�ļ����˿ڵ���Ϣ
	private InetSocketAddress address;								//�����ż����˿ڵĶ���
	JTextArea showUsers = new JTextArea("");
	JScrollPane	showUsersScroll = new JScrollPane(showUsers);
	Image playerImage;
	JLabel viewersInfors[];
	String viewersInforsID[];

	Server(Constant cc)												//��ʼ��������
	{
		super("�û�");												//���ñ���
		c = cc;														//��ȡ������
		imgTemps = new ImgTemp[c.maxUsers];
		viewersInfors = new JLabel[c.maxUsers];
		viewersInforsID = new String[c.maxUsers];

		Tables = new GameTable[c.maxTables];
		for(int i = 0;i < Tables.length;i++){						//������Ϸ�������鲢ʵ����
			Tables[i] = new GameTable(c);
			Tables[i].setUsers(Users);
			Tables[i].setServer(this);
			if(c.isUseDatabase){									//ʹ�����ݿ�
				Tables[i].setSqlConn(sqlConn);
			}
		}

		Users = new UAS[c.maxUsers];								//�����û������鲢ʵ����
		for(int i = 0;i < Users.length;i++){
			Users[i] = new UAS();	
		}		

		for(int i = 0;i < imgTemps.length;i++){
			imgTemps[i] = new ImgTemp();
		}

		if(c.isShowUser){
			showUser();
		}

		if(c.isUseDatabase){											//ʹ�����ݿ�
			String connectString = "jdbc:oracle:thin:@"+ c.serverIp +":1521:orcl";
			String orclUsername = "scott";
			String orclPassword = "scott";
			sqlConn = new SqlConn();								//�������ݿ⽻����
			sqlConn.setSql(connectString,orclUsername,orclPassword);
		}

		try {
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);							//����socket��������Ϊ�첽
            ss = ssc.socket();
            address = new InetSocketAddress(c.serverPort);
            ss.bind(address);										//��������������˿ڰ�
            ssc.register(selector, SelectionKey.OP_ACCEPT);			//���첽�ķ�����socket����Ľ��ܿͻ��������¼�ע�ᵽselector������
			System.out.println("********* ���������� �����  ********");
            System.out.println("����˶˿�ע�����!");
		}
		catch (Exception e){	
			System.out.println("�������޷�����");
			if(c.debug){	e.printStackTrace();}
			System.exit(0);
		}
		try {
			int userNumber = -1;									//��ȡÿ��ѭ����Ӧ�û��ı��
            while(true)												//ͨ����ѭ���������¼�
			{
                int shijian = selector.select();					//��ѯ�¼����һ���¼���û�о�����
                if(shijian == 0)	{	continue;	}
                ByteBuffer echoBuffer = ByteBuffer.allocate(c.BUFFER_SIZE);			//����һ��byte���������洢�շ�������
                for (SelectionKey key : selector.selectedKeys())					//��ѭ���������в������¼�
				{
                    if(key.isAcceptable())							//����������¼�Ϊ���ܿͻ�������(���пͻ������ӷ�������ʱ�����)
					{
                        ServerSocketChannel server = (ServerSocketChannel)key.channel();	//����һ��������socketͨ��
                        SocketChannel client = server.accept();						//����ʱsocket����ʵ����Ϊ���յ��Ŀͻ��˵�socket
                        client.configureBlocking(false);							//���ͻ��˵�socket����Ϊ�첽
                        client.register(selector, SelectionKey.OP_READ);			//���ͻ��˵�socket�Ķ�ȡ�¼�ע�ᵽ�¼�ѡ������
						System.out.println("�������������");
                        //System.out.println("�������������:" + client);
                    }
                    else if(key.isReadable())						//����������¼�Ϊ��ȡ����(�������ӵĿͻ�����������������ݵ�ʱ�����)
					{
                        SocketChannel client = (SocketChannel)key.channel();		//�ͻ��������µ�SocketChannel
                        echoBuffer.clear();											//�Ƚ��ͻ��˵��������
						int readInt = 0;											//readIntΪ��ȡ�����ݵĳ���
                        try {
                            while ((readInt = client.read(echoBuffer)) > 0){
								//if(readInt == 0 )	{ continue;}					//���� == 0
								byte[] readByte = new byte[readInt];				//����һ����ʱbyte����,���볤����Ϊ��ȡ�����ݵĳ���
								for(int i = 0;i < readInt;i++){						//ѭ�������ʱ�������������
									readByte[i]=echoBuffer.get(i);
								}
								echoBuffer.clear();									//����������գ��Ա������һ�δ洢����
								client.register(selector, SelectionKey.OP_READ);
								userNumber = getUserNum(client);
								prepareParse(readByte,client,userNumber);
                            }
							if(readInt < 0)	{						//�ͻ��˹ر�SocketChannel
								//System.out.println("�ͻ����ж�: " + client);
								System.out.println("�ͻ����ж�");
								userExit(client);
								client.close();
							}
                        }
						catch(Exception e){							//���ͻ����ڶ�ȡ���ݲ���ִ��֮ǰ�Ͽ����ӻ�����쳣��Ϣ
							//System.out.println("�ͻ����쳣: " + client);
							System.out.println("�ͻ����쳣");
							if(c.debug){	e.printStackTrace();}
							userExit(client);
							key.cancel();
                            break;
                        }
                    }
                }
				selector.selectedKeys().removeAll(selector.selectedKeys());	//��ȫ���Ѵ����¼�ɾ��
            }
        }
        catch (Exception e){	
			System.out.println(e);
			if(c.debug){	e.printStackTrace();}
		}
	}

	public void prepareParse(byte[] readByte, SocketChannel client,int userNumber)		//Ԥ�����յ����ַ���,��Ϊ���ܻ��ص�
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

		if(userNumber < 0 || (userNumber >= 0 && !Users[userNumber].getIsFile())) {	//(���û���½��Ϣ)��(��֪�û���ͼƬ��Ϣ)
			for(int i = 0;i < results.length;i++){
				System.out.println("����: " + results[i]);
				Parse(results[i],client,userNumber);	
			}
		}
	}

	public void imageSaveControl(byte[] readByte, SocketChannel client,int userNumber){	//��������������ģ��
		try{
			String dirtyResult = new String(readByte,"ISO-8859-1");
			if(dirtyResult.contains("ULMImg")){						//ͼƬ���濪ʼ,�п�����ǰ����޹���Ϣһͬ������,�𿪷ֱ���
				System.out.println("userNumber = " + userNumber);
				Users[userNumber].setIsFile(true);
				int l = ("ULMImg" + c.STATEND).length();
				int position = dirtyResult.indexOf("ULMImg");
				String remain = dirtyResult.substring(position + l);
				if(remain.length() > 0){
					System.out.println("����ͼƬ�ݴ���-��");
					byte [] remains = remain.getBytes("ISO-8859-1");
					saveImage(userNumber,remains);
					byte another[] = dirtyResult.substring(0,position).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another,client,userNumber);
					}
				}
			}
			else if(dirtyResult.contains("SMImgD")){				//ͼƬ�������,�п����������޹���Ϣһͬ������,�𿪷ֱ���
				Users[userNumber].setIsFile(false);
				Users[userNumber].setIsFileSended(true);
				int l = ("SMImgD" + c.STATEND).length();
				int position = dirtyResult.indexOf("SMImgD");
				String remain = dirtyResult.substring(0,position);
				if(remain.length() > 0){
					System.out.println("����ͼƬ�ݴ���-ֹ");
					byte [] remains = remain.getBytes("ISO-8859-1");
					saveImage(userNumber,remains);
					byte another[] = dirtyResult.substring(position + l).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another,client,userNumber);
					}
				}
				if(Users[userNumber].imgTempNum != -1){						//�������Ϊ��
					System.out.println("����ͼƬ��С:" + imgTemps[Users[userNumber].imgTempNum].getTempLength());
					try{													//�����������Ӳ��
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
					imgTemps[Users[userNumber].imgTempNum].newFiles = null;	//���ImgTemps
					imgTemps[Users[userNumber].imgTempNum].setTempLength(0);//���㳤�ȼ�����
				}
				Users[userNumber].setIsFile(false);
				Users[userNumber].setIsFileSended(true);
				String result = "setIsFileSended;true";
				c.sendInforBack(client,result);
			}

			if(Users[userNumber].getIsFile()){								//ͼƬ���������
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

	public void saveImage(int userNumber, byte[] readByte){			//�����������ͼƬreadByteƬ����ʱImgTemps
		if(readByte.length <= 0)return;
		for(int j = 0; j < c.maxUsers; j++){
			if(Users[userNumber].imgTempNum == -1){					//û�ж�Ӧ��ʱImgTemps,����һ���µ�
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
		if(imgTemps[Users[userNumber].imgTempNum].newFiles == null){//��ʱImgTempsδ��ʼ��,��ʼ��
			imgTemps[Users[userNumber].imgTempNum].newFiles = new byte[c.MaxImageLength];
		}
		imgTemps[Users[userNumber].imgTempNum].setNewFiles(readByte,readByte.length);
	}
	
	public void Parse(String str, SocketChannel client ,int userNumber)		//�����ͻ��˷���������
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
			//�����ַ�����������Ӧ����
			if(message[0].equals("login")&&message.length == 3)		//�û���¼��Ϣ
			{
				//��ʽ��"login;userName;password"
				doUserLogin(message,client);
			}
			else if(message[0].equals("regist"))					//�û�ע����Ϣ
			{
				//��ʽ��"regist;id;name;password"
				regist(client,message[1],message[2],message[3]);
			}
			else if(message[0].equals("sitdown"))					//�û�������������(�ѷ���)
			{
				//��ʽ��"sitdown;���ӱ��"
				int deskNumber = Integer.parseInt(message[1]);
				int i = getUserNum(client);
				Users[i].setDeskNumber(deskNumber);
				System.out.println("isStart = " + Tables[deskNumber].getIsStart());
				if(Tables[deskNumber].getIsStart()){
					String result = "AreadyStart";
					c.sendInforBack(client,result);
				}
			}
			else if(message[0].equals("sitdown2"))					//�û�������������
			{
				//��ʽ��"sitdown;���ӱ��;��λ���;ͷ����"
				int deskNumber = Integer.parseInt(message[1]);
				int chairNumber = Integer.parseInt(message[2]);

				GameTable t = Tables[deskNumber];

				if(!t.seat[chairNumber].userId.equals("")&&!t.getIsStart()){
					String result = "NoSit;";
					c.sendInforBack(client,result);
					return;											//�Ѿ����ˣ�����Ϸδ��ʼ���������£������Թ�
				}
				
				int i = getUserNum(client);

				if(deskNumber < 0)									//��һ���û�����ʱ�������Ÿ�ֵ
				{	t.setTableNumber(deskNumber);}
				UAS	Viewers[] = t.getViewers();
				Seat[] seat = t.getSeat();

				if(!t.getIsStart()){								//��Ϸ���
					for(int j = 0;j < Viewers.length; j++)			//��¼�����û�id,name��SocketChannel
					{
						if(Viewers[j].getId().equals("")){			
							t.seat[chairNumber].set(Users[i].getId(),Users[i].getName(),message[3]); 	
							broadcast(seatState());					//ˢ�´�����λ���
							Viewers[j] = Users[i];
							String result = "ackSit;" + deskNumber + ";" + "NotStart";
							StepRecord stepRecords[] = t.getStepRecords();
							c.sendInforBack(client,result);			//������Ϣ
							System.out.println("�û� "+ Viewers[j].getName() + "����" + deskNumber + "�ŷ���");
							break;
						}
					}
				}
				else if(t.getIsStart()){							//�Թ����
					for(int j = 0;j < Viewers.length; j++)			//��¼�����û�id,name��SocketChannel
					{
						if(Viewers[j].getId().equals("")){			
							Viewers[j] = Users[i];
							String result = "ackSit;" + deskNumber + ";";
							StepRecord stepRecords[] = t.getStepRecords();	//��ȡ�����������
							int step = t.getStep();
							result += "AreadyStart;" + step + ";";
							for(int k = 0;k < step;k++){
								result += stepRecords[k].getX() + "," + stepRecords[k].getY() + "," + stepRecords[k].getColor() + ";";
							}
							c.sendInforBack(client,result);			//������Ϣ

							System.out.println("�û� "+Viewers[j].getName()+"����"+deskNumber+"�ŷ���");
							break;
						}
					}
				}

				t.setViewerSum(t.getViewerSum() + 1);				//���ӹ۲�������1
				t.refreshViewersInfor();							//���ӹ۲���ˢ������
				Users[i].setDeskNumber(deskNumber,chairNumber);
			}
			else if(message[0].equals("initGameHallOk"))			//��ҳ�ʼ���ɹ�
			{
				refreshGameHallPlayers();
				c.sendInforBack(client,seatState());	
			}
			else if(message[0].equals("viewPlayersInfor"))			//��ȡָ�������Ϣ
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
					System.out.println(imageName + " �ļ�����:" + file.length());
					if(file.length() > c.MaxImageLength){
						System.out.println("�ļ����ȹ���");//
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
					System.out.println("���ݷ���ʧ��");
					c.sendInforBack(client,"ImgIllegal" + from);
				}
			}
			else if(message[0].equals("setMyPortrait"))				//�������ͷ��
			{
				int i = getUserNum(client);
				Users[i].setPortrait(message[1]);
			}
			else if(message[0].equals("saveDone"))					//���渴�����
			{
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].refreshGamersInforPartly(client);	
				Tables[deskNumber].refreshViewersInforPartly(client);
				c.sendInforBack(client,seatState());
			}
			else if(message[0].equals("initOppoPictOk"))			//��ҳ�ʼ������ͼƬ�ɹ�
			{
				refreshGameHallPlayersPartly(client);
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].setIOPS(Tables[deskNumber].getIOPS() + 1);	//���ƾ������ظ��ϴ�ͼƬ
			}
			else if(message[0].equals("getSeatState"))				//��Ҷ�������ˢ�´�����λ���
			{
				c.sendInforBack(client,seatState());	
			}
			else if(message[0].equals("started"))					//��Ϸ�Ѿ���ʼ��������Ϣ
			{
				//��ʽ��"started;���ӱ��;���̺�����;����������;������ɫ"
				int deskNumber = Integer.parseInt(message[1]);
				int i = getUserNum(client);
				Users[i].setDeskNumber(deskNumber);
				//System.out.println(Users[i].getDeskNumber());
				if(Tables[deskNumber].checkSetDown(message,client)){	
					broadcast(seatState());	
				}
			}
			else if(message[0].equals("ready"))						//�û�׼����Ϸ��Ϣ
			{
				//��ʽ��"ready;���ӱ��"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].doReadyGame(client);
				if(Tables[deskNumber].getIsStart()){				//��Ϸ��ʼ���������ͼƬ
					broadcast(seatState());	
				}
			}
			else if(message[0].equals("userMessage"))				//�û��������û��Ƿ�����Ϣ
			{
				//��ʽ��"userMessage;���ӱ��;Ҫ���͵�����"
				int deskNumber = Integer.parseInt(message[1]);
				int index = str.indexOf(";");
				index = str.indexOf(";", index + 1);
				String mInfor = str.substring(index + 1);
				if(mInfor.equals(""))	return;
				Tables[deskNumber].doChatting(mInfor,client);
			}
			else if(message[0].equals("userBroadcastMessage"))		//�û����������д����û�������Ϣ
			{
				//��ʽ��"userBroadcastMessage;Ҫ���͵�����"
				int index = str.indexOf(";");
				String mInfor = str.substring(index + 1);
				if(mInfor.equals(""))	return; 
				doBroadcastChatting(mInfor,client);
			}
			else if(message[0].equals("userSeparateChatMessage"))	//�û��򵥶��û�������Ϣ
			{
				//��ʽ��"userBroadcastMessage;userId;Ҫ���͵�����"
				String userId = message[1];
				int index = str.indexOf(";");
				index = str.indexOf(";", index + 1);
				String mInfor = str.substring(index + 1);
				if(mInfor.equals(""))	return; 
				doBroadcastChatting(mInfor,client,userId);
			}
			else if(message[0].equals("closeTable"))				//�û��������û��Ƿ�����Ϣ
			{
				//��ʽ��"closeTable;���ӱ��"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].viewerExit(client);
				resetUserSeat(client);
				broadcast(seatState());								//ˢ�´�����λ���
			}
			else if(message[0].equals("rollbackRequest"))			//�û���������
			{
				//��ʽ��"rollbackRequest;���ӱ��;step"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].rollbackForward(message[2],client);
			}
			else if(message[0].equals("replyRBForward"))			//�û�Ӧ���������
			{
				//��ʽ��"replyRBForward;���ӱ��;reply;step"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].doRollback(Integer.parseInt(message[2]),Integer.parseInt(message[3]),client);
			}
			else if(message[0].equals("refreshGamersInforPartly"))	//�û���ȡ������Ϣ
			{
				//��ʽ��"refreshGamersInforPartly;���ӱ��"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].refreshGamersInforPartly(client);
			}
			else if(message[0].equals("refreshViewersInforPartly"))	//�û���ȡ������Ϣ
			{
				//��ʽ��"refreshViewersInforPartly;���ӱ��"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].refreshViewersInforPartly(client);
			}
			else if(message[0].equals("admitLose"))					//�û�����
			{
				//��ʽ��"admitLose;���ӱ��;ִ����ɫ"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].admitLose(message[2]);
				broadcast(seatState());
			}
			else if(message[0].equals("drawRequest"))				//�û���������
			{
				//��ʽ��"drawRequest;���ӱ��;"
				int deskNumber = Integer.parseInt(message[1]);
				Tables[deskNumber].drawRequest(client);
			}
			else if(message[0].equals("drawRequestReply"))			//�û�Ӧ���������
			{
				//��ʽ��"drawRequestReply;���ӱ��;reply"
				int deskNumber = Integer.parseInt(message[1]);
				if(Tables[deskNumber].doDraw(client , Integer.parseInt(message[2]))){
					broadcast(seatState());
				}
			}
			else if(message[0].equals("addFriendRequest"))			//�û���Ӻ�������
			{
				//��ʽ��"addFriendRequest;userId"
				int i = getUserNum(client);
				int j = getUserNumByUserId(message[1]);
				if(isExsitFriend(Users[i].getId(),Users[i].getName(),Users[j].getId(),Users[j].getName())){
					c.sendInforBack(Users[i].getUserChannel(),"friendExsit;");
					return;
				}
				c.sendInforBack(Users[j].getUserChannel(),"addFriendRequest;" +Users[i].getId() + ";"+ Users[i].getName());
			}
			else if(message[0].equals("agreeAddFriend"))			//ͬ���û���Ӻ���
			{
				//��ʽ��"agreeAddFriend;userId"
				int j = getUserNum(client);
				int i = getUserNumByUserId(message[1]);
				addFriend(Users[i].getId(),Users[i].getName(),Users[j].getId(),Users[j].getName());
				c.sendInforBack(Users[j].getUserChannel(),"addFriendAgree;" +Users[i].getId() + ";"+ Users[i].getName());
				c.sendInforBack(Users[i].getUserChannel(),"addFriendAgree;" +Users[j].getId() + ";"+ Users[j].getName());
			}
			else if(message[0].equals("viewFriends"))				//�鿴����
			{
				//��ʽ��"viewFriends;"
				int i = getUserNum(client);
				c.sendInforBack(client,getFriends(Users[i].getId()));
			}
			else if(message[0].equals("delFriend"))					//ɾ������
			{
				//��ʽ��"delFriend;userId;userName"
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

	public void doUserLogin(String []message,SocketChannel client)	//�û���½����ѯ���ݿ⣬��ȡ�û���Ϣ
	{
		boolean isTrueUser = false;
		boolean isAlreadyLogin = false;
		String userId = message[1];
		String pswd = message[2];
		String userName = "";
		String Score = "0";

		if(c.isUseDatabase){	//ʹ�����ݿ�
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

		else{														//��ʹ�����ݿ�
			try{
				FileReader filein = new FileReader("src/users.txt");
				BufferedReader br = new BufferedReader(filein);
				String temp = "";
				while((temp = br.readLine()) != null) {
					try{
						//System.out.println("�����ļ�����:" + temp);
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

			String strRegex = "[\u4e00-\u9fa5a-zA-Z0-9]*";			//������ĸ����
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
						System.out.println("�����ظ���¼");
					}
				}
				if(!isAlreadyLogin){
					for(int i = 0; i < Users.length;i++){
						if(Users[i].getId().equals("")){
							Users[i].setAll(userId,userName,Integer.parseInt(Score),client);	//��¼�����û�id,name��SocketChannel
							System.out.println("�û� "+Users[i].getName() + " ��¼" + "  Id:" + userId + " ����:" + Score);
							break;
						}
					}
					userSum++;
				}
				if(c.isShowUser){
					refreshShowUser();
				}
			}
			else{													//�û���֤ʧ��
				result = "nak";
				System.out.println("��֤ʧ��");
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
			
				FileOutputStream out = new FileOutputStream(txt,true);		//���
				out.write(contents);
				out.close();
				System.out.println("���û�ע��");
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
				File txt = new File("src/friends.txt");				//�������ļ�д��ͷ����Ϣ,����Ϣ����
				FileOutputStream out = new FileOutputStream(txt);	//��д
				byte []contents = (record).getBytes();
				out.write(contents);
				out.close();
				System.out.println("ɾ������");
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
				File txt = new File("src/friends.txt");				//�������ļ�д��ͷ����Ϣ,����Ϣ����
				FileOutputStream out = new FileOutputStream(txt,true);		//���
				String record = iId + ";" + jId +","+ jName + "\r\n" + jId + ";" + iId  +","+ iName + "\r\n";
				byte []contents = (record).getBytes();
				out.write(contents);
				out.close();
				System.out.println("��Ӻ���");
			}
			catch (IOException ee)
			{	System.out.println(ee);}
		}
	}

	public void userExit(SocketChannel client)						//�����û��˳��ͻ���
	{
		int i = getUserNum(client);
		int deskNumber = -1;
		int chairNumber = -1;
		if(i >= 0){
			deskNumber = Users[i].getDeskNumber();
			chairNumber = Users[i].getChairNumber();
		}
		else{
			System.out.println("���ο�����뿪");
			return;
		}
		if(deskNumber >= 0){	
			System.out.println("���ܷ����:"+deskNumber);
			Tables[deskNumber].viewerExit(client);
		}
		if(chairNumber >= 0){
			if(Tables[deskNumber].seat[chairNumber].userId.equals(Users[i].getId())){
				Tables[deskNumber].seat[chairNumber].clear();
			}
		}

		System.out.println("�û�"+Users[i].getName()+"����");
		Users[i].clear();
		userSum--;
		broadcast(seatState());										//ˢ�´�����λ���
		refreshGameHallPlayers();
		if(c.isShowUser){
			refreshShowUser();
		}
	}

	public void refreshShowUser(){									//ˢ�������ʾ���
		showUsers.setText("");
		showUsers.removeAll();
		for(int i = 0,sum = 0; i < Users.length;i++){
			if(!Users[i].getId().equals("")){
				int perLength = 10;									//ID��ʽ������
				String userFormatId = Users[i].getId();
				int idLenth = userFormatId.getBytes().length;
				if(idLenth > 8){
					userFormatId = userFormatId.substring(0,3)+"��";
					idLenth = userFormatId.getBytes().length;
				}
				String idPos = "";
				for(int j= 0;j < perLength - idLenth;j++){
					idPos += "  ";
				}
				String infor = "    ID " + userFormatId + idPos  + "�ǳ� " + Users[i].getName() ;
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

	public void resetUserSeat(SocketChannel client){				//�����λ�е������Ϣ
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

	public String seatState(){										//������λ��Ϣ
		//System.out.println("������λ��Ϣ");
		String seatState = "seatState;";
		for(int j = 0; j < Tables.length; j ++){
			for(int k = 0 ; k < 2 ; k++)
			if(!Tables[j].seat[k].userId.equals("")){
				seatState = seatState + j + "��";
				seatState = seatState + k + "��";
				seatState = seatState + Tables[j].seat[k].userId + "��";
				seatState = seatState + Tables[j].seat[k].userName + "��";
				seatState = seatState + Tables[j].seat[k].pictName + "��";
				seatState = seatState + Tables[j].getIsStart() + "��";
			}
		}
		return seatState;
	}

	public int getUserNum(SocketChannel client)						//����client��ȡ�û����û�����Users[]�еı��
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

	public void doBroadcastChatting(String infor , SocketChannel client){	//��������
		System.out.println("��������:");
		String userName = "";
		String userId = "";
		int userNum = getUserNum(client);							//��ȡ�û����
		userName = Users[userNum].getName();
		userId = Users[userNum].getId();
		String userMessage = "userBroadcastMessage;1;"+ userId + ";" + userName + ";" +infor;
		for(int i = 0;i < Users.length; i++) {						//��������������SocketChannel������Ϣ��������SocketChannel
			if(!Users[i].getId().equals("")){
				if(Users[i].getUserChannel().isConnected()){		//��ȫ���û�������Ϣ�������Լ�
					c.sendInforBack(Users[i].getUserChannel(),userMessage);
				}
				else												//�û���������˿����ӣ��ÿ��û���Ϣ						
				{	Users[i].clear();	}
			}
		}
	}

	public void doBroadcastChatting(String infor , SocketChannel client, String targetUserId){	//�û�˽��
		System.out.println("�û�˽��:");
		String userName = "";
		String userId = "";
		int userNum = getUserNum(client);							//��ȡ�û����
		userName = Users[userNum].getName();
		userId = Users[userNum].getId();
		String userMessage = "userBroadcastMessage;2;"+ userId + ";" + userName + ";" +infor;
		for(int i = 0;i < Users.length; i++) {						//��������������SocketChannel������Ϣ����ȷ��SocketChannel
			if(Users[i].getId().equals(targetUserId)){
				if(Users[i].getUserChannel().isConnected()){		//��ָ���û�������Ϣ
					c.sendInforBack(Users[i].getUserChannel(),userMessage);
					break;
				}
				else												//�û���������˿����ӣ��ÿ��û���Ϣ						
				{	Users[i].clear();	}
			}
		}
		c.sendInforBack(client,userMessage);							//���� ��Ϣ�ķ�����
	}

	public void refreshGameHallPlayers()							//�������û����ʹ��������Ϣ		
	{
		broadcast(createGameHallPlayersInfor());
	}

	public void refreshGameHallPlayersPartly(SocketChannel client)	//���ض��û����ʹ��������Ϣ
	{
		c.sendInforBack(client,createGameHallPlayersInfor());
	}

	public String createGameHallPlayersInfor(){						//���ɴ��������Ϣ userId,usreName,color,score
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
				infor += uId + "��" + uName + "��" + ucolor + "��" + score + "��" + userPortrait + "��";
			}
		}
		return infor;
	}

	public int getScoresByUserId(String Id)							//ͨ��Id��ȡ�û�����
	{
		int userId = Integer.parseInt(Id);
		int userScores = 0;


		if(c.isUseDatabase){										//ʹ�����ݿ�
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

	public void broadcast(String infor)								//�����������û��㲥��Ϣ��ȫ�������㲥
	{
		System.out.println("�����㲥:");
		for(int i = 0;i < Users.length; i++)						//��������������SocketChannel������Ϣ��������SocketChannel
		{
			if(!Users[i].getId().equals("")){
				//System.out.println(Users[i].getId());
				if(Users[i].getUserChannel().isConnected()){
					c.sendInforBack(Users[i].getUserChannel(),infor);
				}
				else												//�û���������˿����ӣ�����û���Ϣ
				{	Users[i].clear();	}
			}
		}
	}

	public void showUser(){											//�� ��ʾ������� ����

		try {
			String src = c.SysImgpath + "default.png";		
			Image image=ImageIO.read(this.getClass().getResource(src));
			this.setIconImage(image);								//����ͼ��
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

		showUsersScroll.setBounds(c.m(30), c.m(50), c.m(220), c.m(260));	//"�û���ʾ��"
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

	public void setImage(String userId){							//�� ������� ��ʾ����,������ѡ��� ����
		ImageIcon icon = new ImageIcon("image/" + userId + ".png");
		icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(),
		icon.getIconHeight(), Image.SCALE_DEFAULT));
		playerImage = icon.getImage();
		paint(this.getGraphics());
	}

	public void actionPerformed(ActionEvent e){}

	public void mouseClicked(MouseEvent e)				
	{
		if(e.getModifiers() != 16) return;							//ֻ��������¼�
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
				 viewersInfors[i].setBackground(c.chatColor);		//��ɫ
			}
		}
	} 
	public void mouseReleased(MouseEvent e){ }
	public void mouseDragged(MouseEvent e){ }
	public void mouseMoved(MouseEvent e){ }
	public void mousePressed(MouseEvent e) { } 
	public void paint(Graphics g)									//��������,������,���ڱ仯������ִ��
	{
		super.paintComponents(g);
		g.drawImage(playerImage,300 ,80 ,90 ,130 ,this); 
	}
}

class ImgTemp														//��ʱ�����������
{
	int userNumber = -1;
	byte []newFiles;												//�������ͼƬ,���1.8406M
	int tempLength = 0;												//ͼƬ�ϴ������зֶ�,�γ���

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