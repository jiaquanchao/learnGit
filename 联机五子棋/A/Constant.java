/*
 * ����:	 �����
 * QQ:	 	 714670841
 * ����:	 714670841@qq.com
 * ��������:EditPlus
 * Copyright 2014 ����� 
 * ����Ʒֻ���ڸ���ѧϰ���о������ͣ�ת����ע��������
 */

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Constant														//������
{
	final String serverIp = "127.0.0.1";
	//final String serverIp = "192.168.1.101";
	//final String serverIp = "42.122.224.220";
	final int serverPort = 55555;
	final boolean isUseDatabase = false;							//�����Ƿ�ʹ�����ݿ�
	final int maxTables  = 100;										//���������Ŀ
	final int maxUsers  = 2 * maxTables;							//��������Ŀ
	final String STATEND = "stateEnd";								//��������
	final int BUFFER_SIZE = 1024;									//ByteBuffer��С
	final int MaxImageLength = (int)(1024000 * 10);
	final double multiple = 1.0;									//���ڴ�С����(��׼Ϊ1.0)
	final int wsizex = m(512);										//����ˮƽ��С
	final int wsizey = m(364);										//������ֱ��С
	final boolean isShowUser = true;								//�Ƿ���ʾ��������ҽ���(ʾ�����)
	final String imagepath = "image/";
	final String SysImgpath = "sys_image/";
	final Color chatColor = new Color(232,232,232);
	final int ADD = 2;												//Ӯ�����ӷ���
	final int MINUS = -2;											//������ٷ���
	final int ESCAPEMINUS = -4;										//���ܼ��ٷ���
	final boolean debug = true;

	public int m(int i){
		return (int)(multiple * i); 
	}

	ByteBuffer sendbuffer;
	public void sendInforBack(SocketChannel client,String message)	//��ָ���û������ض���Ϣ
	{
		try{
			message += STATEND;										//������������־
			byte[] sendBytes = message.getBytes();					//Ҫ���͵�����
			sendbuffer = ByteBuffer.allocate(sendBytes.length);		//���������洢�������ݵ�byte������
			sendbuffer.put(sendBytes);								//������put��������
			sendbuffer.flip();										//������������־��λ
			client.write(sendbuffer);								//���������������
			System.out.println("���ͣ�" + message);
		}
		catch (Exception e) {
			System.out.println("���ݷ���ʧ��");
			if(debug){	e.printStackTrace();}
		} 
	}
}