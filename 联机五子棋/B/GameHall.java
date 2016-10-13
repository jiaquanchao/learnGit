/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
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
	private SocketChannel clientChannel;					//服务器信息
	private JLabel game_logo;								//游戏图标显示
	private JLabel game_name;								//游戏名称显示
	private JLabel game_sum;								//游戏总人数
	private JLabel my_portrait;								//用户头像显示
	private JLabel my_name;									//用户姓名显示
	private JLabel my_infor;								//用户分数显示
	private JLabel tButton[];
	private JLabel cButton[];
	private JLabel cName[];
	private JLabel desknum[];								//显示桌子编号
	private String imageName = "default.PNG";				//头像名
	private String myImageName = "default.PNG";				//形象名
	private boolean isMyFileSended = false;
	private boolean isSitted = false ; 
	private boolean isWaiting = true;						//大厅启动中,禁止操作
	private boolean waitForReply = false ; 
	private String userId;									//用户信息
	private String userName;
	private String score;
	private JTextArea showUsers = new JTextArea("");
	private JScrollPane	showUsersScroll = new JScrollPane(showUsers);
	private JTextArea chatLabel = new JTextArea("");
	private JScrollPane	chatLabelScroll = new JScrollPane(chatLabel);
	private JTextField messageField = new JTextField();
	private JButton viewFriends = new JButton("好友");
	private JButton chatControl = new JButton("公共");
	private JButton sendMessage = new JButton("发送");
	private JButton clearMessage = new JButton("清空");
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
			addToChatLabel("系统提示: 您的好友 " + userName + " 不在线");
			return;
		}
		aimedUserId = userId;
		aimedUserName = userName;
		chatControl.setMargin(new Insets(0,0,0,0));					//与其他玩家聊天
		chatControl.setText("私聊:" + aimedUserName);
		confirmChat = true;
	}

	public void delFriend(String userId,String userName){
		c.sendMessage(clientChannel,"delFriend" + ";" + userId + ";" + userName);
	}

	GameHall(Constant cc)											//游戏大厅
	{
		super("游戏大厅");											//设置标题
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
		addToChatLabel("系统提示: 欢迎光临游戏大厅");
	}

	public void init(){
		if(c.wsizex > screenSize.width + 20){						//screenSize.width非实际屏幕尺寸,是分辨率
			System.out.println("窗口过大");
			newMultiple("fit");										//大厅自适应窗口大小
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
			this.setIconImage(image);								//设置图标
		}
		catch (Exception e) {
			System.out.println(e);
		}
		background = new ImageIcon(c.SysImgpath + "bg3.jpg");
		background = new ImageIcon(background.getImage().getScaledInstance(c.wsizex,c.m(75),Image.SCALE_FAST));	//通过Image缩小ImageIcon
		bgLabel3 = new JLabel(background);
		bgLabel3.setBounds(c.m(0),c.m(-35),c.wsizex,c.m(75));
		this.getLayeredPane().add(bgLabel3, new Integer(Integer.MIN_VALUE));
		((JPanel)getContentPane()).setOpaque(false);

		imageLogo = new ImageIcon(c.SysImgpath + "logo.png");
		imageLogo = new ImageIcon(imageLogo.getImage().getScaledInstance(c.m(33),c.m(33),Image.SCALE_FAST));	//通过Image缩小ImageIcon
		game_logo = new JLabel(imageLogo);
		add(game_logo);
		game_logo.setBounds(c.m(-5), c.m(-5), c.m(50), c.m(50));	//放置游戏图标

		game_name = new JLabel("<html><font size = 4><b>五子棋</b></font></html>");
		add(game_name);
		game_name.setBounds(c.m(42), c.m(-13), c.m(50), c.m(50));	//放置游戏名称

		game_sum = new JLabel("");
		add(game_sum);
		game_sum.setBounds(c.m(42), c.m(0), c.m(50), c.m(50));		//放置游戏总人数
		if(Players != null){
			addGameSum();
		}

		tableWaitImage = new ImageIcon(c.TableWaitImgPath);
		tableWaitImage = new ImageIcon(tableWaitImage.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));	//通过Image缩小ImageIcon

		for(int i=0 ; i < tButton.length ; i++){					//实例化桌子
			tButton[i] = new JLabel(tableWaitImage);
		}

		chairWaitImage = new ImageIcon(c.ChairWaitImgPath);
		chairWaitImage = new ImageIcon(chairWaitImage.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));	//通过Image缩小ImageIcon
		for(int i=0 ; i< cButton.length ; i++){						//实例化椅子
			cButton[i] = new JLabel(chairWaitImage);
			cName[i] = new JLabel("");
		}

		setLayout(null);
		jPanel = new JPanel(null);
		jPanel.setOpaque(false);
		
		for(int i=0 ; i < tButton.length ; i++){
			int x = ( i % c.perLineDesks ) + 1 ;
			int y = ( i / c.perLineDesks ) + 1;
			jPanel.add(tButton[i]);									//放置桌子
			tButton[i].setBounds(c.m(100) * x - c.m(60), c.m(75) * y - c.m(60), c.m(30), c.m(30));			//放置桌子

			desknum[i] = new JLabel("<html><font size = 3 color = 'white'>- "+ (i+1) + " -</font></html>");
			jPanel.add(desknum[i]);
			desknum[i].setBounds(c.m(100) * x - c.m(52), c.m(75) * y - c.m(40), c.m(40), c.m(40));			//放置桌子编号("- i -")

			jPanel.add(cButton[2 * i]);								//放置椅子
			jPanel.add(cButton[2 * i + 1]);							//放置椅子
			cButton[2 * i].setBounds(c.m(100) * x - c.m(85), c.m(75) * y - c.m(55), c.m(20), c.m(20));		//放置椅子
			cButton[2 * i + 1].setBounds(c.m(100) * x - c.m(25), c.m(75) * y - c.m(55), c.m(20), c.m(20));	//放置椅子

			jPanel.add(cName[2 * i]);								//放置椅子对应姓名
			jPanel.add(cName[2 * i + 1]);							//放置椅子对应姓名
			cName[2 * i].setBounds(c.m(100) * x - c.m(80), c.m(75) * y - c.m(50), c.m(50), c.m(50));		//放置桌子对应姓名
			cName[2 * i + 1].setBounds(c.m(100) * x - c.m(20), c.m(75) * y - c.m(50), c.m(50), c.m(50));	//放置桌子对应姓名
					
			tButton[i].addMouseListener(this);						//第二次初始化已经remove,需重新加入Listener
			cButton[2 * i].addMouseListener(this);
			cButton[2 * i + 1].addMouseListener(this);
		}
		int rownum = ((c.DeskNum + c.perLineDesks - 1)/ c.perLineDesks);
		jPanel.setPreferredSize(new Dimension (c.m(330), c.m(80) * rownum));	//桌子滚动条显示大小(动窗口)
		deskPanel.getViewport().add(jPanel);									//注意和下面语句的顺序
		deskPanel.getViewport().setBackground(new Color(94,131,173));			//注意和上面语句的顺序
		add(deskPanel);
		
		deskPanel.setBounds(c.m(0), c.m(60), c.nowDeskPanelLength, c.m(288));					//桌子滚动界面位置及大小(定窗口)//注意c.x()的位置
		deskPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		showUsersScroll.setBounds(c.extendL(330) + c.dev_x, c.m(60), c.m(140), c.m(100));		//"用户显示框"
		add(showUsersScroll);
		showUsers.setOpaque(true);
		showUsers.setBackground(c.chatColor);

		chatLabelScroll.setBounds(c.extendL(330) + c.dev_x, c.m(170), c.m(140), c.m(130));		//"聊天显示框"
		add(chatLabelScroll);
		chatLabel.setOpaque(true);
		chatLabel.setBackground(c.chatColor);

		add(messageField);
		messageField.setBounds(c.extendL(360) + c.dev_x, c.m(300), c.m(110), c.m(15));			//"信息输入框"
		
		messageField.setBorder (BorderFactory.createLineBorder(Color.gray,1));
		messageField.setOpaque(true);
		messageField.setBackground(c.chatColor);

		add(viewFriends);
		viewFriends.setBounds(c.extendL(430) + c.dev_x, c.m(10), c.m(40), c.m(15));				//"查看好友"
		
		add(chatControl);
		chatControl.setBounds(c.extendL(330) + c.dev_x, c.m(300), c.m(30), c.m(15));			//"聊天控制"
		
		add(sendMessage);
		sendMessage.setBounds(c.extendL(330) + c.dev_x, c.m(320), c.m(40), c.m(15));			//"发送"

		add(clearMessage);
		clearMessage.setBounds(c.extendL(430) + c.dev_x, c.m(320), c.m(40), c.m(15));			//"清空"
		
		showUsers.setEditable(false); 
		chatLabel.setEditable(false);

		VUChat.setLabel("                私聊                ");
		VUAddFriend.setLabel("            添加好友             ");
		
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
						addToChatLabel("系统提示: 您有未结束的游戏");
					}
				}
			});
			
			this.addWindowStateListener(new WindowStateListener () {
				public void windowStateChanged(WindowEvent state) {
					if(state.getNewState() == 0) {					//"窗口还原"方法
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
							if(c.debug){	System.out.println("窗口还原");}
							c = new Constant(c.initMutiple);
							init();
						}
					}
					else if(state.getNewState() == 6) {				//"窗口最大化"方法
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
					if(c.debug){	System.out.println("窗口最大化");} 
					c = new Constant(c.initMutiple,newMultiple ,nowX ,nowY ,c.wsizex ,c.wsizey, false, c.nowDeskPanelLength);
				}
				else{
					if(c.debug){	System.out.println("窗口自适应");} 
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
		if(c.debug){	System.out.println("触发Action");}
		if (e.getSource() == sendMessage || e.getSource() == messageField) {		//点击发送 或 按回车键
			if(aimedUserId.equals("")||confirmChat == false){
				String text = messageField.getText();
				if(text.equals(""))	return;
				String newcontent = "userBroadcastMessage;" + text;	//发起 通话
				c.sendMessage(clientChannel,newcontent);
				messageField.setText("");
			}
			else{
				String text = messageField.getText();
				if(text.equals(""))	return;
				String newcontent = "userSeparateChatMessage;" + aimedUserId + ";" + text;		//发起 通话
				c.sendMessage(clientChannel,newcontent);
				messageField.setText("");
			}
		}
		else if (e.getSource() == clearMessage) {					//情况消息记录
			String text = chatLabel.getText();
			if(text.equals(""))	return;
			chatLabel.setText("");
			chatLabel.removeAll();
			chatLabelsum = 0;
		}
		else if (e.getSource() == viewFriends) {					//查看好友
			c.sendMessage(clientChannel,"viewFriends;");
		}
		else if (e.getSource() == VUChat){							//与某确定玩家聊天
			if(aimedUserId.equals(userId)){							//与自己聊天,是不允许的
				aimedUserId = "";
				aimedUserName = "";
				addToChatLabel("系统提示: 不可与自己私聊");
				chatControl.setText("公共");
				confirmChat = false;
				return;
			}
			if(!aimedUserId.equals("")){
				chatControl.setText("私聊:" + aimedUserName);		//与其他玩家聊天
				confirmChat = true;	
			}
		}
		else if (e.getSource() == VUAddFriend){						//添加某确定玩家为好友
			if(aimedUserId.equals(userId)){
				aimedUserId = "";
				aimedUserName = "";
				addToChatLabel("系统提示: 不可添加自己为好友");
				confirmAF = false;
				return;
			}
			if(!aimedUserId.equals("")){
				int i = JOptionPane.showConfirmDialog(null, "添加 " + aimedUserName + " 为好友？", "确认添加", JOptionPane.YES_NO_OPTION);
				if(i == JOptionPane.YES_OPTION){
					c.sendMessage(clientChannel,"addFriendRequest;" + aimedUserId);
				}
			}
			confirmAF = false;
		}
		else if (e.getSource() == chatControl) {
			if(!chatControl.getText().equals("公共")){				//返回公共频道,记录上一次合法私聊玩家
				if(!aimedUserId.equals("")&&confirmChat){			//存在上一次聊天玩家且已确定聊天
					formerChatToUserId = aimedUserId;				//记录上一次合法私聊玩家
					formerChatToUserName = aimedUserName;
				}
				aimedUserId = "";									//返回公共频道,并置空
				aimedUserName = "";
				chatControl.setText("公共");
				confirmChat = false;
			}
			else{													//回写上次通话玩家
				if(!formerChatToUserId.equals("")){
					aimedUserId = formerChatToUserId;
					aimedUserName = formerChatToUserName;
					chatControl.setMargin(new Insets(0,0,0,0)); 
					chatControl.setText("私聊:" + aimedUserName);
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
	public void mouseClicked(MouseEvent e)							//由指针位置确定棋盘位置
	{
		if(c.debug){	System.out.println("触发mouseClicked");}
		if(e.getModifiers() == 4){
			for(int i = 0 ; i < playersInfors.length ; i++){
				if (e.getSource() == playersInfors[i]){				//由 用户显示框 打开玩家 右键菜单
					viewUserPM.show(playersInfors[i],e.getX(),e.getY());
					aimedUserId = playersInforsID[i];
					aimedUserName = playersInforsName[i];
					//chatToUserPortrait = playersPortrait[i];
					break;
				}
			}

			for(int i = 0 ; i < chatLabelInfor.length ; i++){
				if (e.getSource() == chatLabelInfor[i]){			//由 聊天记录框 打开玩家 右键菜单
					viewUserPM.show(chatLabelInfor[i],e.getX(),e.getY());
					aimedUserId = chatLabelLineUserId[i];
					aimedUserName = chatLabelLineUserName[i];
					break;
				}
			}
		}

		if(e.getModifiers() == 16) {								//只接收左击事件
			if(isWaiting) {
				addToChatLabel("系统提示: 等待系统初始化");
				return;
			}
			if(waitForReply){
				addToChatLabel("系统提示: 等待服务器响应");
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
						addToChatLabel("系统提示: 您已经打开了游戏");
					}
					break;
				}
			}
			
			for(int i = 0 ; i < playersInfors.length ; i++){
				if (e.getSource() == playersInfors[i]){				//查看玩家信息
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
			
			if (e.getSource() == my_portrait){						//修改头像
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
							File txt = new File("src/my_portrait.txt");				//向配置文件写入头像信息,新信息顶置
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
								FileOutputStream out = new FileOutputStream(txt,false);		//重写
								out.write(contents);
								out.close();
							}
							catch (IOException ee)
							{	System.out.println(ee);}
							addToChatLabel("系统提示: 头像修改成功,重启游戏生效");
						}
						else{
							System.out.println("不支持该文件类型！");
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
		super.paintComponents(g);									// 绘制此容器中的每个组件
	} 

	JLabel playersInfors[] = new JLabel[10];
	String playersInforsID[] = new String[10];
	String playersInforsName[] = new String[10];
	String playersInforsScore[] = new String[10];
	String playersPortrait[] = new String[10];
	String Players[];

	public void refreshGameHallPlayers(String Player)				//刷新大厅玩家信息
	{
		//格式："userId , usreName , color , score"
		Players = Player.split("↑");
		String infor = "";
		showUsers.setText("");
		showUsers.removeAll();
		int perLength = 10;
		for(int i = 0 ; i < Players.length ; i++){
			String []mass = Players[i].split("↓");
			if(mass[0].equals("")) continue;
			playersInforsID[i] = mass[0];
			playersInforsName[i] = mass[1]; 
			playersInforsScore[i] = mass[3]; 
			playersPortrait[i] = mass[4]; 
			if(playersInforsName[i].equals(userName)){
				my_infor.setText("积分:" + playersInforsScore[i]);
			}
			int nameLength = mass[1].getBytes().length;
			if(nameLength > 8){
				mass[1] = mass[1].substring(0,3) + "…";
				nameLength = mass[1].getBytes().length;
			}
			int idLength = mass[0].getBytes().length;
			if(idLength > 8){
				mass[0] = mass[0].substring(0,3) + "…";
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
			infor = "    玩家 " + mass[1] + namePos + "ID " + mass[0] + idPos +"分数 " + mass[3];
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
		game_sum.setText(Players.length + "人正在游戏中");
	}

	public void getBroadcastMessage(String type,String uId ,String uName,String userMessage)	
	{
		String text = "";
		int length = 7 + uName.length() + userMessage.length();
		if(type.equals("1")){
			if(userId.equals(uId)){
				text = "<html>"+addColor(">【世界】 ","red") + addColor("你","red") + addColor(" 说: ","red") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,length);
			}
			else{
				text = "<html>"+addColor(">【世界】 ","red") + addColor(uName,"red") + addColor(" 说: ","red") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,uId,uName,length);
			}
		}
		else if(type.equals("2")){
			if(userId.equals(uId)){
				text = "<html>"+addColor(">【私聊】 ","green") + addColor("你","green") + addColor(" 说: ","green") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,length);
			}
			else{
				text = "<html>"+addColor(">【私聊】 ","green") + addColor(uName,"green") + addColor(" 说: ","green") + addColor(userMessage, "black")+"</html>";
				addToChatLabel(text,uId,uName,length);
			}
		}
	}

	public String addColor(String font , String color){
		//return "<font face = '华文行楷' size = '4' color = " + color + ">" + font + "</font>";
		return "<font color = " + color + ">" + font + "</font>";
	}

	int chatLabelsum = 0;											//记录 每条聊天信息的 发起玩家的信息

	int barLength = 257;		//聊天框初始长度 =

	public void addToChatLabel(String text ,String userId ,String userName, int length){	//其他玩家的消息,保存玩家信息
		idNeedClear();
		addChatLabelInfor(text);

		chatLabelLineUserId[chatLabelsum] = userId;
		chatLabelLineUserName[chatLabelsum] = userName;
		chatLabelInfor[chatLabelsum].addMouseListener(this);

		addTakePosition(length);
		sumPlus();
	}

	public void addToChatLabel(String text , int length){			//系统提示消息和玩家自己的消息,无需保存玩家信息
		idNeedClear();
		addChatLabelInfor(text);
		addTakePosition(length);
		sumPlus();
	}	

	public void addToChatLabel(String text){						//系统提示消息和玩家自己的消息,无需保存玩家信息
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

	public void refreshSeatState(String infor){						//刷新座位游戏情况
		if((formerRSSInfor.equals(infor)&&!immediateRefresh)||infor.equals("")){
			return;
		}
		formerRSSInfor = infor;
		try{
			for(int i = 0;i < c.DeskNum; i++){
				for(int j = 0; j < 2 ;j++){
					cName[i * 2 + j].setText("");					//清空座位上的姓名
					ImageIcon image = new ImageIcon(c.ChairWaitImgPath);
					image = new ImageIcon(image.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));
					cButton[i * 2 + j].setIcon(image);				//还原椅子图标
					if(i%2==0){										//根据座位确定桌子(只由左侧座位确定)
						ImageIcon image2 = new ImageIcon(c.TableWaitImgPath);
						image2 = new ImageIcon(image2.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));
						tButton[i / 2].setIcon(image2);				//还原桌子图标
					}
				}
			}
			if(infor.equals("none")) {								//没有玩家坐下,不必继续更新
				return;
			}
			String users[] = infor.split("↑");
			for(int i = 0;i < users.length; i++){
				String infors[] = users[i].split("↓");
				int tableNumber = Integer.parseInt(infors[0]);
				int chairNumber = Integer.parseInt(infors[1]);
				//String userId = infors[2];
				String userName = infors[3];
				String pictName = infors[4];
				boolean isStart = Boolean.parseBoolean(infors[5]);
				cName[tableNumber * 2 + chairNumber].setText("<html><font size = 3 color = 'white'>" + userName + "</font></html>");
				ImageIcon image = new ImageIcon(c.portrait_path + pictName);
				image = new ImageIcon(image.getImage().getScaledInstance(c.m(20),c.m(20),Image.SCALE_FAST));
				cButton[tableNumber * 2 + chairNumber].setIcon(image);			//座位有人,换上对应头像
				if(isStart){
					ImageIcon image2 = new ImageIcon(c.TableStartImgPath);
					image2 = new ImageIcon(image2.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));
					tButton[tableNumber].setIcon(image2);						//游戏开始,换上桌子开始图标
				}
				else{
					ImageIcon image2 = new ImageIcon(c.TableWaitImgPath);
					image2 = new ImageIcon(image2.getImage().getScaledInstance(c.m(30),c.m(30),Image.SCALE_FAST));
					tButton[tableNumber].setIcon(image2);						//游戏未开始,换上桌子等待图标
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
					//System.out.println("配置文件内容:" + temp);
					String dlls[] = temp.split(";");
					String dllIds[] = dlls[0].split("=");
					String dllId = dllIds[1].trim();
					if(dllId.equals(userId)){
						String imageNames[] = dlls[1].split("=");
						if(imageNames[0].trim().equals("imageName")){	
							imageName = imageNames[1].trim();
							System.out.println("我的头像名:" + imageName);
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
		my_portrait.setBounds(c.m(120), c.m(8), c.m(25), c.m(25));	//放置我的头像
		my_portrait.addMouseListener(this);

		my_name = new JLabel(userName);
		add(my_name);
		my_name.setBounds(c.m(150), c.m(-10), c.m(50), c.m(50));	//放置我的名字
		my_infor = new JLabel("积分:"+score);
		add(my_infor);
		my_infor.setBounds(c.m(150), c.m(0), c.m(50), c.m(50));		//放置我的信息
	}

	public void initMyImageName(){
		try{
			FileReader filein = new FileReader("src/dll.txt");
			BufferedReader br = new BufferedReader(filein);
			String temp = null;
			while((temp = br.readLine()) != null) {
				try{
					//System.out.println("配置文件内容:"+temp);
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
			System.out.println("我的形象名:" + myImageName);
			setMyImageName(c.imagepath + myImageName);

			if(!getIsMyFileSended()){									//仅首次坐下上传一次形象	
				c.sendMessage(clientChannel,"ULMImg");					//upLoadMyImage
				sendImage();											//向服务器传送形象
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
				System.out.println("文件长度过长");//
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
			System.out.println("数据发送失败");
		}
	}

	public void addFriendRequest(String reqId,String reqName){
		int i = JOptionPane.showConfirmDialog(null, "同意 " + reqName +"(" + reqId + ")" + " 添加您为好友?", "确认添加", JOptionPane.YES_NO_OPTION);
		if(i == JOptionPane.YES_OPTION){
			c.sendMessage(clientChannel,"agreeAddFriend;" + reqId);
		}
		else{
			c.sendMessage(clientChannel,"refuseAddFriend;" + reqId);
		}
	}
} 