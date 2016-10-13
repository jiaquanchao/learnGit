/*
 * ����:	 �����
 * QQ:	 	 714670841
 * ����:	 714670841@qq.com
 * ��������:EditPlus
 * Copyright 2014 ����� 
 * ����Ʒֻ���ڸ���ѧϰ���о������ͣ�ת����ע��������
 */

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class GameHall extends JFrame implements ActionListener, MouseListener, MouseMotionListener
{
	private Constant c;
	private SocketChannel clientChannel;					//��������Ϣ
	private JLabel game_logo;								//��Ϸͼ����ʾ
	private JLabel game_name;								//��Ϸ������ʾ
	private JLabel game_sum;								//��Ϸ������
	private JLabel my_portrait;								//�û�ͷ����ʾ
	private JLabel my_name;									//�û�������ʾ
	private JLabel my_infor;								//�û�������ʾ
	private JLabel tButton[];
	private JLabel cButton[];
	private JLabel cName[];
	private JLabel desknum[];								//��ʾ���ӱ��
	private String imageName = "default.PNG";				//ͷ����
	private String myImageName = "default.PNG";				//������
	private boolean isMyFileSended = false;
	private boolean isSitted = false ; 
	private boolean isWaiting = true;						//����������,��ֹ����
	private boolean waitForReply = false ; 
	private String userId;									//�û���Ϣ
	private String userName;
	private String score;
	private JTextArea showUsers = new JTextArea("");
	private JScrollPane	showUsersScroll = new JScrollPane(showUsers);
	private JTextArea chatLabel = new JTextArea("");
	private JScrollPane	chatLabelScroll = new JScrollPane(chatLabel);
	private JTextField messageField = new JTextField();
	private JButton viewFriends = new JButton("����");
	private JButton chatControl = new JButton("����");
	private JButton sendMessage = new JButton("����");
	private JButton clearMessage = new JButton("���");
	private JScrollPane	deskPanel = new JScrollPane();
	private PopupMenu viewUserPM = new PopupMenu();
	private MenuItem VUChat = new MenuItem();
	private MenuItem VUAddFriend = new MenuItem();
	private PopupMenu showChatUser = new PopupMenu();
	private MenuItem chatUser = new MenuItem();	
	private PlayerInfor playerInfor = null;
	private boolean alreadyAddListener = false;
	private ImageIcon background;
	private JLabel bgLabel3;
	private ImageIcon imageLogo;
	private ImageIcon tableWaitImage;
	private ImageIcon chairWaitImage;
	private JPanel jPanel;
	private Image image;
	private ImageIcon myPortraitImage;
	private boolean firstOpen = true;
	private String chatLabelLineUserId[];
	private String chatLabelLineUserName[];
	private JLabel chatLabelInfor[];
	private Toolkit toolkit;
	private Dimension screenSize;

	public PlayerInfor getPlayerInfor(){
		return playerInfor;
	}

	public void setUser(String uId,String uName,String sc)
	{
		userId = uId;
		userName = uName;
		score = sc;
	}

	public void setIsWaiting(boolean isW)
	{	isWaiting = isW;}

	public void setChannel(SocketChannel socketChannel)
	{	clientChannel = socketChannel;	}
	public SocketChannel getChannel()
	{	return clientChannel;	}

	public void setIsSitted(boolean isSit)
	{	isSitted = isSit;}

	public void setWaitForReply(boolean wtfp)
	{	waitForReply = wtfp;}

	public void setMyImageName(String myIName)
	{	myImageName = myIName;	}

	public String getMyImageName(){
		return myImageName;
	}

	public boolean getIsMyFileSended()
	{	return isMyFileSended;}

	public void setIsMyFileSended(boolean isfes)
	{	isMyFileSended = isfes; }

	public void setChatTo(String userId , String userName){
		boolean isExist = false;
		for(int i = 0;i < playersInforsID.length;i++){
			if(playersInforsID[i] != null){
				if(playersInforsID[i].equals(userId)){
					isExist = true;
				}
			}
		}
		if(isExist == false){
			addToChatLabel("ϵͳ��ʾ: ���ĺ��� " + userName + " ������");
			return;
		}
		aimedUserId = userId;
		aimedUserName = userName;
		chatControl.setMargin(new Insets(0,0,0,0));					//�������������
		chatControl.setText("˽��:" + aimedUserName);
		confirmChat = true;
	}

	public void delFriend(String userId,String userName){
		c.sendMessage(clientChannel,"delFriend" + ";" + userId + ";" + userName);
	}

	GameHall(Constant cc)											//��Ϸ����
	{
		super("��Ϸ����");											//���ñ���
		c = cc;
		chatLabelLineUserId = new String[200];
		chatLabelLineUserName = new String[200];
		chatLabelInfor = new JLabel[200];
		tButton = new JLabel[c.DeskNum];
		cButton = new JLabel[c.ChairNum];
		cName = new JLabel[c.ChairNum];
		desknum = new JLabel[c.DeskNum];
		toolkit = this.getToolkit();
		screenSize = toolkit.getScreenSize();
		init();
		addToChatLabel("ϵͳ��ʾ: ��ӭ������Ϸ����");
	}

	public void init(){
		if(c.wsizex > screenSize.width + 20){						//screenSize.width��ʵ����Ļ�ߴ�,�Ƿֱ���
			System.out.println("���ڹ���");
			newMultiple("fit");										//��������Ӧ���ڴ�С
		}
		if(!firstOpen){
			remove(game_logo);
			remove(game_name);
			remove(my_portrait);
			this.getLayeredPane().remove(bgLabel3);
			remove(my_name);
			remove(my_infor);
			remove(game_sum);
			paintMyPortrait();
		}
		try {
			String src = c.SysImgpath + "default.png";		
			image = ImageIO.read(this.getClass().getResource(src));
			this.setIconImage(image);								//����ͼ��
		}
		catch (Exception e) {
			System.out.println(e);
		}
		background = new ImageIcon(c.SysImgpath + "bg3.jpg");
		background = new ImageIcon(background.getImage().getScaledInstance(c.wsizex,c.m(75),Image.SCALE_FAST));	//ͨ��Image��СImageIcon
		bgLabel3 = new JLabel(background);
		bgLabel3.setBounds(c.m(0),c.m(-35),c.wsizex,c.m(75));
		this.getLayeredPane().add(bgLabel3, new Integer(Integer.MIN_VALUE));
		((JPanel)getContentPane()).setOpaque(false);

		imageLogo = new ImageIcon(c.SysImgpath + "logo.png");
		imageLogo = new ImageIcon(imageLogo.getImage().getScaledInstance(c.m(33),c.m(33),Image.SCALE_FAST));	//ͨ��Image��СImageIcon
		game_logo = new JLabel(imageLogo);
		add(game_logo);
		game_logo.setBounds(c.m(-5), c.m(-5), c.m(50), c.m(50));	//������Ϸͼ��

		game_name = new JLabel("<html><font size = 4><b>������</b></font></html>");
		add(game_name);
		game_name.setBounds(c.m(42), c.m(-13), c.m(50), c.m(50));	//������Ϸ����

		game_sum = new JLabel("");
		add(game_sum);
		game_sum.setBounds(c.m(42), c.m(0), c.m(50), c.m(50));		//������Ϸ������
		if(Players != null){
			addGameSum();
		}

		tableWaitImage = new ImageIcon(c.TableWaitImgPath);
		tableWaitImage = new ImageIcon(tableWaitImage.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));	//ͨ��Image��СImageIcon

		for(int i=0 ; i < tButton.length ; i++){					//ʵ��������
			tButton[i] = new JLabel(tableWaitImage);
		}

		chairWaitImage = new ImageIcon(c.ChairWaitImgPath);
		chairWaitImage = new ImageIcon(chairWaitImage.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));	//ͨ��Image��СImageIcon
		for(int i=0 ; i< cButton.length ; i++){						//ʵ��������
			cButton[i] = new JLabel(chairWaitImage);
			cName[i] = new JLabel("");
		}

		setLayout(null);
		jPanel = new JPanel(null);
		jPanel.setOpaque(false);
		
		for(int i=0 ; i < tButton.length ; i++){
			int x = ( i % c.perLineDesks ) + 1 ;
			int y = ( i / c.perLineDesks ) + 1;
			jPanel.add(tButton[i]);									//��������
			tButton[i].setBounds(c.m(100) * x - c.m(60), c.m(75) * y - c.m(60), c.m(30), c.m(30));			//��������

			desknum[i] = new JLabel("<html><font size = 3 color = 'white'>- "+ (i+1) + " -</font></html>");
			jPanel.add(desknum[i]);
			desknum[i].setBounds(c.m(100) * x - c.m(52), c.m(75) * y - c.m(40), c.m(40), c.m(40));			//�������ӱ��("- i -")

			jPanel.add(cButton[2 * i]);								//��������
			jPanel.add(cButton[2 * i + 1]);							//��������
			cButton[2 * i].setBounds(c.m(100) * x - c.m(85), c.m(75) * y - c.m(55), c.m(20), c.m(20));		//��������
			cButton[2 * i + 1].setBounds(c.m(100) * x - c.m(25), c.m(75) * y - c.m(55), c.m(20), c.m(20));	//��������

			jPanel.add(cName[2 * i]);								//�������Ӷ�Ӧ����
			jPanel.add(cName[2 * i + 1]);							//�������Ӷ�Ӧ����
			cName[2 * i].setBounds(c.m(100) * x - c.m(80), c.m(75) * y - c.m(50), c.m(50), c.m(50));		//�������Ӷ�Ӧ����
			cName[2 * i + 1].setBounds(c.m(100) * x - c.m(20), c.m(75) * y - c.m(50), c.m(50), c.m(50));	//�������Ӷ�Ӧ����
					
			tButton[i].addMouseListener(this);						//�ڶ��γ�ʼ���Ѿ�remove,�����¼���Listener
			cButton[2 * i].addMouseListener(this);
			cButton[2 * i + 1].addMouseListener(this);
		}
		int rownum = ((c.DeskNum + c.perLineDesks - 1)/ c.perLineDesks);
		jPanel.setPreferredSize(new Dimension (c.m(330), c.m(80) * rownum));	//���ӹ�������ʾ��С(������)
		deskPanel.getViewport().add(jPanel);									//ע�����������˳��
		deskPanel.getViewport().setBackground(new Color(94,131,173));			//ע�����������˳��
		add(deskPanel);
		
		deskPanel.setBounds(c.m(0), c.m(60), c.nowDeskPanelLength, c.m(288));					//���ӹ�������λ�ü���С(������)//ע��c.x()��λ��
		deskPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		showUsersScroll.setBounds(c.extendL(330) + c.dev_x, c.m(60), c.m(140), c.m(100));		//"�û���ʾ��"
		add(showUsersScroll);
		showUsers.setOpaque(true);
		showUsers.setBackground(c.chatColor);

		chatLabelScroll.setBounds(c.extendL(330) + c.dev_x, c.m(170), c.m(140), c.m(130));		//"������ʾ��"
		add(chatLabelScroll);
		chatLabel.setOpaque(true);
		chatLabel.setBackground(c.chatColor);

		add(messageField);
		messageField.setBounds(c.extendL(360) + c.dev_x, c.m(300), c.m(110), c.m(15));			//"��Ϣ�����"
		
		messageField.setBorder (BorderFactory.createLineBorder(Color.gray,1));
		messageField.setOpaque(true);
		messageField.setBackground(c.chatColor);

		add(viewFriends);
		viewFriends.setBounds(c.extendL(430) + c.dev_x, c.m(10), c.m(40), c.m(15));				//"�鿴����"
		
		add(chatControl);
		chatControl.setBounds(c.extendL(330) + c.dev_x, c.m(300), c.m(30), c.m(15));			//"�������"
		
		add(sendMessage);
		sendMessage.setBounds(c.extendL(330) + c.dev_x, c.m(320), c.m(40), c.m(15));			//"����"

		add(clearMessage);
		clearMessage.setBounds(c.extendL(430) + c.dev_x, c.m(320), c.m(40), c.m(15));			//"���"
		
		showUsers.setEditable(false); 
		chatLabel.setEditable(false);

		VUChat.setLabel("                ˽��                ");
		VUAddFriend.setLabel("            ��Ӻ���             ");
		
		viewUserPM.add(VUChat);
		viewUserPM.add(VUAddFriend);
		add(viewUserPM);

		showChatUser.add(chatUser);
		add(showChatUser);

		if(firstOpen){
			addMouseListener(this);
			viewFriends.addActionListener(this);
			chatControl.addActionListener(this);
			chatControl.addMouseListener(this);
			sendMessage.addActionListener(this);
			messageField.addActionListener(this);
			clearMessage.addActionListener(this);
			VUChat.addActionListener(this);
			VUAddFriend.addActionListener(this);
		}

		viewFriends.setMargin(new Insets(0,0,0,0));	
		sendMessage.setMargin(new Insets(0,0,0,0));	
		clearMessage.setMargin(new Insets(0,0,0,0));	
		chatControl.setMargin(new Insets(0,0,0,0));	
		
		immediateRefreshSeatState();
		
		this.setSize(c.wsizex,c.wsizey);
		if(c.isFitPosition){
			this.setLocationRelativeTo(null);  
		}
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		if(!alreadyAddListener){
			alreadyAddListener = true;
			this.addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e){
					if(!isSitted){
						System.exit(0);
					}
					else{
						addToChatLabel("ϵͳ��ʾ: ����δ��������Ϸ");
					}
				}
			});
			
			this.addWindowStateListener(new WindowStateListener () {
				public void windowStateChanged(WindowEvent state) {
					if(state.getNewState() == 0) {					//"���ڻ�ԭ"����
						int nowX = c.wsizex;
						int nowY = c.wsizey;
						double nowXmultiple = (nowX * 1.0)/c.wsizex * c.multiple;
						double nowYmultiple = (nowY * 1.0)/c.wsizey * c.multiple;
						double newMultiple;
						if(nowXmultiple < nowYmultiple){
							newMultiple = (nowX * 1.0) / 512;
						}
						else{
							newMultiple = (nowY * 1.0) / 364;
						}
						if(newMultiple != c.initMutiple){
							if(c.debug){	System.out.println("���ڻ�ԭ");}
							c = new Constant(c.initMutiple);
							init();
						}
					}
					else if(state.getNewState() == 6) {				//"�������"����
						if(newMultiple("max")){
							init();
						}
					}
				}         
			}); 
		}
		firstOpen = false;
	}

	public boolean newMultiple(String change){
		try{
			int nowX = getWidth();
			int nowY = getHeight();
			if(change.equals("max")){
				nowX = getWidth();
				nowY = getHeight();
			}
			else{
				nowX = screenSize.width - 30;
				nowY = screenSize.height - 30;
			}
			double nowXmultiple = (nowX * 1.0)/c.wsizex * c.multiple;
			double nowYmultiple = (nowY * 1.0)/c.wsizey * c.multiple;
			double newMultiple;
			if(nowXmultiple < nowYmultiple){
				newMultiple = (nowX * 1.0) / 512;
			}
			else{
				newMultiple = (nowY * 1.0) / 364;
			}
			if(newMultiple != c.multiple){
				if(c.debug){
					System.out.println("nowX =" + nowX);
					System.out.println("nowY =" + nowY);
					System.out.println("nowXmultiple =" + nowXmultiple);
					System.out.println("nowYmultiple =" + nowYmultiple);
					System.out.println("newMultiple =" + newMultiple);
				}
				if(change.equals("max")){
					if(c.debug){	System.out.println("�������");} 
					c = new Constant(c.initMutiple,newMultiple ,nowX ,nowY ,c.wsizex ,c.wsizey, false, c.nowDeskPanelLength);
				}
				else{
					if(c.debug){	System.out.println("��������Ӧ");} 
					c = new Constant(newMultiple);
				}
				return true;
			}
		}
		catch(Exception e){
			System.out.println(e);
			if(c.debug) {	e.printStackTrace();}
		}
		return false;

	}

	public void actionPerformed(ActionEvent e)
	{ 
		if(c.debug){	System.out.println("����Action");}
		if (e.getSource() == sendMessage || e.getSource() == messageField) {		//������� �� ���س���
			if(aimedUserId.equals("")||confirmChat == false){
				String text = messageField.getText();
				if(text.equals(""))	return;
				String newcontent = "userBroadcastMessage;" + text;	//���� ͨ��
				c.sendMessage(clientChannel,newcontent);
				messageField.setText("");
			}
			else{
				String text = messageField.getText();
				if(text.equals(""))	return;
				String newcontent = "userSeparateChatMessage;" + aimedUserId + ";" + text;		//���� ͨ��
				c.sendMessage(clientChannel,newcontent);
				messageField.setText("");
			}
		}
		else if (e.getSource() == clearMessage) {					//�����Ϣ��¼
			String text = chatLabel.getText();
			if(text.equals(""))	return;
			chatLabel.setText("");
			chatLabel.removeAll();
			chatLabelsum = 0;
		}
		else if (e.getSource() == viewFriends) {					//�鿴����
			c.sendMessage(clientChannel,"viewFriends;");
		}
		else if (e.getSource() == VUChat){							//��ĳȷ���������
			if(aimedUserId.equals(userId)){							//���Լ�����,�ǲ������
				aimedUserId = "";
				aimedUserName = "";
				addToChatLabel("ϵͳ��ʾ: �������Լ�˽��");
				chatControl.setText("����");
				confirmChat = false;
				return;
			}
			if(!aimedUserId.equals("")){
				chatControl.setText("˽��:" + aimedUserName);		//�������������
				confirmChat = true;	
			}
		}
		else if (e.getSource() == VUAddFriend){						//���ĳȷ�����Ϊ����
			if(aimedUserId.equals(userId)){
				aimedUserId = "";
				aimedUserName = "";
				addToChatLabel("ϵͳ��ʾ: ��������Լ�Ϊ����");
				confirmAF = false;
				return;
			}
			if(!aimedUserId.equals("")){
				int i = JOptionPane.showConfirmDialog(null, "��� " + aimedUserName + " Ϊ���ѣ�", "ȷ�����", JOptionPane.YES_NO_OPTION);
				if(i == JOptionPane.YES_OPTION){
					c.sendMessage(clientChannel,"addFriendRequest;" + aimedUserId);
				}
			}
			confirmAF = false;
		}
		else if (e.getSource() == chatControl) {
			if(!chatControl.getText().equals("����")){				//���ع���Ƶ��,��¼��һ�κϷ�˽�����
				if(!aimedUserId.equals("")&&confirmChat){			//������һ�������������ȷ������
					formerChatToUserId = aimedUserId;				//��¼��һ�κϷ�˽�����
					formerChatToUserName = aimedUserName;
				}
				aimedUserId = "";									//���ع���Ƶ��,���ÿ�
				aimedUserName = "";
				chatControl.setText("����");
				confirmChat = false;
			}
			else{													//��д�ϴ�ͨ�����
				if(!formerChatToUserId.equals("")){
					aimedUserId = formerChatToUserId;
					aimedUserName = formerChatToUserName;
					chatControl.setMargin(new Insets(0,0,0,0)); 
					chatControl.setText("˽��:" + aimedUserName);
					confirmChat = true;
				}
			}
		}
	}

	String formerChatToUserId = "";
	String formerChatToUserName = "";
	//String formerchatToUserPortrait = "";

	String aimedUserId = "";
	String aimedUserName = "";
	//String chatToUserPortrait = "";
	boolean confirmChat = false;
	boolean confirmAF = false;
	public void mouseClicked(MouseEvent e)							//��ָ��λ��ȷ������λ��
	{
		if(c.debug){	System.out.println("����mouseClicked");}
		if(e.getModifiers() == 4){
			for(int i = 0 ; i < playersInfors.length ; i++){
				if (e.getSource() == playersInfors[i]){				//�� �û���ʾ�� ����� �Ҽ��˵�
					viewUserPM.show(playersInfors[i],e.getX(),e.getY());
					aimedUserId = playersInforsID[i];
					aimedUserName = playersInforsName[i];
					//chatToUserPortrait = playersPortrait[i];
					break;
				}
			}

			for(int i = 0 ; i < chatLabelInfor.length ; i++){
				if (e.getSource() == chatLabelInfor[i]){			//�� �����¼�� ����� �Ҽ��˵�
					viewUserPM.show(chatLabelInfor[i],e.getX(),e.getY());
					aimedUserId = chatLabelLineUserId[i];
					aimedUserName = chatLabelLineUserName[i];
					break;
				}
			}
		}

		if(e.getModifiers() == 16) {								//ֻ��������¼�
			if(isWaiting) {
				addToChatLabel("ϵͳ��ʾ: �ȴ�ϵͳ��ʼ��");
				return;
			}
			if(waitForReply){
				addToChatLabel("ϵͳ��ʾ: �ȴ���������Ӧ");
				return;
			}
			/*
			for(int i=0 ; i<tButton.length ; i++){
				if (e.getSource() == tButton[i]){
					if(!isSitted){
						isSitted  = true ;
						c.sendMessage(clientChannel,"sitdown;"+ i+";");
					}
					break;
				}
			}*/
			for(int i = 0 ; i < cButton.length ; i++){
				if (e.getSource() == cButton[i]){
					if(!isSitted){
						c.sendMessage(clientChannel,"sitdown2;" + (i / 2) + ";" + (i % 2) + ";" + imageName);
						waitForReply = true;
					}
					else{
						addToChatLabel("ϵͳ��ʾ: ���Ѿ�������Ϸ");
					}
					break;
				}
			}
			
			for(int i = 0 ; i < playersInfors.length ; i++){
				if (e.getSource() == playersInfors[i]){				//�鿴�����Ϣ
					waitForReply = true;
					if(playerInfor != null){
						playerInfor.dispose();
						playerInfor = null;
					}
					playerInfor = new PlayerInfor(c);
					playerInfor.setInfor(playersInforsID[i],playersInforsName[i],playersInforsScore[i],playersPortrait[i]);
					c.sendMessage(clientChannel,"viewPlayersInfor;" + playersInforsID[i] + ";GameHall");
				}
			}
			
			if (e.getSource() == my_portrait){						//�޸�ͷ��
				JFileChooser chooser = new JFileChooser(c.portrait_path);
				int status = chooser.showOpenDialog(null);
				if(status != JFileChooser.APPROVE_OPTION)
				{	System.out.println("No file chosen!");}
				else{
					try{
						File file = chooser.getSelectedFile();
						String prefix = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
						System.out.println(prefix);
						String allowTypes = "jpg,png,gif";
						String allowtypes[] = allowTypes.split(",");
						boolean filterResult = filterType(prefix,allowtypes);
						if (filterResult) {
							File txt = new File("src/my_portrait.txt");				//�������ļ�д��ͷ����Ϣ,����Ϣ����
							FileReader filein = new FileReader("src/my_portrait.txt");
							BufferedReader br = new BufferedReader(filein);
							String temp = null;
							String record = "userId = " + userId + ";imageName = " + file.getName() + "\r\n";
							while((temp = br.readLine()) != null) {
								//System.out.println(temp);
								String dlls[] = temp.split(";");
								String dllIds[] = dlls[0].split("=");
								String dllId = dllIds[1].trim();
								if(!dllId.equals(userId)){
									record = record + temp + "\r\n";
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
							addToChatLabel("ϵͳ��ʾ: ͷ���޸ĳɹ�,������Ϸ��Ч");
						}
						else{
							System.out.println("��֧�ָ��ļ����ͣ�");
						}
					}
					catch(Exception e3)
					{	System.out.println(e3);}
				}
			}
		}
	}
	
	public boolean filterType(String fileType,String[] types) {
		for (String type : types) {
			if (fileType.equals(type)) {
				return true;
			}
		}
		return false;
	}

	public void mouseEntered(MouseEvent e){
		for(int i = 0 ; i< playersInfors.length ; i++){
			if (e.getSource() == playersInfors[i]){
				playersInfors[i].setOpaque(true);
				playersInfors[i].setBackground(new Color(48,117,174)); 
			}
		}
		if (e.getSource() == chatControl){
			if(!aimedUserName.equals("")&&confirmChat){
				chatUser.setLabel(aimedUserName);
				showChatUser.show(chatControl,0,-30);
			}
		}
	}
	public void mouseExited(MouseEvent e) { 
		for(int i = 0 ; i< playersInfors.length ; i++){
			if (e.getSource() == playersInfors[i]){
				 playersInfors[i].setBackground(c.chatColor); 
			}
		}
	} 
	public void mouseReleased(MouseEvent e){ }
	public void mouseDragged(MouseEvent e){}
	public void mouseMoved(MouseEvent e){ }
	public void mousePressed(MouseEvent e) { } 
	public void paint(Graphics g)
	{
		super.paintComponents(g);									// ���ƴ������е�ÿ�����
	} 

	JLabel playersInfors[] = new JLabel[10];
	String playersInforsID[] = new String[10];
	String playersInforsName[] = new String[10];
	String playersInforsScore[] = new String[10];
	String playersPortrait[] = new String[10];
	String Players[];

	public void refreshGameHallPlayers(String Player)				//ˢ�´��������Ϣ
	{
		//��ʽ��"userId , usreName , color , score"
		Players = Player.split("��");
		String infor = "";
		showUsers.setText("");
		showUsers.removeAll();
		int perLength = 10;
		for(int i = 0 ; i < Players.length ; i++){
			String []mass = Players[i].split("��");
			if(mass[0].equals("")) continue;
			playersInforsID[i] = mass[0];
			playersInforsName[i] = mass[1]; 
			playersInforsScore[i] = mass[3]; 
			playersPortrait[i] = mass[4]; 
			if(playersInforsName[i].equals(userName)){
				my_infor.setText("����:" + playersInforsScore[i]);
			}
			int nameLength = mass[1].getBytes().length;
			if(nameLength > 8){
				mass[1] = mass[1].substring(0,3) + "��";
				nameLength = mass[1].getBytes().length;
			}
			int idLength = mass[0].getBytes().length;
			if(idLength > 8){
				mass[0] = mass[0].substring(0,3) + "��";
				idLength = mass[0].getBytes().length;
			}
			String namePos = "";
			for(int j = 0;j < perLength - nameLength;j++){
				namePos += "  ";
			}
			String idPos = "";
			for(int j = 0;j < perLength - idLength;j++){
				idPos += "  ";
			}
			infor = "    ��� " + mass[1] + namePos + "ID " + mass[0] + idPos +"���� " + mass[3];
			playersInfors[i] = new JLabel(infor);
			//playersInfors[i].setBorder(BorderFactory.createLineBorder(Color.gray)); 
			showUsers.add(playersInfors[i]);
			playersInfors[i].setBounds(0,20 * i , 277,20);
			showUsers.append("\r\n");
			playersInfors[i].addMouseListener(this);
		}
		addGameSum();
	}

	public void addGameSum(){
		game_sum.setText(Players.length + "��������Ϸ��");
	}

	public void getBroadcastMessage(String type,String uId ,String uName,String userMessage)	
	{
		String text = "";
		int length = 7 + uName.length() + userMessage.length();
		if(type.equals("1")){
			if(userId.equals(uId)){
				text = "<html>"+addColor(">�����硿 ","red") + addColor("��","red") + addColor(" ˵: ","red") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,length);
			}
			else{
				text = "<html>"+addColor(">�����硿 ","red") + addColor(uName,"red") + addColor(" ˵: ","red") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,uId,uName,length);
			}
		}
		else if(type.equals("2")){
			if(userId.equals(uId)){
				text = "<html>"+addColor(">��˽�ġ� ","green") + addColor("��","green") + addColor(" ˵: ","green") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,length);
			}
			else{
				text = "<html>"+addColor(">��˽�ġ� ","green") + addColor(uName,"green") + addColor(" ˵: ","green") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,uId,uName,length);
			}
		}
	}

	public String addColor(String font , String color){
		//return "<font face = '�����п�' size = '4' color = " + color + ">" + font + "</font>";
		return "<font color = " + color + ">" + font + "</font>";
	}

	int chatLabelsum = 0;											//��¼ ÿ��������Ϣ�� ������ҵ���Ϣ

	int barLength = 257;		//������ʼ���� =

	public void addToChatLabel(String text ,String userId ,String userName, int length){	//������ҵ���Ϣ,���������Ϣ
		idNeedClear();
		addChatLabelInfor(text);

		chatLabelLineUserId[chatLabelsum] = userId;
		chatLabelLineUserName[chatLabelsum] = userName;
		chatLabelInfor[chatLabelsum].addMouseListener(this);

		addTakePosition(length);
		sumPlus();
	}

	public void addToChatLabel(String text , int length){			//ϵͳ��ʾ��Ϣ������Լ�����Ϣ,���豣�������Ϣ
		idNeedClear();
		addChatLabelInfor(text);
		addTakePosition(length);
		sumPlus();
	}	

	public void addToChatLabel(String text){						//ϵͳ��ʾ��Ϣ������Լ�����Ϣ,���豣�������Ϣ
		idNeedClear();
		addChatLabelInfor(text);
		chatLabel.append("\r\n");
		sumPlus();
	}

	public void idNeedClear(){
		if(chatLabelsum >= c.maxChatLabelsum){
			chatLabel.setText("");
			chatLabel.removeAll();
			chatLabelsum = 0;
		}
	}

	public void addChatLabelInfor(String text){
		chatLabelInfor[chatLabelsum] = new JLabel(text);
		chatLabelInfor[chatLabelsum].setBounds(0,18 * chatLabelsum , 2000,18);
		chatLabel.add(chatLabelInfor[chatLabelsum]);
	}

	public void addTakePosition(int length){
		String takePostion = "";
		for(int i = 0 ; i < (9 * length)/10 ;i++){
			takePostion += "     ";
		}
		chatLabel.append(takePostion + "\r\n");
	}

	public void sumPlus(){
		chatLabelsum ++;
		JScrollBar bar = chatLabelScroll.getVerticalScrollBar();
		bar.setValue(bar.getMaximum());
	}

	String formerRSSInfor = "";
	boolean immediateRefresh = false;

	public void immediateRefreshSeatState(){
		immediateRefresh = true;
		refreshSeatState(formerRSSInfor);
		immediateRefresh = false;
	}

	public void refreshSeatState(String infor){						//ˢ����λ��Ϸ���
		if((formerRSSInfor.equals(infor)&&!immediateRefresh)||infor.equals("")){
			return;
		}
		formerRSSInfor = infor;
		try{
			for(int i = 0;i < c.DeskNum; i++){
				for(int j = 0; j < 2 ;j++){
					cName[i * 2 + j].setText("");					//�����λ�ϵ�����
					ImageIcon image = new ImageIcon(c.ChairWaitImgPath);
					image = new ImageIcon(image.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));
					cButton[i * 2 + j].setIcon(image);				//��ԭ����ͼ��
					if(i%2==0){										//������λȷ������(ֻ�������λȷ��)
						ImageIcon image2 = new ImageIcon(c.TableWaitImgPath);
						image2 = new ImageIcon(image2.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));
						tButton[i / 2].setIcon(image2);				//��ԭ����ͼ��
					}
				}
			}
			if(infor.equals("none")) {								//û���������,���ؼ�������
				return;
			}
			String users[] = infor.split("��");
			for(int i = 0;i < users.length; i++){
				String infors[] = users[i].split("��");
				int tableNumber = Integer.parseInt(infors[0]);
				int chairNumber = Integer.parseInt(infors[1]);
				//String userId = infors[2];
				String userName = infors[3];
				String pictName = infors[4];
				boolean isStart = Boolean.parseBoolean(infors[5]);
				cName[tableNumber * 2 + chairNumber].setText("<html><font size = 3 color = 'white'>" + userName + "</font></html>");
				ImageIcon image = new ImageIcon(c.portrait_path + pictName);
				image = new ImageIcon(image.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));
				cButton[tableNumber * 2 + chairNumber].setIcon(image);			//��λ����,���϶�Ӧͷ��
				if(isStart){
					ImageIcon image2 = new ImageIcon(c.TableStartImgPath);
					image2 = new ImageIcon(image2.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));
					tButton[tableNumber].setIcon(image2);						//��Ϸ��ʼ,�������ӿ�ʼͼ��
				}
				else{
					ImageIcon image2 = new ImageIcon(c.TableWaitImgPath);
					image2 = new ImageIcon(image2.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));
					tButton[tableNumber].setIcon(image2);						//��Ϸδ��ʼ,�������ӵȴ�ͼ��
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public void showMyportrait(){
		try{
			FileReader filein = new FileReader("src/my_portrait.txt");
			BufferedReader br = new BufferedReader(filein);
			String temp = null;
			while((temp = br.readLine()) != null) {
				try{
					//System.out.println("�����ļ�����:" + temp);
					String dlls[] = temp.split(";");
					String dllIds[] = dlls[0].split("=");
					String dllId = dllIds[1].trim();
					if(dllId.equals(userId)){
						String imageNames[] = dlls[1].split("=");
						if(imageNames[0].trim().equals("imageName")){	
							imageName = imageNames[1].trim();
							System.out.println("�ҵ�ͷ����:" + imageName);
							break;
						}
					}
				}
				catch(Exception e){
					continue;
				}
			}
			c.sendMessage(clientChannel,"setMyPortrait;" + imageName);
			paintMyPortrait();
		}
		catch (Exception e2)
		{	System.out.println(e2);}
	}

	public void paintMyPortrait(){
		myPortraitImage = new ImageIcon(c.portrait_path + imageName);
		myPortraitImage = new ImageIcon(myPortraitImage.getImage().getScaledInstance(c.m(33),c.m(33),Image.SCALE_FAST));
		my_portrait = new JLabel(myPortraitImage);
		add(my_portrait);
		my_portrait.setBounds(c.m(120), c.m(8), c.m(25), c.m(25));	//�����ҵ�ͷ��
		my_portrait.addMouseListener(this);

		my_name = new JLabel(userName);
		add(my_name);
		my_name.setBounds(c.m(150), c.m(-10), c.m(50), c.m(50));	//�����ҵ�����
		my_infor = new JLabel("����:"+score);
		add(my_infor);
		my_infor.setBounds(c.m(150), c.m(0), c.m(50), c.m(50));		//�����ҵ���Ϣ
	}

	public void initMyImageName(){
		try{
			FileReader filein = new FileReader("src/dll.txt");
			BufferedReader br = new BufferedReader(filein);
			String temp = null;
			while((temp = br.readLine()) != null) {
				try{
					//System.out.println("�����ļ�����:"+temp);
					String dlls[] = temp.split(";");
					String dllIds[] = dlls[0].split("=");
					String dllId = dllIds[1].trim();
					if(dllId.equals(userId)){
						String imageNames[] = dlls[1].split("=");
						if(imageNames[0].trim().equals("imageName")){	
							myImageName = imageNames[1].trim();
							break;
						}
					}
				}
				catch(Exception e){
					continue;
				}
			}
			System.out.println("�ҵ�������:" + myImageName);
			setMyImageName(c.imagepath + myImageName);

			if(!getIsMyFileSended()){									//���״������ϴ�һ������	
				c.sendMessage(clientChannel,"ULMImg");					//upLoadMyImage
				sendImage();											//���������������
				c.sendMessage(clientChannel,"SMImgD");					//sendMyImageDone
				setIsMyFileSended(true);
			}
		}
		catch (Exception e)
		{	System.out.println(e);}
	}

	public void sendImage(){
		int n = 0;
		try{
			File file =new File(myImageName);
			if(file.length() > c.MaxImageLength){
				System.out.println("�ļ����ȹ���");//
				return;
			}
			FileInputStream  fr = new FileInputStream (file);
			byte[] b = new byte[c.ImageBufferSize];
			ByteBuffer sendbuffer; 
			while ((n = fr.read(b)) > 0) {	
				sendbuffer = ByteBuffer.wrap(b,0,n);
				clientChannel.write(sendbuffer);
				sendbuffer.flip();
				Thread.sleep(3);	//Thread.sleep(3);
			}
			fr.close();
		}
		catch(Exception sendMessageError){
			System.out.println(sendMessageError);
			System.out.println("���ݷ���ʧ��");
		}
	}

	public void addFriendRequest(String reqId,String reqName){
		int i = JOptionPane.showConfirmDialog(null, "ͬ�� " + reqName +"(" + reqId + ")" + " �����Ϊ����?", "ȷ�����", JOptionPane.YES_NO_OPTION);
		if(i == JOptionPane.YES_OPTION){
			c.sendMessage(clientChannel,"agreeAddFriend;" + reqId);
		}
		else{
			c.sendMessage(clientChannel,"refuseAddFriend;" + reqId);
		}
	}
} 