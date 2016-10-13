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
import java.awt.Color;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JFrame;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import java.awt.Image;
import javax.swing.SwingUtilities;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Review extends JFrame implements ActionListener, MouseListener, MouseMotionListener 
{
	private Constant c;
	private GoThread goThread = null;
	private Graphics g;
	private boolean flag = false;
	private StepRecord stepRecords[] = new StepRecord[225];			//	记录玩家所有落子信息
	private int nowColor = -1;	
	private int user_color = -1;									// 用户所持棋子的颜色标识 0:白子 1:黑子
	private int step = 0;											//当前步数
	private int nowstep = 0;
	private boolean isStart = false;								//游戏开始标志
	private JButton playControl = new JButton("播放");
	private JButton rollback2 = new JButton("后退2步");
	private JButton rollback5 = new JButton("后退5步");
	private JLabel lblWin = new JLabel(" ");
	private JLabel oppsInfor = new JLabel(" ");
	private JLabel myInfor = new JLabel(" ");
       
	public void setIsStart(boolean start)
	{	isStart = start;	}
	public boolean getIsStart()
	{	return isStart;	}

	public void setColor(int uColor)
	{	user_color = uColor;}

	Review(Constant cc){
		super("复盘回放");											//设置标题
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
		ImageIcon img = new ImageIcon(c.SysImgpath + "bg2.jpg");
		JLabel bgLabel = new JLabel(img);
		bgLabel.setBounds(0,-15,1024,850);
		this.getLayeredPane().add(bgLabel, new Integer(Integer.MIN_VALUE));
		((JPanel)getContentPane()).setOpaque(false);

		setLayout(null);
		addMouseListener(this);
		setResizable(false);

		add(oppsInfor);
		oppsInfor.setBounds(c.m(0) + c.dev_x, c.m(20), c.m(70), c.m(30));		//"提示对家的信息"
		
		add(playControl);
		playControl.setBounds(c.m(70) + c.dev_x, c.m(320), c.m(45), c.m(20));	//"播放"
		playControl.addActionListener(this);

		add(rollback2);
		rollback2.setBounds(c.m(210) + c.dev_x, c.m(320), c.m(45), c.m(20));	//"后退2步"
		rollback2.addActionListener(this);

		add(rollback5);
		rollback5.setBounds(c.m(280) + c.dev_x, c.m(320), c.m(45), c.m(20));	//"后退5步"
		rollback5.addActionListener(this);

		add(myInfor);
		myInfor.setBounds(c.m(0) + c.dev_x, c.m(220), c.m(70), c.m(30));		//"提示我的信息"
		
		add(lblWin);
		lblWin.setBounds(c.m(0) + c.dev_x, c.m(160), c.m(70), c.m(30));			//"提示"

		playControl.setMargin(new Insets(0,0,0,0));
		rollback2.setMargin(new Insets(0,0,0,0));
		rollback5.setMargin(new Insets(0,0,0,0));

		this.setBounds(160,0,c.wsizex,c.wsizey);

		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				dispose();
			}
		});
	}
	public void initGame(String info)
	{
		init();
		final int POSITION = 6;
		//String ucolor = startColor(Integer.parseInt(tcolor));
		//lblWin.setText("游戏开始," + ucolor + "先手");

		for(int i = 0; i < stepRecords.length ; i++)
		{	stepRecords[i] = new StepRecord();	}	
		try{
			String []serverInfor = info.split(";");

			String []mass1 = serverInfor[1].split(",");
			myInfor.setText("玩家" + mass1[1] + " 执" + startColor(Integer.parseInt(mass1[2])));

			String []mass2 = serverInfor[2].split(",");
			oppsInfor.setText("玩家 " + mass2[1] + " 执" + startColor(Integer.parseInt(mass2[2])));	

			step = Integer.parseInt(serverInfor[3]);

			for(int i = POSITION;i < step + POSITION; i++){
				String []locate =serverInfor[i].split(",");
				int initi = Integer.parseInt(locate[0]);
				int initj = Integer.parseInt(locate[1]);
				int initColor = Integer.parseInt(locate[2]);
				stepRecords[i - POSITION].setStepRecord(initi,initj,initColor);
				//initLocate(locate[0],locate[1],locate[2]);
			}
			DirectionRecord = Integer.parseInt(serverInfor[4]);
			winLinePlusSum = Integer.parseInt(serverInfor[5]);
			winLineX = stepRecords[step - 1].getX();
			winLineY = stepRecords[step - 1].getY();
		}
		catch(Exception e){
			System.out.println("文件读取失败" + e);
			this.dispose();
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{ 
		if (e.getSource() == playControl) {
			if(playControl.getText().equals("播放")){
				playControl.setText("暂停");
				g = this.getGraphics();
				flag = true;
				if(goThread == null){
					goThread = new GoThread();
					goThread.start();
				}
			}
			else if(playControl.getText().equals("暂停")){
				playControl.setText("播放");
				flag = false;
			}
			else if(playControl.getText().equals("重新播放")){
				playControl.setText("暂停");				
				g = this.getGraphics();
				if(nowstep == step){
					nowstep = 0;
					winLineDirection = -1;
					paint(g);
					if(!goThread.isAlive()){
						goThread = new GoThread();
						goThread.start();
					}
				}
				flag = true;
				if(goThread == null){
					goThread = new GoThread();
					goThread.start();
				}
			}
		}

		if (e.getSource() == rollback2) {
			winLineDirection = -1;
			rollback(2);
		}
		if (e.getSource() == rollback5) {
			winLineDirection = -1;
			rollback(5);
		}
	}
	public void mouseClicked(MouseEvent e){ }
	public void mouseEntered(MouseEvent e){ }
	public void mouseExited(MouseEvent e) { } 
	public void mouseReleased(MouseEvent e){ }
	public void mouseDragged(MouseEvent e){ }
	public void mouseMoved(MouseEvent e){ }
	public void mousePressed(MouseEvent e) { } 
	public void paint(Graphics g)									//绘制容器,画棋盘,窗口变化会重新执行
	{
		super.paintComponents(g);									// 绘制此容器中的每个组件

		g.setColor(Color.lightGray);								//棋盘背景颜色
		g.fill3DRect(c.halflength + c.dev_y, c.halflength + c.dev_x, c.maxlength, c.maxlength, true);				//棋盘背景大小
		g.setColor(Color.black);									//棋盘线颜色
		for (int i = 1; i < 16; i++){
			g.drawLine( c.WLen + c.dev_y,  c.WLen * i + c.dev_x,  c.maxlength + c.dev_y,  c.WLen * i + c.dev_x);	//画棋盘线
			g.drawLine(c.WLen * i + c.dev_y,  c.WLen + c.dev_x,   c.WLen * i + c.dev_y, c.maxlength + c.dev_x);	//当前棋子显示窗格
		} 
		
		for(int i = 0 ; i < nowstep; i++)							//移动窗口，触发此方法。这里重画所有内容
		{
			int wy = c.WLen * stepRecords[i].getX();
			int wx = c.WLen * stepRecords[i].getY();
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

			if(nowstep > 1){
				int formerY = stepRecords[nowstep - 2].getX();
				int formerX = stepRecords[nowstep - 2].getY();
				int formerColor = stepRecords[nowstep - 2].getColor();

				int formerx = c.WLen * formerX;
				int formery = c.WLen * formerY;
				if (set_color == 1){								// 判断黑子还是白子 
					g.setColor(Color.white);						//上一步落子颜色
					g.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   //覆盖落子
				}
				else{ 
					g.setColor(Color.black);						//上一步落子颜色
					g.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   //覆盖落子
				}
			}
			if(i == nowstep - 1){
				g.setColor(Color.red);
				g.drawLine(wx + c.dev_y , wy + c.dev_x - c.redlength, wx + c.dev_y , wy + c.dev_x + c.redlength);
				g.drawLine(wx + c.dev_y - c.redlength, wy + c.dev_x , wx + c.dev_y + c.redlength , wy + c.dev_x );	
			}
			drawWinLine();
		}
	} 
	
	Thread Swingrun = new Thread()
	{  
		public void run(){ 
			try{
				if(nowstep < 0) {
					nowstep = 0;
					return;
				}
				if(nowstep >= step) return;
				int wy = c.WLen * stepRecords[nowstep].getX();
				int wx = c.WLen * stepRecords[nowstep].getY();
				int set_color  = stepRecords[nowstep].getColor();
				lblWin.setText("当前步数："+ (nowstep+1) + " " + startColor((set_color + 1) % 2) + "执棋");
				if (set_color == 0)	{								//判断黑子还是白子 
					g.setColor(Color.white);
					g.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   //画棋子
					g.setColor(Color.black);						//在客户端画出正在走棋的用户的颜色
				}
				else{ 
					g.setColor(Color.black);
					g.fillOval(wx - c.halflength + c.dev_y, wy - c.halflength + c.dev_x, c.WLen, c.WLen);   //画棋子
					g.setColor(Color.white);						//在客户端画出正在走棋的用户的颜色
				}
				g.fillOval(c.m(20) + c.dev_x, c.m(150), c.WLen, c.WLen);						//在客户端画出持子用户的颜色	
				nowstep ++;

				if(nowstep > 1){
					int formerY = stepRecords[nowstep - 2].getX();
					int formerX = stepRecords[nowstep - 2].getY();
					int formerColor = stepRecords[nowstep - 2].getColor();

					int formerx = c.WLen * formerX;
					int formery = c.WLen * formerY;
					if (set_color == 1){							// 判断黑子还是白子 
						g.setColor(Color.white);					//上一步落子颜色
						g.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   //覆盖落子
					}
					else{ 
						g.setColor(Color.black);					//上一步落子颜色
						g.fillOval(formerx - c.halflength + c.dev_y, formery - c.halflength + c.dev_x, c.WLen, c.WLen);   //覆盖落子
					}
				}
				g.setColor(Color.red);
				g.drawLine(wx + c.dev_y , wy + c.dev_x - c.redlength, wx + c.dev_y , wy + c.dev_x + c.redlength);
				g.drawLine(wx + c.dev_y - c.redlength, wy + c.dev_x , wx + c.dev_y + c.redlength , wy + c.dev_x );	

				if(nowstep == step){

					winLineDirection = DirectionRecord;
					drawWinLine();

					lblWin.setText("游戏结束");
					playControl.setText("重新播放");
				}
			}
			catch(Exception e){
				System.out.println("终止播放");
			}
		}
	};
	
	public void rollback(int rstep)
	{
		if(rstep > nowstep)	return;
		nowstep = nowstep - rstep;
		paint(this.getGraphics());
		int set_color  = stepRecords[nowstep].getColor();
		lblWin.setText("当前步数："+ (nowstep) +" "+startColor((set_color)%2)+"执棋");
	}

	public String startColor(int x)
	{
		if (x == 0) { return "白子"; } 
		else { return "黑子"; } 
	}

	class GoThread extends Thread
	{
		public void run(){
			while (nowstep < step) {
				try {
					Thread.sleep(1500);
				}
				catch (InterruptedException e) {
					System.out.println(e);
				}
				if (flag) {
					SwingUtilities.invokeLater(Swingrun);			//将对象排到事件派发线程的队列中
				}
			}
		}
	}

	private int winLineX = -1;
	private int winLineY = -1;
	private int winLineDirection = -1;
	private int winLinePlusSum = -1;

	private int DirectionRecord = -1;

	public void SetWinLineRecord(String sx,String sy,String sdirec,String splus){
		winLineX = Integer.parseInt(sx);
		winLineY = Integer.parseInt(sy);
		winLineDirection = Integer.parseInt(sdirec);
		winLinePlusSum = Integer.parseInt(splus);
	}

	public void drawWinLine(){

		if(winLineDirection < 0) return;

		int wx = c.WLen * winLineY;
		int wy = c.WLen * winLineX;									//warning:坐标是反的
		int lineLength = winLinePlusSum * c.WLen;
		int opplineLength = (4 - winLinePlusSum) * c.WLen;

		Graphics graphics = this.getGraphics();
		Graphics2D g = (Graphics2D)graphics;
		float lineWidth = 3.0f;
		g.setStroke(new BasicStroke(lineWidth));

		g.setColor(Color.red);

		if(winLineDirection == 1){
			g.drawLine( wx + c.dev_y , wy + c.dev_x + lineLength ,  wx +c.dev_y, wy + c.dev_x);
			g.drawLine( wx + c.dev_y , wy + c.dev_x - opplineLength ,  wx +c.dev_y, wy + c.dev_x);
		}

		if(winLineDirection == 2){
			g.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y + lineLength , wy + c.dev_x);
			g.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y - opplineLength , wy + c.dev_x);
		}

		if(winLineDirection == 3){
			g.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y - lineLength , wy + c.dev_x + lineLength);
			g.drawLine( wx + c.dev_y , wy + c.dev_x,  wx + c.dev_y + opplineLength, wy + c.dev_x - opplineLength);
		}

		if(winLineDirection == 4){
			g.drawLine( wx + c.dev_y, wy + c.dev_x,  wx + c.dev_y + lineLength, wy + c.dev_x + lineLength);
			g.drawLine( wx + c.dev_y, wy + c.dev_x,  wx + c.dev_y - opplineLength, wy + c.dev_x - opplineLength);
		}
	}
} 