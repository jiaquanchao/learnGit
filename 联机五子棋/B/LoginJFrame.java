/*
 * ����:	 �����
 * QQ:	 	 714670841
 * ����:	 714670841@qq.com
 * ��������:EditPlus
 * Copyright 2014 ����� 
 * ����Ʒֻ���ڸ���ѧϰ���о������ͣ�ת����ע��������
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
	private SocketChannel clientChannel;							//��½�����ȡSocketChannel
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
		super("�û���¼");											//���ñ���
		c = cc;
		try {
			String src = c.SysImgpath + "default.png";		
			Image image=ImageIO.read(this.getClass().getResource(src));
			this.setIconImage(image);								//����ͼ��
		} 
		catch (Exception e) {
			System.out.println(e);
		}   
	}

	public void startClient()										//�򿪵�¼����
	{
		setResizable(false);
		getContentPane().setLayout(null);
	
		jLabel1 = new JLabel();
		getContentPane().add(jLabel1);
		jLabel1.setText("�˺�");
		jLabel1.setBounds(39, 39, 63, 18);
	
		idtext = new JTextField();
		getContentPane().add(idtext);
		idtext.setBounds(109, 37, 156, 22);
	
		jLabel2 = new JLabel();
		getContentPane().add(jLabel2);
		jLabel2.setText("����");
		jLabel2.setBounds(39, 77, 38, 18);
	
		passtext = new JPasswordField();
		getContentPane().add(passtext);
		passtext.setBounds(109, 75, 156, 22);
	
		loginbutton = new JButton();
		getContentPane().add(loginbutton);
		loginbutton.setText("��¼");
		loginbutton.setBounds(109, 113, 60, 28);

		loginbutton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String name = idtext.getText().trim();
				String password = new String(passtext.getPassword()).trim();
				if(name.equals("")){
					hint.setText("�������û���");
					name = "";
					return;
				}
				if(password.equals("")){
					hint.setText("����������");
					password = "";
					return;
				}
				String strRegex = "[\u4e00-\u9fa5a-zA-Z0-9]*";		//������ĸ����
				Pattern p = Pattern.compile(strRegex);
				Matcher m = p.matcher(name); 
				if(!name.matches(strRegex)){
					hint.setText("�˺�ֻ���Ǻ���,��ĸ,����");
					return;
				} 
				if(name != null&&name.length() > 0){
					String message = "login;" + name + ";" + password;
					hint.setText("������֤�ͻ��ˣ����Ժ�");
					c.sendMessage(clientChannel,message);			//�����û���Ϣ����½��Ϸ
				}
			}
		});

		registebutton = new JButton();
		getContentPane().add(registebutton);
		registebutton.setText("ע��");
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