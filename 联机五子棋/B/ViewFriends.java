/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.Insets;
import java.awt.Color;
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
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.FileReader;

class ViewFriends extends JFrame	implements ActionListener, MouseListener, MouseMotionListener 	
{
	private Constant c;
	private InforChange client;
	private GameHall gameHall;										//游戏大厅 指针
	private JTextArea showUsers = new JTextArea("");
	private JScrollPane	showUsersScroll = new JScrollPane(showUsers);
	private JLabel viewersInfors[] = new JLabel[100];
	private String viewersInforsID[] = new String[100];
	private String viewersInforsName[] = new String[100];
	private PopupMenu viewUserPM = new PopupMenu();
	private MenuItem VUChat = new MenuItem();
	private MenuItem delFriend = new MenuItem();

	public void setGameHall(GameHall GH){
		gameHall = GH;
	}

	public void setInforChange(InforChange ic){
		client = ic;
	}

	ViewFriends(Constant cc,String infors)							//初始化服务器
	{
		super("好友列表");											//设置标题
		c = cc;
		try {
			String src = c.SysImgpath + "default.png";		
			Image image = ImageIO.read(this.getClass().getResource(src));
			this.setIconImage(image);								//设置图标
		}
		catch (Exception e) {
			System.out.println(e);
		}

		((JPanel)getContentPane()).setOpaque(false);

		setLayout(null);
		setResizable(false);
		setVisible(true);

		showUsersScroll.setBounds(c.m(0), c.m(0), c.m(118), c.m(240));	//"用户显示框"
		add(showUsersScroll);
		showUsers.setOpaque(true);
		showUsers.setBackground(c.chatColor);
		showUsers.setEditable(false); 

		VUChat.setLabel("                私聊                ");
		VUChat.addActionListener(this);

		
		delFriend.setLabel("            删除好友             ");
		delFriend.addActionListener(this);

		viewUserPM.add(VUChat);
		viewUserPM.add(delFriend);
		add(viewUserPM);

		this.setBounds(870,130,c.m(120),c.m(250));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				dispose();
				client.setViewFriends(null);						//置空客服端查看好友面板,可以打开新的好友面板
			}
		});
		try{
			String friends[] = infors.split(";");

			for(int i = 1,sum = 0; i < friends.length;i++){
				String friendsInfors[] = friends[i].split(",");
				if(!friendsInfors[0].equals("")){
					int perLength = 10;								//ID格式化长度
					String userFormatId = friendsInfors[0];
					int idLenth = userFormatId.getBytes().length;
					if(idLenth > 8){
						userFormatId = userFormatId.substring(0,3)+"…";
						idLenth = userFormatId.getBytes().length;
					}
					String idPos = "";
					for(int j= 0;j < perLength - idLenth;j++){
						idPos += "  ";
					}
					String infor = "    ID " + userFormatId + idPos  + "昵称 " + friendsInfors[1] ;
					viewersInforsID[sum] = friendsInfors[0];
					viewersInforsName[sum] = friendsInfors[1];
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
		catch(Exception e){
			e.printStackTrace();
		}
	}

	String chatToUserId = "";
	String chatToUserName = "";
	String chatToUserPortrait = "";

	public void actionPerformed(ActionEvent e){
		if (e.getSource() == VUChat){							//与某确定玩家聊天
			gameHall.setChatTo(chatToUserId,chatToUserName);	//调用大厅方法
			dispose();
			client.setViewFriends(null);							//置空客服端查看好友面板,可以打开新的好友面板
		}
		
		if (e.getSource() == delFriend){							//删除好友
			gameHall.delFriend(chatToUserId,chatToUserName);		//调用大厅方法
			dispose();
			client.setViewFriends(null);							//置空客服端查看好友面板,可以打开新的好友面板
		}
	}

	public void mouseClicked(MouseEvent e)				
	{
		if(e.getModifiers() == 4){
			for(int i = 0 ; i < viewersInfors.length ; i++){
				if (e.getSource() == viewersInfors[i]){				//由 用户显示框 打开玩家 右键菜单
					viewUserPM.show(viewersInfors[i],e.getX(),e.getY());
					chatToUserId = viewersInforsID[i];
					chatToUserName = viewersInforsName[i];
					//chatToUserPortrait = playersPortrait[i];
					break;
				}
			}
		}
		if(e.getModifiers() != 16) return;							//只接收左击事件
		for(int i = 0 ; i < viewersInfors.length ; i++){
			if (e.getSource() == viewersInfors[i]){
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
	}
}

