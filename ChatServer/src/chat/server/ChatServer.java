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
	
	static{//static{}(即static块)，会在类被加载的时候执行且仅会被执行一次，
			//一般用来初始化静态变量和调用静态方法。
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
			// TODO 自动生成的构造函数存根
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
							case 0://上线
							{
								ClientBean cBean = new ClientBean();
								cBean.setName(bean.getName());
								cBean.setSocket(client);
								onlines.put(bean.getName(), cBean);
								ChatBean serverBean = new ChatBean();
								serverBean.setType(0);
								serverBean.setInfo(bean.getTimer()+" "+bean.getName()+"上线了 ");
								HashSet<String> set = new HashSet<String>();
								set.addAll(onlines.keySet());
								serverBean.setClients(set);
								sendAll(serverBean);
								break;
							}
							case -1://下线
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
							//向剩下的人发送离线通知
							ChatBean serverBean_1 = new ChatBean();
							serverBean_1.setInfo(bean.getTimer() + "  "
									+ bean.getName() + " " + " 下线了");
							serverBean_1.setType(0);
							HashSet<String> set = new HashSet<String>();
							set.addAll(onlines.keySet());
							serverBean_1.setClients(set);
							sendAll(serverBean_1);
							return;
							}
							
							case 1://聊天
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
							
							case 2://请求接受文件
							{
								ChatBean serverBean = new ChatBean();
								String info = bean.getTimer() + "  " + bean.getName()
								+ "向你传送文件,是否需要接受";
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
							
							case 3://确定接收文件
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
								// 向选中的客户发送数据
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

	
	//向选中的用户发送数据
	private void sendMessage(ChatBean serverBean)
	{
		Set<String> cbSet = onlines.keySet();
		Iterator<String> iterator = cbSet.iterator();
		
		HashSet<String> clients= serverBean.getClients();
		while (iterator.hasNext())
			{
				String client = iterator.next();
				//如果客户在线则发送
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
	
	//向所有用户发送数据
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
