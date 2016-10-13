/*
 * ����:	 �����
 * QQ:	 	 714670841
 * ����:	 714670841@qq.com
 * ��������:EditPlus
 * Copyright 2014 ����� 
 * ����Ʒֻ���ڸ���ѧϰ���о������ͣ�ת����ע��������
 */

import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.awt.Color;
	
class Constant														//������,�����޸�
{
	//final String serverIp = "192.168.1.101";
	final String serverIp = "127.0.0.1";
	//final String serverIp = "42.122.224.220";
	final int serverPort = 55555;
	final int DeskNum = 10;											//��������(��������������:100)
	final int ChairNum = DeskNum * 2;								//��������
	final int perLineDesks;											//ÿ����ʾ��������(�Ƽ�ȡ1��2��3)
	final double initMutiple;										//����Ĭ�ϴ�С����(��׼Ϊ2.0)
	final double multiple;											//��ǰ���ڴ�С����	
	final int wsizex;												//����ˮƽ��С
	final int wsizey;												//������ֱ��С
	final int formerX;												//֮ǰ����ˮƽ��С
	final int formerY;												//֮ǰ������ֱ��С
	final int halflength;											//������С
	final int WLen;													//����С
	final int redlength;											//���Ѻ�ɫʮ�ֳ���
	final int maxlength;											//���̳���
	final int dev_x;												//���̾��ϱ߽�λ��
	final int dev_y;												//���̾���߽�λ��
	final int nowDeskPanelLength;									//���ӷ��ý����С
	final String STATEND = "stateEnd";								//��������־
	final int BUFFER_SIZE = 1024;									//��������Ϊ15(DLPImg0stateEnd)
	//final int BUFFER_SIZE = 20;									//��������Ϊ15(DLPImg0stateEnd)
	final int ImageBufferSize = 1024;								//����ͼƬbuffer
	final int MaxImageLength = (int)(1024000 * 10);					//����ͼƬ����ֽ���
	final int MaxByteLength = (int)(1024000 * 10);
	final String imagepath = "image/";
	final String SysImgpath = "sys_image/";
	final String portrait_path = "sys_portrait/";
	final String TableWaitImgPath = "sys_image/a.jpg";
	final String TableStartImgPath = "sys_image/c.jpg";
	final String ChairWaitImgPath = "sys_image/b.jpg";
	final Color chatColor = new Color(232,232,232);
	final int maxChatLabelsum = 50;
	final boolean debug = true;
	final boolean isFitPosition;									//�Ƿ��Զ�����(���ʱ�����Զ�����,��Ϊ�Զ������޷����)
	
	Constant(double IM){
		initMutiple = IM;
		multiple = initMutiple;	
		wsizex = m(512);	
		wsizey = m(364);
		formerX = wsizex;
		formerY = wsizey;
		halflength = m(10);											
		WLen = m(20);												
		redlength = m(4);											
		maxlength = m(300);											
		dev_x = m(15);												
		dev_y = m(80);		
		nowDeskPanelLength = m(340);															//m(340)Ϊ���ӷ��ý����ֵ��С,���ɸĶ�
		perLineDesks = nowDeskPanelLength / m(100);
		isFitPosition = true;
	}

	Constant(double IM,double m,int x,int y,int formerx,int formery,boolean fit,int FDL){		//�������/��ԭ ��Ϸ����,�޸Ļ�����λ��
		initMutiple = IM;
		multiple = m;
		wsizex = x;
		wsizey = y;
		formerX = formerx;
		formerY = formery;
		halflength = m(10);											
		WLen = m(20);												
		redlength = m(4);											
		maxlength = m(300);											
		dev_x = m(15);												
		dev_y = m(80);
		nowDeskPanelLength = wsizex - (int)(formerX*multiple/initMutiple) + (int)(FDL*multiple/initMutiple);	
		perLineDesks = nowDeskPanelLength / m(100);
		isFitPosition = fit;
	}

	public int m(int i){
		return (int)(multiple * i); 
	}

	public int extendL(int i) {
		int windowChangeLength = wsizex - (int)(formerX*multiple/initMutiple);		//���ڱ仯��С
		return windowChangeLength + m(i);											//�������ӽ����С+�Ӵ��ڱ仯��С
	}

	private ByteBuffer sbuffer;
	public void sendMessage(SocketChannel socket,String message)	//�������������Ϣ
	{
		try{
			message += STATEND;
			byte[] sendBytes = message.getBytes();					//Ҫ���͵�����
			sbuffer = ByteBuffer.allocate(sendBytes.length);		//���������洢�������ݵ�byte������
			sbuffer.put(sendBytes);									//������put��������
			sbuffer.flip();											//������������־��λ
			socket.write(sbuffer);									//���������������
			System.out.println("���ͣ�" + message);
		}
		catch(Exception e){
			System.out.println("���ݷ���ʧ��");
			if(debug){	e.printStackTrace();}
		}
	}
}