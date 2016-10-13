/*
 * 作者:	 韩旭滨
 * QQ:	 	 714670841
 * 邮箱:	 714670841@qq.com
 * 开发工具:EditPlus
 * Copyright 2014 韩旭滨 
 * 本作品只用于个人学习、研究或欣赏，转发请注明出处。
 */

import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.imageio.ImageIO;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LoginJFrame extends JFrame
{
	Constant c;
	private SocketChannel clientChannel;							//登陆界面获取SocketChannel
	private JLabel jLabel1;
	private JTextField idtext;
	private JLabel jLabel2;
	private JButton loginbutton;
	private JButton registebutton;
	public JLabel hint;
	private JPasswordField passtext;
	private InforChange inforChange;
	public RegistJFrame regist;

	public void setIdtext(String id){
		idtext.setText(id);
	}

	public void setInforChange(InforChange ic){
		inforChange = ic;
	}

	public void setPasstext(String pt){
		passtext.setText(pt);
	}

	public void setChannel(SocketChannel socketChannel)
	{	clientChannel = socketChannel;}

	LoginJFrame(Constant cc){
		super("用户登录");											//设置标题
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

	public void startClient()										//打开登录界面
	{
		setResizable(false);
		getContentPane().setLayout(null);
	
		jLabel1 = new JLabel();
		getContentPane().add(jLabel1);
		jLabel1.setText("账号");
		jLabel1.setBounds(39, 39, 63, 18);
	
		idtext = new JTextField();
		getContentPane().add(idtext);
		idtext.setBounds(109, 37, 156, 22);
	
		jLabel2 = new JLabel();
		getContentPane().add(jLabel2);
		jLabel2.setText("密码");
		jLabel2.setBounds(39, 77, 38, 18);
	
		passtext = new JPasswordField();
		getContentPane().add(passtext);
		passtext.setBounds(109, 75, 156, 22);
	
		loginbutton = new JButton();
		getContentPane().add(loginbutton);
		loginbutton.setText("登录");
		loginbutton.setBounds(109, 113, 60, 28);

		loginbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String name = idtext.getText().trim();
				String password = new String(passtext.getPassword()).trim();
				if(name.equals("")){
					hint.setText("请输入用户名");
					name = "";
					return;
				}
				if(password.equals("")){
					hint.setText("请输入密码");
					password = "";
					return;
				}
				String strRegex = "[\u4e00-\u9fa5a-zA-Z0-9]*";		//汉字字母数字
				Pattern p = Pattern.compile(strRegex);
				Matcher m = p.matcher(name); 
				if(!name.matches(strRegex)){
					hint.setText("账号只能是汉字,字母,数字");
					return;
				} 
				if(name != null&&name.length() > 0){
					String message = "login;" + name + ";" + password;
					hint.setText("正在验证客户端，请稍后…");
					c.sendMessage(clientChannel,message);			//发送用户信息并登陆游戏
				}
			}
		});

		registebutton = new JButton();
		getContentPane().add(registebutton);
		registebutton.setText("注册");
		registebutton.setBounds(200, 113, 60, 28);

		registebutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				RegistJFrame regist = new RegistJFrame(c);
				regist.setChannel(clientChannel);
				regist.setInforChange(inforChange);
				regist.startClient();
				inforChange.setRegistJFrame(regist);
				dispose();
			}
		});

		hint = new JLabel();
		getContentPane().add(hint);
		hint.setBounds(109, 8, 172, 23);
		this.setSize(318, 200);
		this.setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}