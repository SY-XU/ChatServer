package chat.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.SSLContext;

import chat.function.ChatBean;
import chat.function.ClientBean;

public class ChatServer
{
	private static ServerSocket serverSocket;
	public static HashMap<String, ClientBean> onlines;
	
	static{//static{}(��static��)�������౻���ص�ʱ��ִ���ҽ��ᱻִ��һ�Σ�
			//һ��������ʼ����̬�����͵��þ�̬������
		try
			{
				serverSocket = new ServerSocket(8520);
				onlines = new HashMap<String,ClientBean>();
			} catch (IOException e)
			{
				// TODO: handle exception
				e.printStackTrace();
			}
	}
	class ChatClientThread extends Thread
	{
		private Socket client;
		private ChatBean bean;
		private ObjectInputStream oiStream;
		private ObjectOutputStream ooStream;
		
		public ChatClientThread(Socket client)
		{
			// TODO �Զ����ɵĹ��캯�����
			this.client = client;
		}
		
		@Override
		public void run()
		{
			try
				{
					while (true)
						{
							oiStream = new ObjectInputStream(client.getInputStream());
							bean = (ChatBean) oiStream.readObject();
							switch (bean.getType())
							{
							case 0://����
							{
								ClientBean cBean = new ClientBean();
								cBean.setName(bean.getName());
								cBean.setSocket(client);
								onlines.put(bean.getName(), cBean);
								ChatBean serverBean = new ChatBean();
								serverBean.setType(0);
								serverBean.setInfo(bean.getTimer()+" "+bean.getName()+"������ ");
								HashSet<String> set = new HashSet<String>();
								set.addAll(onlines.keySet());
								serverBean.setClients(set);
								sendAll(serverBean);
								break;
							}
							case -1://����
							{
								ChatBean serverBean = new ChatBean();
								serverBean.setType(-1);
								try
									{
										ooStream = new ObjectOutputStream(client.getOutputStream());
										ooStream.writeObject(serverBean);
										ooStream.flush();
									} catch (IOException e)
									{
										// TODO: handle exception
										e.printStackTrace();
									}
							
							
							onlines.remove(bean.getName());
							//��ʣ�µ��˷�������֪ͨ
							ChatBean serverBean_1 = new ChatBean();
							serverBean_1.setInfo(bean.getTimer() + "  "
									+ bean.getName() + " " + " ������");
							serverBean_1.setType(0);
							HashSet<String> set = new HashSet<String>();
							set.addAll(onlines.keySet());
							serverBean_1.setClients(set);
							sendAll(serverBean_1);
							return;
							}
							
							case 1://����
							{
								ChatBean serverBean = new ChatBean();
								serverBean.setType(1);
								serverBean.setClients(bean.getClients());
								serverBean.setInfo(bean.getInfo());
								serverBean.setName(bean.getName());
								serverBean.setTimer(bean.getTimer());
								sendMessage(serverBean);
								break;
							}
							
							case 2://��������ļ�
							{
								ChatBean serverBean = new ChatBean();
								String info = bean.getTimer() + "  " + bean.getName()
								+ "���㴫���ļ�,�Ƿ���Ҫ����";
								serverBean.setType(2);
								serverBean.setClients(bean.getClients());//des
								serverBean.setFileName(bean.getFileName());
								serverBean.setSize(bean.getSize());
								serverBean.setInfo(info);
								serverBean.setName(bean.getName());//from
								serverBean.setTimer(bean.getTimer());
								sendMessage(serverBean);
								break;
								}
							
							case 3://ȷ�������ļ�
							{
								ChatBean serverBean = new ChatBean();
								serverBean.setType(3);
								serverBean.setClients(bean.getClients());
								serverBean.setTo(bean.getTo());
								serverBean.setFileName(bean.getFileName());
								serverBean.setIp(bean.getIp());
								serverBean.setPort(bean.getPort());
								serverBean.setName(bean.getName());
								serverBean.setTimer(bean.getTimer());
								sendMessage(serverBean);
								break;
							}
							
							case 4:
							{
								ChatBean serverBean = new  ChatBean();
								serverBean.setType(4);
								serverBean.setClients(bean.getClients());
								serverBean.setTo(bean.getTo());
								serverBean.setFileName(bean.getFileName());
								serverBean.setInfo(bean.getInfo());
								serverBean.setName(bean.getName());
								serverBean.setTimer(bean.getTimer());
								sendMessage(serverBean);
								break;
							}
							
							case 5://chat
							{
								ChatBean serverBean = new ChatBean();
								serverBean.setType(5);
								serverBean.setClients(bean.getClients());
								serverBean.setInfo(bean.getInfo());
								serverBean.setName(bean.getName());
								serverBean.setTimer(bean.getTimer());
								// ��ѡ�еĿͻ���������
								sendAll(serverBean);
								//sendMessage(serverBean);
								break;
							}
							default:
								break;
							}
						}
				} catch (IOException e)
				{
					// TODO: handle exception
					e.printStackTrace();
				}catch (ClassNotFoundException e) {
					// TODO: handle exception
					e.printStackTrace();
				}finally{
					close();
				}
		}

	
	//��ѡ�е��û���������
	private void sendMessage(ChatBean serverBean)
	{
		Set<String> cbSet = onlines.keySet();
		Iterator<String> iterator = cbSet.iterator();
		
		HashSet<String> clients= serverBean.getClients();
		while (iterator.hasNext())
			{
				String client = iterator.next();
				//����ͻ���������
				if(clients.contains(client))
					{
						Socket aSocket = onlines.get(client).getSocket();
						ObjectOutputStream ooStream;
						try
							{
								ooStream = new ObjectOutputStream(aSocket.getOutputStream());
								ooStream.writeObject(serverBean);
								ooStream.flush();
							} catch (IOException e)
							{
								// TODO: handle exception
								e.printStackTrace();
							}
					}
				
			}
	}
	
	//�������û���������
	public void sendAll(ChatBean serverBean)
	{
		Collection<ClientBean> clients = onlines.values();
		Iterator<ClientBean> iterator = clients.iterator();
		ObjectOutputStream ooStream;
		
		while (iterator.hasNext())
			{
				Socket aSocket = iterator.next().getSocket();
				try
					{
						ooStream = new ObjectOutputStream(aSocket.getOutputStream());
						ooStream.writeObject(serverBean);
						ooStream.flush();
					} catch (IOException e)
					{
						// TODO: handle exception
						e.printStackTrace();
					}
			}
	}
	
	private void close()
	{
		if (ooStream!=null)
			{
				try
					{
						ooStream.close();
					} catch (IOException e)
					{
						// TODO: handle exception
						e.printStackTrace();
					}
			}
		if (oiStream!=null)
			{
				try
					{
						oiStream.close();
					} catch (IOException e)
					{
						// TODO: handle exception
						e.printStackTrace();
					}
			}
		if(client!=null)
			{
				try
					{
						client.close();
					} catch (IOException e)
					{
						// TODO: handle exception
						e.printStackTrace();
					}
			}
	}
}
	public void start()
	{
		try
			{
				while (true)
					{
						Socket client = serverSocket.accept();
						new ChatClientThread(client).start();
					}
			} catch (IOException e)
			{
				// TODO: handle exception
				e.printStackTrace();
			}
	}
	
	public static void main(String[] args)
	{
		new ChatServer().start();
	}
	{
		
	}
}
