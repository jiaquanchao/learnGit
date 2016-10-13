/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;

class Wuziqi extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
	private Constant c;
	private StepRecord stepRecords[] = new StepRecord[255];			//记录玩家所有落子信息
	private SocketChannel clientChannel;							//服务器信息
	private int tableNumber = -1;									//玩家所在桌号
	private String userId;											//玩家账号
	private String userName;										//玩家昵称
	private int score;												//玩家分数
	private int user_color = -1;									// 用户所持棋子的颜色标识 0:白子 1:黑子
	private int nowColor = -1;										//当前走棋者颜色
	private int step = 0;											//当前步数
	private boolean isStart = false;								//游戏开始标志
	private String thisId;
	private String thisName;
	private String thisColor;
	private byte myImageBytes [];
	private String oppoId;
	private String oppoName;
	private String oppoColor;
	private byte oppoImageByte[];									//保存对家形象图片
	boolean isHaveOppoImage = false;								//是否已有对家形象图片
	private String firstStartColor;									//先手标记,用户保存布局,确定先手
	private boolean waitForReply = false ;							//图片下载过程,锁定操作
	private boolean waitForRetractReply = false;					//悔棋等待,不能一次点多次,可以分次悔棋
	private boolean waitForDrawReply = false;						//和棋等待,不能一次点多次
	private String myImageName = "default.PNG";						//头像默认值
	private String imageName = "default.PNG";						//形象默认值
	public JButton startGame = new JButton("开始游戏");
	public JButton admitLose = new JButton("认输");
	public JButton retract = new JButton("悔棋");
	public JButton draw  = new JButton("求和");
	private JButton review  = new JButton("调入复盘");
	private JButton setImage  = new JButton("修改形象");
	private JTextArea showUsers = new JTextArea("");
	private JScrollPane	showUsersScroll = new JScrollPane(showUsers);
	private JTextArea chatLabel = new JTextArea("");
	private JScrollPane	chatLabelScroll = new JScrollPane(chatLabel);
	private JTextField messageField = new JTextField();
	private JButton sendMessage = new JButton("发送");
	private JButton clearMessage = new JButton("清空");
	public JLabel lblWin = new JLabel(" ");
	public JLabel oppsInfor = new JLabel(" ");
	public JLabel myInfor = new JLabel(" ");
	private Image myImage;
	public Image oppoImage;
	private BufferedImage oppoBufferedImage;
	
	public void setTableNumber(int tNumber)
	{	tableNumber = tNumber;	}
	public int getTableNumber()
	{	return tableNumber;	}

	public void setWaitForRetractReply(boolean wfrr){
		waitForRetractReply = wfrr;
	}

	public void setWaitForDrawReply(boolean wfdr){
		waitForDrawReply = wfdr;
	}

	public void setMyImageName(String myIName)
	{	myImageName = myIName;	}

	public void setOppoImageByte(byte[] oImage){
		oppoImageByte = oImage;
	}

	public void setWaitForReply(boolean wtfp)
	{	waitForReply = wtfp;}

	public void setScore(int Score)
	{	score = Score;	}
	public int getScore()
	{	return score;	}

	public void setIsStart(boolean start)
	{	isStart = start;	}
	public boolean getIsStart()
	{	return isStart;	}

	public void setIsHaveOppoImage(boolean isHOI)
	{	isHaveOppoImage = isHOI;	}

	public boolean getIsHaveOppoImage()
	{	return isHaveOppoImage;	}

	public void setChannel(SocketChannel socketChannel)
	{	clientChannel = socketChannel;	}
	public SocketChannel getChannel()
	{	return clientChannel;	}

	public void setUser(String uId,String uName,int sc){
		userId = uId;
		userName = uName;
		score = sc;
	}
	public void setColor(int uColor)
	{	user_color = uColor;}

	Wuziqi(Constant cc){
		super("五子棋");											//设置标题
		c = cc;
		try {
			String src = c.SysImgpath + "default.png";		
			Image image=ImageIO.read(this.getClass().getResource(src));
			this.setIconImage(image);								//设置图标
		} 
		catch (Exception e) {
			System.out.println(e);
		}  
	}

	public void init()												//画游戏界面
	{
		for(int i = 0; i < stepRecords.length ; i++)
		{	stepRecords[i] = new StepRecord();	}

		ImageIcon img = new ImageIcon(c.SysImgpath + "bg2.jpg");
		JLabel bgLabel = new JLabel(img);
		bgLabel.setBounds(c.m(0), c.m(-8), c.m(512), c.m(425));
		this.getLayeredPane().add(bgLabel, new Integer(Integer.MIN_VALUE));
		((JPanel)getContentPane()).setOpaque(false);
		
		setLayout(null);
		addMouseListener(this);		
		setResizable(false);

		add(oppsInfor);
		oppsInfor.setBounds(c.m(0) + c.dev_x, c.m(20), c.m(70), c.m(30));			//"提示对家的信息"
		
		add(setImage);
		setImage.setBounds(c.m(3) + c.dev_x, c.m(320), c.m(45), c.m(20));			//"修改头像"
		setImage.addActionListener(this);

		add(startGame);
		startGame.setBounds(c.m(70) + c.dev_x, c.m(320), c.m(45), c.m(20));			//"游戏开始"
		startGame.addActionListener(this);

		add(admitLose);
		admitLose.setBounds(c.m(150) + c.dev_x, c.m(320), c.m(45), c.m(20));		//"认输"
		admitLose.addActionListener(this);
		admitLose.setEnabled(false);

		add(retract);
		retract.setBounds(c.m(230) + c.dev_x, c.m(320), c.m(45), c.m(20));			//"悔棋"
		retract.addActionListener(this);
		retract.setEnabled(false);

		add(draw);
		draw.setBounds(c.m(310) + c.dev_x, c.m(320), c.m(45), c.m(20));				//"求和"
		draw.addActionListener(this);
		draw.setEnabled(false);

		add(review);
		review.setBounds(c.m(390) + c.dev_x, c.m(320), c.m(45), c.m(20));			//"调入复盘"
		review.addActionListener(this);

		showUsersScroll.setBounds(c.m(380) + c.dev_x, c.m(30), c.m(110), c.m(70));	//"用户显示框"
		add(showUsersScroll);
		showUsers.setOpaque(true);
		showUsers.setBackground(c.chatColor);
		showUsers.setEditable(false);

		chatLabelScroll.setBounds(c.m(380) + c.dev_x, c.m(120), c.m(110), c.m(150));//"聊天显示框"
		add(chatLabelScroll);
		chatLabel.setOpaque(true);
		chatLabel.setBackground(c.chatColor);
		chatLabel.setEditable(false); 

		add(messageField);
		messageField.setBounds(c.m(380) + c.dev_x, c.m(270), c.m(110), c.m(15));	//"信息输入框"
		messageField.addActionListener(this);
		messageField.setBorder (BorderFactory.createLineBorder(Color.gray,1));
		messageField.setOpaque(true);
		messageField.setBackground(c.chatColor);

		add(sendMessage);
		sendMessage.setBounds(c.m(380) + c.dev_x, c.m(290), c.m(40), c.m(15));		//"发送"
		sendMessage.addActionListener(this);
		
		add(clearMessage);
		clearMessage.setBounds(c.m(450) + c.dev_x, c.m(290), c.m(40), c.m(15));		//"清空"
		clearMessage.addActionListener(this);

		add(lblWin);
		lblWin.setBounds(c.m(0) + c.dev_x, c.m(160), c.m(70), c.m(30));				//"提示"

		add(myInfor);
		myInfor.setBounds(c.m(0) + c.dev_x, c.m(220), c.m(70), c.m(30));			//"提示我的信息"

		startGame.setMargin(new Insets(0,0,0,0));
		admitLose.setMargin(new Insets(0,0,0,0));
		retract.setMargin(new Insets(0,0,0,0));
		draw.setMargin(new Insets(0,0,0,0));
		review.setMargin(new Insets(0,0,0,0));
		sendMessage.setMargin(new Insets(0,0,0,0));
		clearMessage.setMargin(new Insets(0,0,0,0));
		setImage.setMargin(new Insets(0,0,0,0));
		
		ImageIcon icon = new ImageIcon(myImageName);
		icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(),icon.getIconHeight(), Image.SCALE_DEFAULT));
		myImage = icon.getImage();									//读取我的形象

		this.setSize(c.wsizex,c.wsizey);
		this.setLocationRelativeTo(null);

		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){					//关闭游戏界面,并告知服务器,方可打开新的游戏界面
			public void windowClosing(WindowEvent e){
				if(isStart && user_color >= 0){
					int i = JOptionPane.showConfirmDialog(null, "强制退出游戏？", "退出游戏", JOptionPane.YES_NO_OPTION);
					if(i == JOptionPane.YES_OPTION){
						String newcontent = "closeTable;" + getTableNumber();
						c.sendMessage(clientChannel,newcontent);
						dispose();
					}
				}
				else{
					String newcontent = "closeTable;" + getTableNumber();
					c.sendMessage(clientChannel,newcontent);
					dispose();
				}
			}
		});
		addToChatLabel("系统提示: 您进入" + (tableNumber + 1) + "号桌");	//与实际数组差1
	}

	public void initOppoImage(){									//显示对家形象
		ImageIcon icon = new ImageIcon(oppoImageByte);
		
		icon.setImage(icon.getImage().getScaledInstance(icon.getIconWidth(),
		icon.getIconHeight(), Image.SCALE_DEFAULT));
		oppoImage = icon.getImage();
		isHaveOppoImage = true;
		paint(this.getGraphics());									//游戏界面重绘以显示图片

		System.out.println(icon.getIconHeight() + "," + icon.getIconWidth());
		c.sendMessage(clientChannel,"initOppoPictOk;" + tableNumber);
	}

	public void initGame(String result)								//旁观者进入游戏,初始化(同步)落子情况
	{
		isStart = false;
		clearRecords();
		startGame.setEnabled(false);
		String []serverInfor = result.split(";");
		int initStep = Integer.parseInt(serverInfor[3]);
		for(int i = 4;i < initStep + 4; i++)
		{
			String []locate =serverInfor[i].split(",");
			int initi = Integer.parseInt(locate[0]);
			int initj = Integer.parseInt(locate[1]);
			int initColor = Integer.parseInt(locate[2]);
			stepRecords[i - 4].setStepRecord(initi,initj,initColor);
		}
		step = initStep;
		paint(this.getGraphics());
	}

	public void clearRecords()										//清空所有落子信息
	{
		for(int i = 0; i < stepRecords.length ; i++)
		{	stepRecords[i].setStepRecord(-1,-1,-1);	}
	}

	public void start(String tcolor)								//游戏正式开始
	{
		if(isStart){												//重复发送,不必响应(特殊处理)
			return;
		}
		firstStartColor = tcolor;
		String ucolor = startColor(Integer.parseInt(tcolor));
		lblWin.setText("游戏开始," + ucolor + "先手");
		step = 0;
		isStart = true;
		startGame.setEnabled(false);
		admitLose.setEnabled(false);
		retract.setEnabled(true);
		draw.setEnabled(true);
		addToChatLabel("游戏提示: 游戏开始");
		if(user_color == -2){										//旁观玩家
			startGame.setEnabled(false);
			admitLose.setEnabled(false);
			retract.setEnabled(false);
			draw.setEnabled(false);
			clearRecords();
			SetWinLineRecord("-1","-1","-1","-1");
			repaint();												//棋盘重置
		}
	}

	private int winLineX = -1;
	private int winLineY = -1;
	private int winLineDirection = -1;
	private int winLinePlusSum = -1;

	public void SetWinLineRecord(String sx,String sy,String sdirec,String splus){					//设置赢棋提示红线,置空或赋值
		winLineX = Integer.parseInt(sx);
		winLineY = Integer.parseInt(sy);
		winLineDirection = Integer.parseInt(sdirec);
		winLinePlusSum = Integer.parseInt(splus);
	}

	public void drawWinLine(){										//画出赢棋 5子 提示红线

		if(winLineDirection < 0) return;

		int wx = c.WLen * winLineY;
		int wy = c.WLen * winLineX;									//warning:坐标是反的
		int lineLength = winLinePlusSum * c.WLen;
		int opplineLength = (4 - winLinePlusSum) * c.WLen;

		Graphics graphics = this.getGraphics();
		Graphics2D g2D = (Graphics2D)graphics;
		float lineWidth = 3.0f;
		g2D.setStroke(new BasicStroke(lineWidth));

		g2D.setColor(Color.red);

		if(winLineDirection == 1){									
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x + lineLength ,  wx +c.dev_y, wy + c.dev_x);
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x - opplineLength ,  wx +c.dev_y, wy + c.dev_x);
		}

		if(winLineDirection == 2){
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y + lineLength , wy + c.dev_x);
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y - opplineLength , wy + c.dev_x);
		}

		if(winLineDirection == 3){
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y - lineLength , wy + c.dev_x + lineLength);
			g2D.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y + opplineLength, wy + c.dev_x - opplineLength);
		}

		if(winLineDirection == 4){
			g2D.drawLine( wx + c.dev_y, wy + c.dev_x,  wx + c.dev_y + lineLength, wy + c.dev_x + lineLength);
			g2D.drawLine( wx + c.dev_y, wy + c.dev_x,  wx + c.dev_y - opplineLength, wy + c.dev_x - opplineLength);
		}
	}

	public void gameEnd()											//游戏结束处理
	{
		startGame.setEnabled(true);
		admitLose.setEnabled(false);
		retract.setEnabled(false);
		draw.setEnabled(false);
		if(user_color == -2)										//旁观玩家
		{	startGame.setText("继续观看");}
		if(!isStart) return;										//游戏未开始
		isStart = false;
		if(step > 0) {
			int j = JOptionPane.showConfirmDialog(null, "是否保存棋谱？", "保存", JOptionPane.YES_NO_OPTION);
			if(j == JOptionPane.YES_OPTION){
				String saveMessage = "";
				for(int i = 0;i < step;i++){
					saveMessage += stepRecords[i].getX() + "," + stepRecords[i].getY() + ","+stepRecords[i].getColor() + ";";
				}
				Date now = new Date(); 
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd HH时mm分ss秒");
				String nowtime = dateFormat.format(now);
				File newfile = new File("..\\棋谱\\" + thisName + " vs " + oppoName + " " + nowtime + ".txt");
				byte []gameRcords = (firstStartColor + ";" + thisId + "," + thisName + "," + thisColor + ";" + oppoId + "," + oppoName + "," + oppoColor + ";" + step + ";" +winLineDirection + ";" + winLinePlusSum + ";" + saveMessage).getBytes();
				try{
					FileOutputStream out = new FileOutputStream(newfile);
					out.write(gameRcords);
					out.close();
					addToChatLabel("游戏提示: 保存成功");
				}
				catch (IOException e){
					System.out.println(e);
				}	
			}
		}
		c.sendMessage(clientChannel,"saveDone;" + tableNumber + ";");
	}

	public void actionPerformed(ActionEvent e)
	{ 
		if(waitForReply){											//游戏界面初始化等 可能需要锁定用户操作
			addToChatLabel("系统提示: 等待服务器响应");
			return;
		}
		if (e.getSource() == startGame && user_color <= 1) {		//发起 准备游戏 请求
			if(isStart)	return;
			String newcontent = "ready;" + tableNumber + ";";		
			c.sendMessage(clientChannel,newcontent);
			clearRecords();
			step = 0;
			SetWinLineRecord("-1","-1","-1","-1");
			repaint();
			startGame.setEnabled(false);
		}
		else if (e.getSource() == sendMessage || e.getSource() == messageField) {		//点击发送 或 按回车键 发起 通话
			String text = messageField.getText();
			if(text.equals(""))	return;
			String newcontent = "userMessage;" + tableNumber + ";" + text;		
			c.sendMessage(clientChannel,newcontent);
			messageField.setText("");
		}
		else if (e.getSource() == clearMessage) {
			String text = chatLabel.getText();
			if(text.equals(""))	return;
			chatLabel.setText("");
		}
		else if (e.getSource() == retract) {						//发起 悔棋 请求
			if(waitForRetractReply){
				addToChatLabel("游戏提示: 等待对方回应");
				return;
			}
			if((isStart!=true)||(step < 1&&user_color == nowColor)||(step < 2&&user_color != nowColor)||(user_color < 0)){
				addToChatLabel("游戏提示: 禁止操作！");
				return;
			}
			waitForRetractReply = true;
			retract.setEnabled(false);
			String newcontent = "rollbackRequest;" + tableNumber + ";" + step + ";";	
			c.sendMessage(clientChannel,newcontent);
		}
		else if (e.getSource() == admitLose) {						//认输

			if((step < 7)||(!isStart)||(user_color < 0 ))	{
				addToChatLabel("游戏提示: 目前不可认输！");
				return;
			}
			int i = JOptionPane.showConfirmDialog(null, "确定认输？", "认输", JOptionPane.YES_NO_OPTION);
			if(i == JOptionPane.YES_OPTION){
				String newcontent = "admitLose;" + tableNumber + ";" + user_color;
				c.sendMessage(clientChannel,newcontent);
			}
		}
		else if (e.getSource() == draw) {							//发起 和棋 请求
			if(waitForDrawReply){
				addToChatLabel("游戏提示: 等待对方回应");
				return;
			}
			if((user_color < 0 )||(!isStart))	{
				addToChatLabel("游戏提示: 禁止操作！");
				return;
			}
			//if(step < 7)	return;									//7步内禁止和棋,根据需要开/闭
			waitForDrawReply = true;
			draw.setEnabled(false);
			String newcontent = "drawRequest;" + tableNumber + ";";
			c.sendMessage(clientChannel,newcontent);
		}
		else if (e.getSource() == review) {							//观看复盘
			if(isStart){
				addToChatLabel("游戏提示: 禁止操作！");
				return;
			}
			JFileChooser chooser = new JFileChooser("..\\棋谱");
			int status = chooser.showOpenDialog(null);
			if(status != JFileChooser.APPROVE_OPTION){	
				System.out.println("No file chosen!");
				return;
			}
			try{
				File file = chooser.getSelectedFile();
				Scanner scan = new Scanner(file);
				String info = "";
				while(scan.hasNext())
				{	info += scan.nextLine();}
				System.out.println(info);
				scan.close();
				Review review = new Review(c);
				review.initGame(info);
			}
			catch(Exception e3)
			{	System.out.println(e3);}
		}
		if (e.getSource() == setImage) {							//修改形象
			JFileChooser chooser = new JFileChooser();
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
						int byteread = 0;
						if (file.exists()) {									//文件存在时   
							InputStream inStream = new FileInputStream(file);	//读入用户头像图片  
							FileOutputStream fs = new FileOutputStream(c.imagepath + userId + "-" + file.getName()); 
							byte[] buffer = new byte[1024];               
							//int length;               
							while ( (byteread = inStream.read(buffer)) != -1) {          
								fs.write(buffer, 0, byteread);           
							}             
							inStream.close();
							fs.close();
						}
						File txt = new File("src/dll.txt");						//向配置文件写入头像信息,新信息顶置
						FileReader filein = new FileReader("src/dll.txt");
						BufferedReader br = new BufferedReader(filein);
						String temp = null;
						String record = "userId = " + userId +";imageName = " + userId + "-" + file.getName()+ "\r\n";
						while((temp = br.readLine()) != null) {
							if(temp.trim().equals("")) continue;
							System.out.println("temp = "+temp);
							String dlls[] = temp.split(";");
							String dllIds[] = dlls[0].split("=");
							String dllId = dllIds[1].trim();
							if(!dllId.equals(userId)){
								record = record + temp + "\r\n";
							}
						}
						filein.close();
						br.close();
						byte []contents = (record).getBytes();
						try{
							FileOutputStream out = new FileOutputStream(txt,false);		//重写
							out.write(contents);
							out.close();
						}
						catch (IOException ee)
						{	System.out.println(ee);}
						addToChatLabel("系统提示: 设置成功,重启游戏生效");
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

	public boolean filterType(String fileType,String[] types) {
		for (String type : types) {
			if (fileType.equals(type)) {
				return true;
			}
		}
		return false;
	}

	PlayerInfor playerInfor = null;

	public void mouseClicked(MouseEvent e)							//由指针位置确定棋盘位置
	{
		if(e.getModifiers() != 16) return;							//只接收左击事件
		//System.out.println("isStart = " + isStart);
		if(waitForReply == true){
			addToChatLabel("系统提示: 等待服务器响应");
			return;
		}
		for(int i = 0 ; i < viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){					//查看玩家信息
				waitForReply = true;
				if(playerInfor != null){
					playerInfor.dispose();
					playerInfor = null;
				}
				playerInfor = new PlayerInfor(c);
				playerInfor.setInfor(viewersInforsID[i],viewersInforsName[i],viewersInforsScore[i],viewersPortrait[i]);
				c.sendMessage(clientChannel,"viewPlayersInfor;" + viewersInforsID[i] + ";Wuziqi");
			}
		}
		if (isStart){												// 判断游戏未开始
			int x, y;
			final int localx,localy;
			final String sx,sy;
			x = e.getX(); y = e.getY();
			x -= c.dev_y; y -= c.dev_x;
			if (x < c.halflength || x > c.maxlength + c.halflength || y < c.halflength || y > c.maxlength +c.halflength )
			{  return;  }
			if (x % c.WLen > c.halflength)
			{  x += c.WLen;  } 
			if (y % c.WLen > c.halflength) 
			{  y += c.WLen;  }
			x = x / c.WLen;
			y = y / c.WLen;
			sx=x + "";
			sy=y + "";
			localx = x;
			localy = y;
			String newcontent = "started;" + tableNumber + ";" + sy + ";" + sx + ";" + user_color + ";";		//warning:坐标是反的
			c.sendMessage(clientChannel,newcontent);								//发送 落子 请求
		}
	}
	public void mouseEntered(MouseEvent e){ 
		for(int i=0 ; i<viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){					//指向玩家信息,背景变蓝
				viewersInfors[i].setOpaque(true);
				viewersInfors[i].setBackground(new Color(48,117,174)); 
			}
		}
	}
	public void mouseExited(MouseEvent e) {							//指出玩家信息,背景恢复
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
		super.paintComponents(g);									// 绘制此容器中的每个组件
		g.setColor(Color.lightGray);								//棋盘背景颜色
		//g.setColor(Color.black);									//棋盘背景颜色
		g.fill3DRect(c.halflength+c.dev_y, c.halflength+c.dev_x, c.maxlength, c.maxlength, true);		//棋盘背景大小
		g.setColor(Color.black);									//棋盘线颜色
		//g.setColor(Color.white);									//棋盘线颜色
		for (int i = 1; i < 16; i++){
			g.drawLine( c.WLen+c.dev_y,  c.WLen*i+c.dev_x,  c.maxlength+c.dev_y,  c.WLen*i+c.dev_x);	//画棋盘线
			g.drawLine(c.WLen*i+c.dev_y,  c.WLen+c.dev_x,   c.WLen*i+c.dev_y, c.maxlength+c.dev_x);	//当前棋子显示窗格
		} 
		for(int i = 0 ; i < stepRecords.length ; i++)				//移动窗口，触发此方法。这里重画所有内容
		{
			if(stepRecords[i].getX() < 0)
			{	break;}
			int wy = c.WLen*stepRecords[i].getX();					//warning:坐标是反的
			int wx = c.WLen*stepRecords[i].getY();
			int set_color  = stepRecords[i].getColor();
			if (set_color == 0)	{									//判断黑子还是白子 
				g.setColor(Color.white);
				g.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   //画棋子
				g.setColor(Color.black);							//在客户端画出正在走棋的用户的颜色
			}
			else{ 
				g.setColor(Color.black);
				g.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   //画棋子
				g.setColor(Color.white);							//在客户端画出正在走棋的用户的颜色
			}
			g.fillOval(c.m(20) + c.dev_x, c.m(150), c.WLen, c.WLen);						//在客户端画出持子用户的颜色

			if(step > 1){
				int formerY = stepRecords[step - 2].getX();
				int formerX = stepRecords[step - 2].getY();
				int formerColor = stepRecords[step - 2].getColor();

				int formerx = c.WLen * formerX;
				int formery = c.WLen * formerY;
				if (set_color == 1){								//判断黑子还是白子 
					g.setColor(Color.white);						//上一步落子颜色
					g.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   //覆盖落子
				}
				else{ 
					g.setColor(Color.black);						//上一步落子颜色
					g.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   //覆盖落子
				}
			}
			if(i == step - 1){
				g.setColor(Color.red);
				g.drawLine(wx + c.dev_y , wy + c.dev_x - c.redlength, wx + c.dev_y , wy + c.dev_x + c.redlength);
				g.drawLine(wx + c.dev_y - c.redlength, wy + c.dev_x , wx + c.dev_y + c.redlength , wy + c.dev_x );	
			}
		}
		drawWinLine();
		g.drawImage(myImage,c.m(20) ,c.m(270) ,c.m(45) ,c.m(65) ,this); 
		g.drawImage(oppoImage,c.m(20) ,c.m(73) ,c.m(45) ,c.m(65) ,this); 
	} 

	public void myPaint(){
		paint(this.getGraphics());
	}
	public void setDown(int x, int y, int set_color)				//系统允许落子,落子
	{
		stepRecords[step].setStepRecord(x,y,set_color);				//数组0号也用
		Graphics s_graphics = this.getGraphics();
		//System.out.println(x + "," + y);
		int wy = c.WLen * x;										//warning:坐标是反的
		int wx = c.WLen * y;
		if (set_color == 0){										// 判断黑子还是白子 
			s_graphics.setColor(Color.white);						//当前落子颜色
			s_graphics.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   //落子
			s_graphics.setColor(Color.black);						//在客户端画出正在走棋的用户的颜色
		}
		else{ 
			s_graphics.setColor(Color.black);						//当前落子颜色
			s_graphics.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   //落子
			s_graphics.setColor(Color.white);						//在客户端画出正在走棋的用户的颜色
		}
		s_graphics.fillOval(c.m(20) + c.dev_x, c.m(150), c.WLen, c.WLen);						//在客户端画出持子用户的颜色
		step ++;
		if((step >= 7)&&(isStart)&&(user_color >= 0 ))	{
			admitLose.setEnabled(true);
		}
		nowColor = (set_color + 1)%2;

		if(step > 1){												//将上一步的棋子重绘，以掩盖其标记红十字
			int formerY = stepRecords[step - 2].getX();
			int formerX = stepRecords[step - 2].getY();
			int formerColor = stepRecords[step - 2].getColor();

			int formerx = c.WLen * formerX;
			int formery = c.WLen * formerY;
			if (set_color == 1){									//判断上一步落子颜色黑子还是白子 
				s_graphics.setColor(Color.white);				
				s_graphics.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   //覆盖落子
			}
			else{ 
				s_graphics.setColor(Color.black);				
				s_graphics.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   //覆盖落子
			}
		}
		s_graphics.setColor(Color.red);								//当前步标记出醒目红十字
		s_graphics.drawLine(wx + c.dev_y , wy + c.dev_x - c.redlength, wx + c.dev_y , wy + c.dev_x + c.redlength);
		s_graphics.drawLine(wx + c.dev_y - c.redlength, wy + c.dev_x , wx + c.dev_y + c.redlength , wy + c.dev_x );

		lblWin.setText("当前步数：" + step + " " + startColor((set_color + 1) % 2) + "执棋");
	} 
	public String startColor(int x)
	{
		if (x == 0) { return "白子"; } 
		else { return "黑子"; } 
	}

	public void sendImage(byte [] myImageBytes)						//上传玩家自己的形象
	{
		int n = 0;
		try{
			File file =new File(myImageName);
			if(file.length() > c.MaxImageLength){
				System.out.println("文件长度过长");//
				return;
			}
			FileInputStream  fr = new FileInputStream (file);
			byte[] b = new byte[1024];
			ByteBuffer sendbuffer; 
			while ((n = fr.read(b)) > 0) {	
				sendbuffer = ByteBuffer.wrap(b,0,n);
				clientChannel.write(sendbuffer);
				sendbuffer.flip();
				Thread.sleep(3);	//Thread.sleep(3);
			}
			fr.close();
		}
		catch(Exception error){
			System.out.println(error);
			System.out.println("数据发送失败");
		}
	}

	public void getMessage(String uId ,String uName,String userMessage)		//显示玩家发来的消息						
	{
		String text = "";
		if(userId.equals(uId)){
			text = "你说: " + userMessage;
		}
		else{
			text = uName + "说: " + userMessage;
		}
		addToChatLabel(text);
	}

	public void addToChatLabel(String text){						//向聊天框内写入信息
		if(chatLabel.getText().equals(""))
		{	chatLabel.append(text);}
		else chatLabel.append("\r\n"+text );
		JScrollBar bar = chatLabelScroll.getVerticalScrollBar();
		bar.setValue(bar.getMaximum());
	}

	public void getViewersInforPartly()								//向服务器请求刷新旁观者信息	
	{
		String newcontent = "refreshViewersInforPartly;" + tableNumber + ";";				//刷新信息请求
		c.sendMessage(clientChannel,newcontent);
	}

	String formerViewersInfor = "";									//记录上一次所有旁观者信息,与本次对比判断旁观者进入或离开
	JLabel viewersInfors[] = new JLabel[10];
	String viewersInforsID[] = new String[10];
	String viewersInforsName[] = new String[10];
	String viewersInforsScore[] = new String[10];
	String viewersPortrait[] = new String[10];

	public void refreshViewersInfor(String Viewer)					//刷新旁观者信息
	{
		//格式："userId , usreName , color , score"
		String []viewers = Viewer.split("↑");
		int viewersLen = 0;
		String []formerViewers = formerViewersInfor.split("↑");
		int formersLen = 0;

		for(int i = 0;i< viewers.length ;i++){
			if(viewers[i].trim().equals("")){
				viewersLen = i;
				break;
			}
			else{
				viewersLen++;
			}
		}

		for(int i = 0;i< formerViewers.length ;i++){
			if(formerViewers[i].trim().equals("")){
				formersLen = i;
				break;
			}
			else{
				formersLen++;
			}
		}

		if(!Viewer.equals(formerViewersInfor)){
			for(int i = 0;i< viewersLen ;i++){
				boolean isExist = false;
				String []viewerMasses = viewers[i].split("↓");
				for(int j = 0;j< formersLen ;j++){
					String []formerMasses = formerViewers[j].split("↓");
					if(viewerMasses[0].trim().equals(formerMasses[0].trim())){
						isExist = true;
						break;										//玩家依然在，无需更新，结束本轮循环
					}
				}
				if(!isExist){
					if(!viewers[i].trim().equals("")){
						String []mass = viewers[i].split("↓");
						addToChatLabel("游戏提示: " + mass[1]+" 进入本桌");
					}
				}
			}
			for(int i = 0;i < formersLen ;i++){
				boolean isExist = false;
				String []formerMasses = formerViewers[i].split("↓");
				for(int j = 0;j < viewersLen ;j++){
					String []viewerMasses = viewers[j].split("↓");
					if(viewerMasses[0].equals(formerMasses[0].trim())){
						isExist = true;
						break;										//玩家依然在，无需更新，结束本轮循环
					}
				}
				if(!isExist){
					if(!formerViewers[i].trim().equals("")){
						String []mass = formerViewers[i].split("↓");
						addToChatLabel("游戏提示: " + mass[1]+" 离开本桌");
					}
				}
			}
			formerViewersInfor = Viewer;
		}

		String infor = "";
		showUsers.setText("");
		int perLength = 10;											//姓名分数格式化长度
		showUsers.removeAll();
		for(int i = 0 ; i < viewersLen ; i++)						//生成所有旁观者(包括玩家)信息面板
		{
			String []mass = viewers[i].split("↓");
			if(mass[0].equals("")) continue;
			viewersInforsID[i] = mass[0];
			viewersInforsName[i] = mass[1]; 
			viewersInforsScore[i] = mass[3]; 
			viewersPortrait[i] = mass[4];
			int nameLength = mass[1].getBytes().length;
			if(nameLength > 8){
				mass[1] = mass[1].substring(0,3)+"…";
				nameLength = mass[1].getBytes().length;
			}
			String namePos = "";
			for(int j= 0;j < perLength - nameLength;j++){
				namePos += "  ";
			}
			if(!isStart && (mass[2].equals("0")||mass[2].equals("1")) && !mass[1].equals(userName) && user_color == -1){
				addToChatLabel("游戏提示: " + mass[1]+" 准备游戏");
			}

			String vColor = "";
			if(mass[2].equals("0")&&(user_color == -2)){			//旁观者显示玩家执子颜色
				vColor = " 持白子";
			}
			else if(mass[2].equals("1")&&(user_color == -2)){		//旁观者显示玩家执子颜色
				vColor = " 持黑子";
			}
			infor = "    玩家 " + mass[1] + namePos  +"分数 " + mass[3] + vColor;
			viewersInfors[i] = new JLabel(infor);
			//viewersInfors[i].setBorder(BorderFactory.createLineBorder(Color.gray));
			showUsers.add(viewersInfors[i]);						//加入旁观者信息面板
			viewersInfors[i].setBounds(0,20 * i,217,20);
			showUsers.append("\r\n");
			viewersInfors[i].addMouseListener(this);
		}
	}

	public void getGamersInforPartly()					
	{
		String newcontent = "refreshGamersInforPartly;" + tableNumber + ";";				//刷新信息请求
		c.sendMessage(clientChannel,newcontent);
	}

	public void refreshGamersInfor(String infor1,String infor2)		//刷新玩家信息(
	{
		//格式："userId , usreName , color , score"
		String thisScore = "";
		String oppoScore = "";
		if(user_color < 0) {										//旁观者
			String []mass1 = infor1.split(",");
			thisId = mass1[0];
			thisName = mass1[1];
			thisColor = mass1[2];
			thisScore = mass1[3];

			String []mass2 = infor2.split(",");
			oppoId = mass2[0];
			oppoName = mass2[1];
			oppoColor = mass2[2];
			oppoScore = mass2[3];
			return ;												//旁观者执行到此,下面为玩家执行
		}
		String []mass1 = infor1.split(",");							//第一个玩家的信息
		if(mass1[0].equals(userId)){								//是自家信息
			myInfor.setText(mass1[1] + "  " + mass1[3]);	
			thisId = mass1[0];
			thisName = mass1[1];
			thisColor = mass1[2];
			thisScore = mass1[3];
		}	
		else {														//是对家信息
			oppsInfor.setText(mass1[1] + "  " + mass1[3]);	
			oppoId = mass1[0];
			oppoName = mass1[1];
			oppoColor = mass1[2];
			oppoScore = mass1[3];
		}
		
		String []mass2 = infor2.split(",");							//第二个玩家的信息
		if(mass2[0].equals(userId)){								//是自家信息
			myInfor.setText(mass2[1] + "  " + mass2[3]);
			thisId = mass2[0];
			thisName = mass2[1];
			thisColor = mass2[2];
			thisScore = mass2[3];
		}
		else {														//是对家信息
			oppsInfor.setText(mass2[1] + "  " + mass2[3]);
			oppoId = mass2[0];
			oppoName = mass2[1];
			oppoColor = mass2[2];
			oppoScore = mass2[3];
		}

		if(!isStart) return ;										//游戏开始方可继续执行
		String mycolor = startColor(user_color);
		String infor = thisName + "  " + thisScore;
		infor = infor + "  持" + mycolor;
		myInfor.setText(infor);										//更新自己信息

		String oppositecolor;
		if(user_color==0){
			oppositecolor = "黑子";
		}
		else{
			oppositecolor = "白子";
		}
		infor = oppoName + "  " + oppoScore;
		infor = infor + " 持" + oppositecolor;
		oppsInfor.setText(infor);									//更新对家信息
	}

	public void refreshGamersInfor(String infor1)					//刷新玩家信息(对家逃跑剩自己时执行)
	{
		//格式："userId , usreName , color , score"
		if(user_color < 0) {
			return ;
		}
		String []mass1 = infor1.split(",");
		if(Integer.parseInt(mass1[2]) == user_color){	
			myInfor.setText(mass1[1] + "  " + mass1[3]);	
			thisId = mass1[0];
			thisName = mass1[1];
			thisColor = mass1[2];
		}

		if(!isStart) return ;
		String mycolor = startColor(user_color);
		String infor = mass1[1] + "  " + mass1[3];
		infor = infor + "  持" + mycolor;
		myInfor.setText(infor);
	}

	public void replyRBForward(String step)							//应答对家悔棋请求
	{
		int reply;
		int i = JOptionPane.showConfirmDialog(null, "是否同意对方悔棋请求？", "同意", JOptionPane.YES_NO_OPTION);
		if(i == JOptionPane.YES_OPTION){
			reply = 1;
		}
		else reply = 0;
		String newcontent = "replyRBForward;" + getTableNumber() + ";" + reply + ";" + step + "";
		c.sendMessage(clientChannel,newcontent);
	}

	public void rollback(int rstep,int St)
	{
		if(St == 1){
			stepRecords[step - 1].setStepRecord(-1,-1,-1);
			step = step - 1;
		}
		else if(St == 2){
			stepRecords[step - 1].setStepRecord(-1,-1,-1);
			stepRecords[step - 2].setStepRecord(-1,-1,-1);
			step = step - 2;
		}
		waitForRetractReply = false;
		retract.setEnabled(true);
		paint(this.getGraphics());
	}

	public void noRollback(int requestColor){
		if(user_color == requestColor){
			addToChatLabel("游戏提示: 对方拒绝悔棋");
			setWaitForRetractReply(false);
			retract.setEnabled(true);
		}
	}

	public void drawRequest()										//应答对家和棋请求
	{
		int reply;
		int i = JOptionPane.showConfirmDialog(null, "是否同意对方和棋请求？", "同意", JOptionPane.YES_NO_OPTION);
		if(i == JOptionPane.YES_OPTION){
			reply = 1;
		}
		else reply = 0;
		String newcontent = "drawRequestReply;" + getTableNumber() + ";" + reply + ";";
		c.sendMessage(clientChannel,newcontent);
	}
} 
	
class StepRecord													//记录落子信息类
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
	{	return x;	}
	public int getY()
	{	return y;	}
	public int getColor()
	{	return color;	}
}