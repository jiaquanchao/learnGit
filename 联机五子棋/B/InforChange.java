/*
 * ����:	 �����
 * QQ:	 	 714670841
 * ����:	 714670841@qq.com
 * ��������:EditPlus
 * Copyright 2014 ����� 
 * ����Ʒֻ���ڸ���ѧϰ���о������ͣ�ת����ע��������
 */

import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.io.File;

class InforChange implements Runnable								//�ͻ������̣߳����е��û�����Ϊֹ
{
	private Constant c;
	private SocketChannel clientChannel;
	private Selector selector;
	private SocketAddress address;
	private boolean openLogin = false;								//����ֻ����һ�ε�¼����
	private boolean isLogin=false;									//�ж��û��Ƿ��Ѿ���¼
	private boolean isOpenWin=false;								//�����û�ֻ�ܴ�һ����Ϸ����
	private String userId;											//�û��˻�
	private String userName;										//�û��ǳ�
	private int score;												//�û�����
	private LoginJFrame login = null;								//��½���� ָ��
	private RegistJFrame regist;									//ע����� ָ��
	private Wuziqi wuziqi;											//��Ϸ���� ָ��
	private GameHall gameHall;										//��Ϸ���� ָ��
	private ViewFriends viewFriends = null;							//���ѽ��� ָ��
	private boolean isHaveOppoImage = false;						//�Ƿ����жԼ�����ͼƬ
	private boolean isPlayerImage = false;							//�Ƿ����ڽ���ͼƬ
	private byte []newPlayerFiles;											
	private int playerTempLength = 0;								//ͼƬ����
	private boolean isMyFileSended = false;							//ͼƬ�Ƿ���
	private boolean isOverBufferSizeLength = false;					//����Ƿ񳬹�c.BUFFER_SIZE����
	private byte [] overLengthStatementBytes;						//���ڱ��泬�������ֽ�����//overLengthStatementBytes

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

	public void setNewPlayerFiles(byte[] readByte, int length){		//����ͼƬ,ͼƬ�ֿ鴫��,��װ
		try{
			for (int i= playerTempLength; i< playerTempLength + length; i++ ){
				newPlayerFiles[i] = readByte[i - playerTempLength];
			}
			playerTempLength += length;
		}
		catch(Exception e){
			System.out.println("ͼƬ����ʧ��" + e);
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

	InforChange(Constant cc)										//�����������������
	{
		c = cc;
		overLengthStatementBytes = new byte[c.MaxByteLength];		//���ڱ��泬�������ֽ�����//overLengthStatementBytes
		newPlayerFiles = new byte[c.MaxImageLength];
		openLogin = true;
		try{
			selector = Selector.open();								//����һ����¼�׽���ͨ���¼��Ķ���
			address = new InetSocketAddress(c.serverIp, c.serverPort);		//����һ����������ַ�Ķ���
			clientChannel = SocketChannel.open(address);					//�����첽�ͻ���
			clientChannel.configureBlocking(false);					//���ͻ����趨Ϊ�첽
			clientChannel.register(selector, SelectionKey.OP_READ);	//����Ѷ������ע��˿ͻ��˵Ķ�ȡ�¼�
			System.out.println("�ɹ����ӵ�������");
			run();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	int pictsum = 0;

	public void newLoginJFrame(String id){							//�򿪵�¼����
		login = new LoginJFrame(c);
		login.setChannel(clientChannel);
		login.setInforChange(this);
		openLogin = false;
		login.startClient();
		login.setIdtext(id);
	}

	public void run()												//�û��ͻ��˳�ʱ������������Ƿ�����Ϣ��������SocketChannel����һֱ����״̬
	{
		try{
			if(openLogin){		
				newLoginJFrame("");
			}
			ByteBuffer readBuffer = ByteBuffer.allocate(c.BUFFER_SIZE);
			int readInt = 0;
			while (true) {
				//System.out.println("������ʼ");
				if (!clientChannel.isOpen())	{					//����ͻ�������û�д򿪾��˳�ѭ��
					System.out.println("����ʧ��");
					break;
				}
				int shijian = selector.select();					//�˷���Ϊ��ѯ�Ƿ����¼��������û�о�����,�еĻ������¼�����
				//if (shijian==0) {	continue;	}					//���û���¼�����ѭ��			//�����ǧ��Ҫ��
				SocketChannel sc;									//����һ����ʱ�Ŀͻ���socket����
				for (final SelectionKey key : selector.selectedKeys())	//�������е��¼�
				{
					if (key.isReadable())	{						//������¼�������Ϊreadʱ,��ʾ�������򱾿ͻ��˷���������
						sc = (SocketChannel) key.channel();			//����ʱ�ͻ��˶���ʵ��Ϊ���¼���socket����
						readBuffer.clear();							//������������Ա��´ζ�ȡ
						try {
							while ((readInt = sc.read(readBuffer)) > 0)	{	//��ѭ���ӱ��¼��Ŀͻ��˶����ȡ�����������������ݵ���������
								//System.out.println("����");
								byte[] readByte = new byte[readInt];	//����һ����ʱbyte����,���볤����Ϊ��ȡ�����ݵĳ���
								for(int i = 0;i < readInt;i++)			//ѭ�������ʱ�������������
								{	readByte[i] = readBuffer.get(i);	}
								prepareParse(readByte);
								readBuffer.clear();						//������������Ա��´ζ�ȡ
								readBuffer.flip();
							}
							if(readInt < 0){
								System.out.println("���������ӹر�!");
							}
						}
						catch(Exception ee){
							System.out.println("���������ӹر�!");
							System.exit(0);								//���ݷ������ر�,�Ƿ�ͬʱ�رտͷ���,����Ҫ��ע�͵�
						}
					}
				}
				selector.selectedKeys().remove(selector.selectedKeys());
				//System.out.println("��������");  
			}
		}
		catch (Exception e)
		{	System.out.println(e);	} 
	}	

	public void prepareParse(byte[] readByte)						//������������������Ԥ����,��ֹ���β�ͬ�����ͬʱ����
	{	
		String dirtyResult = new String(readByte);
		imageSavePlayerControl(readByte);							//ȫ�� ͼƬ���� ����ģ��
		
		String results[];
		try{
			results = dirtyResult.split(c.STATEND);
		}
		catch(Exception e)
		{	return; }

		if(!isPlayerImage) {										//(���û���½��Ϣ)��(��֪�û���ͼƬ��Ϣ)(�����ݲ���ͼƬ)
			if(overLengthStatementBytesLength != 0&&isRemain){
				//System.out.println("ʣ�ಢ��: " + OLSBtoString() +"��"+new String(readByte));
				readByte = preAdd(overLengthStatementBytes,overLengthStatementBytesLength,readByte);
				dirtyResult = new String(readByte);
				results = dirtyResult.split(c.STATEND);
				overLengthStatementBytesLength = 0;
				isRemain = false;
			}

			if(dirtyResult.endsWith(c.STATEND)){					//δ���������
				for(int i = 0;i < results.length;i++){				//�����������
					System.out.println("����: " + results[i]);
					Parse(results[i]);	
				}
			}
			else if(!dirtyResult.endsWith(c.STATEND)){				//��䳬��c.BUFFER_SIZE����,��䳬�����⴦��
				isOverBufferSizeLength = true;
			}

			if(isOverBufferSizeLength){								//�����ۼӹ�����
				overLengthStatementBytesAdd(readByte);
				//System.out.println("�ۼ�" + OLSBtoString());		//��Ƭ����ۼ�(ֱ�Ӱ�byte����ת��ΪString���,ĩβ����ʧ������)
				if(OLSBtoString().contains(c.STATEND)){
					overLengthStatementParse();
				}
			}
		}
	}

	/*************	��䳬������ ģ��	*************/
	public void overLengthStatementParse(){							//��䳬������
		String results[];
		try{
			results = OLSBtoString().split(c.STATEND);
		}
		catch(Exception e)
		{	return; }
		if(OLSBtoString().endsWith(c.STATEND)){
			for(int i = 0;i < results.length;i++){					//�����������
				System.out.println("��������: " + results[i]);
				Parse(results[i]);	
			}
			overLengthStatementBytesLength = 0;						//������䴦�����,���
		}
		else if(!OLSBtoString().endsWith(c.STATEND)){				//(��Ȼ�ǼӺͺ�����)��������һ�����,�����δ����(��ǰһ����ʣ��)
			for(int i = 0;i < results.length - 1;i++){				//ֻ�����������,�������һ����������
				Parse(results[i]);	
			}
			isRemain = true;
			String aa = results[results.length - 1];
			int position = OLSBtoISOString().lastIndexOf(c.STATEND);
			byte remainBytes [] = new byte[overLengthStatementBytesLength - position - c.STATEND.length()];
			for(int i = 0 ; i< remainBytes.length ; i ++){
				remainBytes [i] = overLengthStatementBytes[position + c.STATEND.length() + i];
			}
			//System.out.println("����ʣ��:" + new String(remainBytes));

			overLengthStatementBytesLength = 0;						//��������ʶ�𲿷ִ������,
			overLengthStatementBytesAdd(remainBytes);				//����ʣ����������,�ͺ������Ӻ�(���ϱ�֤ʣ�ಿ�ֲ�ʧ��)
		}
		isOverBufferSizeLength = false;
	}

	public boolean isRemain = false;
	int overLengthStatementBytesLength = 0;							//���鳤��

	public void overLengthStatementBytesAdd(byte[] addBytes){		//����Ƭ����byte[]��װ�Ӻ�.��ֹ���Ӻ�ʧ��,����String����ԭʼbyte[]
		for(int i = overLengthStatementBytesLength ;i< overLengthStatementBytesLength + addBytes.length; i++ ){
			overLengthStatementBytes[i] = addBytes[i - overLengthStatementBytesLength];
		}
		overLengthStatementBytesLength += addBytes.length;
	}

	public String OLSBtoString(){									//�������������ת��Ϊ�ַ���(ĩβ����ʧ��,�˷���ֻ�����жϺʹ�ӡ,�������Ӻ�)
		return new String(overLengthStatementBytes,0,overLengthStatementBytesLength);
	}

	public String OLSBtoISOString(){								//������byte[]��ת��ΪString��ȷ��λ�ַ�λ��,��������String���ɶ�			
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

	public byte[] preAdd(byte[] addBytes,int addLength ,byte[] origBytes){			//�����ʣ��(����������һ�����),�ӵ���һ����ǰ��
		byte newBytes[] = new byte[origBytes.length + addLength];
		for(int i = 0 ;i < addLength; i++){
			newBytes[i] = addBytes[i];
		}
		for(int i = addLength ;i < newBytes.length; i++){
			newBytes[i] = origBytes[i - addLength];
		}
		return newBytes;
	}

	/*************	ͼƬ���� ����ģ��	*************/
	String from = "";
	public void imageSavePlayerControl(byte[] readByte){			//ͼƬ���� ����ģ��
		//byte���ݷ�ΧΪ-128~127�������ԭ���ֽ�������Ϣ����127�Ļ�������ת����String���ͣ�
		//��ת��Ϊbyte[]������ԭʼ�ֽ��벻һ�¡�Ĭ�ϻ���'?'���棬
		//��0xC9���޷���ʾ���ַ�������byte[]->String->byte[]���ͻ���0x3F('?')��
		try{
			String dirtyResult = new String(readByte,"ISO-8859-1");	
			
			if(dirtyResult.contains("DLPImg")){						//downLoadPlayersImage

				isPlayerImage  = true;								//��⵽ͼƬ���ڷ���,��ʼ����ͼƬ
				int l = ("DLPImg" + c.STATEND).length();	
				int position = dirtyResult.indexOf("DLPImg");
				int StrLen = "DLPImg".length();
				String remain = dirtyResult.substring(position + l + 1);				//����from����ռһλ
				from = dirtyResult.substring(position + StrLen,position + StrLen +1);	//����from����ռһλ
				if(from.equals("0")){
					gameHall.setWaitForReply(true);					//������,�鿴�������
				}
				else if(from.equals("1")){
					wuziqi.setWaitForReply(true);					//������,�鿴�������
				}
				else if(from.equals("9")){							//��ʼ��Ϸʱ,���͵Ķ���ͼƬ
					wuziqi.setWaitForReply(true);					//���Ӳ�������,�ȴ�ͼƬ����
				}
				//System.out.println("from = " + from);
				if(remain.length() > 0){
					System.out.println("����ͼƬ�ݴ���-��");
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
					gameHall.setWaitForReply(false);				//������,�鿴�������
				}
				else if(from.equals("1")){
					wuziqi.setWaitForReply(false);					//������,�鿴�������
				}
				else if(from.equals("9")){							//��ʼ��Ϸʱ,���͵Ķ���ͼƬ
					wuziqi.setWaitForReply(false);
					isHaveOppoImage = true;
				}
				int l = ("LPImgD" + c.STATEND).length();
				int position = dirtyResult.indexOf("LPImgD");
				String remain = dirtyResult.substring(0,position);
				if(remain.length() > 0){
					System.out.println("����ͼƬ�ݴ���-ֹ");
					byte [] remains = remain.getBytes("ISO-8859-1");
					setNewPlayerFiles(remains,remains.length);
					byte another[] = dirtyResult.substring(position + l).getBytes("ISO-8859-1");
					if(another.length > 0){
						prepareParse(another);
					}
				}
				System.out.println("����ͼƬ��С:" + playerTempLength);
				playerTempLength = 0;
				if(from.equals("0")){								//������,�鿴�������
					gameHall.getPlayerInfor().setImage(newPlayerFiles);
				}
				else if(from.equals("1")){							//������,�鿴�������
					wuziqi.playerInfor.setImage(newPlayerFiles);
				}
				else if(from.equals("9")){							//��ʼ��Ϸʱ,���͵Ķ���ͼƬ
					wuziqi.setOppoImageByte(newPlayerFiles);
					wuziqi.initOppoImage();
				}
				newPlayerFiles = new byte[c.MaxImageLength];		//���,���⹲��ʱ����
			}

			if(isPlayerImage){
				if((!dirtyResult.contains("DLPImg")) && (!dirtyResult.contains("LPImgD"))) {
					setNewPlayerFiles(readByte,readByte.length);
				}
			}

			if(dirtyResult.contains("ImgIllegal")){					//������
				isPlayerImage  = false;		
				playerTempLength = 0;
				int l = ("ImgIllegal" + c.STATEND).length();	
				int position = dirtyResult.indexOf("ImgIllegal");
				int StrLen = "ImgIllegal".length();
				String ImgIllegal = dirtyResult.substring(position + StrLen,position + StrLen +1);	//����ImgIllegal����ռһλ
				if(from.equals("0")){
					gameHall.setWaitForReply(false);	
					gameHall.addToChatLabel("ϵͳ��ʾ: ��������ȡʧ��");
					gameHall.getPlayerInfor().setImage(new byte[1]);
				}
				else if(from.equals("1")){
					wuziqi.setWaitForReply(false);	
					wuziqi.addToChatLabel("ϵͳ��ʾ: ��������ȡʧ��");
					wuziqi.playerInfor.setImage(new byte[1]);
				}
				if(from.equals("9")){										
					wuziqi.setWaitForReply(false);
					wuziqi.addToChatLabel("ϵͳ��ʾ: ��������ȡʧ��");
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

	/*************	�ַ������� ģ��	*************/
	public void Parse(String result)								//�����ַ���
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
			if(serverInfor[0].equals("ack")){						//ͬ���¼,���������������״̬
				if(serverInfor.length < 2)	return;
				if(!isLogin){
					String []userInfor = serverInfor[1].split(",");
					login.dispose();								//������¼���رյ�¼����
					gameHall =new GameHall(c);
					gameHall.setChannel(clientChannel);
					//System.out.println(userInfor[0] + " " + userInfor[1] + " " + userInfor[2]);				//�û�ID �û��� �û�����
					setUser( userInfor[0],userInfor[1],Integer.parseInt(userInfor[2]));
					gameHall.setUser(userInfor[0],userInfor[1],userInfor[2]);
					isLogin=true;
				}
				gameHall.showMyportrait();							//������ʾͷ��
				gameHall.initMyImageName();							//�����ϴ����󵽷�����
				gameHall.setIsMyFileSended(isMyFileSended);			//�����ϴ����
				gameHall.setIsWaiting(false);						//��������״̬���
				c.sendMessage(clientChannel,"initGameHallOk;");
			}
			else if(serverInfor[0].equals("nak"))					
			{
				login.hint.setText("�û����������");
				login.setPasstext("");
			}
			else if(serverInfor[0].equals("nak_reLogin"))					
			{
				login.hint.setText("�����ظ���¼");
				login.setPasstext("");
			}
			else if(serverInfor[0].equals("start"))					//��Ϸ��ʼ ��ʽ:"start";color;
			{
				if(serverInfor.length < 2)	return;
				wuziqi.start(serverInfor[1]);
			}
			else if(serverInfor[0].equals("ackSit"))				//ͬ������ ��ʽ:"ackSit";tableNumber;["AreadyStart"]
			{
				if(serverInfor.length < 3)	return;
				if(isOpenWin)
				{ return; }
				wuziqi = new Wuziqi(c);								//��������,���������û�����
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
				gameHall.setWaitForReply(false);					//��������ʱ�����ȴ��������
			}
			else if(serverInfor[0].equals("NoSit"))					//��ͬ������ ��ʽ:"NoSit;"
			{
				if(serverInfor.length < 1)	return;
				gameHall.setWaitForReply(false);					//��������ʱ�����ȴ��������
				gameHall.addToChatLabel("��Ϸ��ʾ: ��λ���������");
			}
			else if(serverInfor[0].equals("idRepeat"))				//�˺��ظ� ��ʽ:"idRepeat;"
			{
				if(serverInfor.length < 1)	return;
				try{
					regist.hint.setText("���˺��ѱ�ע��");
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else if(serverInfor[0].equals("registSuccess"))			//ע��ɹ� ��ʽ:"registSuccess;"
			{
				if(serverInfor.length < 1)	return;
				try{
					regist.newLoginJFrame(regist.getIdtext().getText().trim());
					login.hint.setText("�˺�ע��ɹ�");
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			else if(serverInfor[0].equals("seatState"))				//���´��������Ϣ��λ���
			{
				if(serverInfor.length > 1){
					gameHall.refreshSeatState(serverInfor[1]);
				}
				else{
					gameHall.refreshSeatState("none");
				}
			}
			else if(serverInfor[0].equals("refreshGameHallPlayers"))//���´��������Ϣ
			{
				if(serverInfor.length < 2)	return;
				gameHall.refreshGameHallPlayers(serverInfor[1]);
			}
			else if(serverInfor[0].equals("readyGame"))				//���׼����Ϸ
			{
				if(serverInfor.length < 2)	return;
				wuziqi.addToChatLabel("��Ϸ��ʾ:" + serverInfor[1] + " ׼����Ϸ");
			}
			else if(serverInfor[0].equals("initGame"))				//�Թۣ���Ϸ�Ѿ���ʼ�����г�ʼ��
			{
				wuziqi.initGame(result);
			}
			else if(serverInfor[0].equals("located"))				//��Ϸ��ʼ����������λ�ü�������ɫ
			{	
				if(serverInfor.length < 4)	return;
				int xi = Integer.parseInt(serverInfor[1]);
				int yj = Integer.parseInt(serverInfor[2]);
				int set_color = Integer.parseInt(serverInfor[3]);
				wuziqi.setDown(xi, yj , set_color);
			}
			else if(serverInfor[0].equals("gameEnd"))				//��Ϸ����
			{
				if(serverInfor.length < 6)	return;
				wuziqi.lblWin.setText(wuziqi.startColor(Integer.parseInt(serverInfor[1])) + "Ӯ��!"); 
				wuziqi.addToChatLabel("��Ϸ��ʾ: " + wuziqi.startColor(Integer.parseInt(serverInfor[1])) + "Ӯ��");
				wuziqi.SetWinLineRecord(serverInfor[2],serverInfor[3],serverInfor[4],serverInfor[5]);
				wuziqi.drawWinLine();
				wuziqi.gameEnd();
			}
			else if(serverInfor[0].equals("gameDraw"))				//��Ϸ����
			{
				wuziqi.lblWin.setText("ƽ��!"); 
				wuziqi.addToChatLabel("��Ϸ��ʾ: ƽ��");
				wuziqi.gameEnd();
			}
			else if(serverInfor[0].equals("mycolor"))				//��ȡ�Լ�������ɫ
			{
				if(serverInfor.length < 2)	return;
				int user_color = Integer.parseInt(serverInfor[1]);
				wuziqi.setColor(user_color);
			}
			else if(serverInfor[0].equals("gamerExited"))
			{
				isHaveOppoImage = false;							//��Ҫ,��Ȼ���ӡ��ͼƬbyte[]
				wuziqi.oppsInfor.setText("");
				wuziqi.lblWin.setText("����뿪����Ϸ");
				wuziqi.setIsHaveOppoImage(false);
				wuziqi.oppoImage = null;
				wuziqi.myPaint();
				wuziqi.gameEnd();
			}
			else if(serverInfor[0].equals("userMessage"))			//������ҷ�����Ϣ
			{
				int index = result.indexOf(";");
				index = result.indexOf(";", index + 1);
				index = result.indexOf(";", index + 1);
				String mInfor = result.substring(index + 1);
				if(mInfor.equals(""))	return; 
				wuziqi.getMessage(serverInfor[1],serverInfor[2],mInfor);			//userMessage;userId;userName ;infor
			}
			else if(serverInfor[0].equals("userBroadcastMessage"))	//��ҷ��������㲥��Ϣ
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
				isHaveOppoImage = false;							//��Ҫ,��Ȼ���ӡ��ͼƬbyte[]
			}
			else if(serverInfor[0].equals("rollbackForward"))		//�Լһ�������
			{
				if(serverInfor.length < 2)	return;
				wuziqi.replyRBForward(serverInfor[1]);
			}
			else if(serverInfor[0].equals("rollbackReply"))			//��������������
			{
				if(serverInfor.length < 4)	return;
				if(serverInfor[1].equals("yes")){
					wuziqi.rollback(Integer.parseInt(serverInfor[2]),Integer.parseInt(serverInfor[3]));
				}
				else if(serverInfor[1].equals("no")){
					wuziqi.noRollback(Integer.parseInt(serverInfor[2]));
				}
			}
			else if(serverInfor[0].equals("drawRequest"))			//�ԼҺ�������
			{
				wuziqi.drawRequest();
			}
			else if(serverInfor[0].equals("noDraw"))				//�ԼҾܾ�����
			{
				wuziqi.addToChatLabel("��Ϸ��ʾ: �Է��ܾ�����");
				wuziqi.setWaitForDrawReply(false);
				wuziqi.draw.setEnabled(true);
			}
			else if(serverInfor[0].equals("refreshGamersInfor"))	//ˢ�������Ϣ(����)
			{
				if(serverInfor.length < 2)	return;
				if(serverInfor.length == 3)
				{	wuziqi.refreshGamersInfor(serverInfor[1],serverInfor[2]);}
				if(serverInfor.length == 2)							//������ܣ�ʣһ��
				{	wuziqi.refreshGamersInfor(serverInfor[1]);}
			}
			else if(serverInfor[0].equals("refreshViewersInfor"))	//ˢ�����й۲�����Ϣ(����)
			{
				if(serverInfor.length < 2)	return;
				wuziqi.refreshViewersInfor(serverInfor[1]);
			}
			else if(serverInfor[0].equals("setIsFileSended"))		//��Ϸ��ʼ����������λ�ü�������ɫ
			{	
				if(serverInfor.length < 2)	return;
				boolean isMyFileSended = Boolean.parseBoolean(serverInfor[1]);
				setIsMyFileSended(isMyFileSended);
			}
			else if(serverInfor[0].equals("addFriendRequest"))		//��Ӻ�������
			{	
				if(serverInfor.length < 3)	return;
				gameHall.addFriendRequest(serverInfor[1],serverInfor[2]);
			}
			else if(serverInfor[0].equals("addFriendAgree"))		//��Ӻ�������
			{	
				if(serverInfor.length < 3)	return;
				gameHall.addToChatLabel("��Ϸ��ʾ: ���ɹ���� " + serverInfor[2] + "(" + serverInfor[1] + ") Ϊ����");
			}
			else if(serverInfor[0].equals("Myfriends"))				//������Ϣ
			{	
				if(serverInfor.length < 1)	return;
				if(viewFriends != null){
					return;
				}
				viewFriends = new  ViewFriends(c,result);
				viewFriends.setInforChange(this);
				viewFriends.setGameHall(gameHall);
			}
			else if(serverInfor[0].equals("friendExsit"))			//�����Ѿ�����
			{
				gameHall.addToChatLabel("��Ϸ��ʾ: �Է��������ĺ����б���");
			}
			else if(serverInfor[0].equals("delSuccess"))			//����ɾ����Ϣ
			{
				if(serverInfor.length == 1){
					gameHall.addToChatLabel("��Ϸ��ʾ: ����ɾ���ɹ�");
				}
				if(serverInfor.length == 2){
					gameHall.addToChatLabel("��Ϸ��ʾ: " + serverInfor[1] +" �����Ӻ�����ɾ��");
				}
			}	
			else{
				//System.out.println("��Ч���");
			}
		}
	} 
}