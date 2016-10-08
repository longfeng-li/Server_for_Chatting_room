import javax.swing.*;


import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.awt.*;
import java.awt.List;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
	Frame f;
	Panel top_left;
	Panel top_bot;
	Panel top;
	Panel bottom;
	TextArea content;
	TextField  txt_message;
	Button send;
	TextField hostIp;
	TextField hostPort;
	TextField max_thread;
	Button connect;
	Button disconnect;
	List onLineList;
	boolean isconnected = false;
	ArrayList<ClientThread> clients;
	ServerSocket serverSocket;
    serverThread serverThread;
	
	public static void main(String[] args) {
		new Server();
	}
	public void send() {
		if (!isconnected) {
			JOptionPane.showMessageDialog(f, "Server is not connected successfully and can't send message.", "error", JOptionPane
					.ERROR_MESSAGE);
			return;
		}
		if (clients.size() == 0) {
			JOptionPane.showMessageDialog(f, "There is no clients on line and can't send message", "error", JOptionPane
					.ERROR_MESSAGE);
		}
		String message = txt_message.getText().trim();
		if (message.length() == 0) {
			JOptionPane.showMessageDialog(f, "You can't send nothing", "error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		sendServerMessage(message);
		content.append("Server: " + txt_message.getText() + "\r\n");
		txt_message.setText(null);
	}
	public  Server() {
		f = new Frame("Server");
		f.setLayout(new BorderLayout());
		content = new TextArea();
		content.setEditable(false);
		txt_message = new TextField();
		send = new Button("send");
		hostIp = new TextField("127.0.0.1");
		hostPort = new TextField("6666");
		max_thread = new TextField("30");
		connect = new Button("connect");
		disconnect = new Button("disconnect");
		onLineList = new List();
		top_left = new Panel();
		top_left.setLayout(new BorderLayout());
		top_bot = new Panel();
		top_bot.setLayout(new BorderLayout());
		top_bot.add(txt_message, BorderLayout.CENTER);
		top_bot.add(send, BorderLayout.EAST);
		top_left.add(content, BorderLayout.NORTH);
		top_left.add(top_bot, BorderLayout.SOUTH);
		top = new Panel();
		top.setLayout(new BorderLayout());
		top.add(top_left, BorderLayout.WEST);
		top.add(onLineList, BorderLayout.EAST);
		bottom = new Panel();
		bottom.setLayout(new GridLayout(1, 5));
		bottom.add(hostIp);
		bottom.add(hostPort);
		bottom.add(max_thread);
		bottom.add(connect);
		bottom.add(disconnect);
		f.add(top, BorderLayout.NORTH);
		f.add(bottom, BorderLayout.SOUTH);
		f.pack();
		f.setVisible(true);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		txt_message.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				send();
			}
		});
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isconnected) {
					JOptionPane.showMessageDialog(f, "This server already started and can't retart", "error", JOptionPane
							.ERROR_MESSAGE);
					return;
				}
				int max;
				int port;
				try {
					try {
						max = Integer.parseInt(max_thread.getText());
					} catch(Exception e1) {
						throw new Exception("The number of client should be positive");
					}
					if (max <= 0) {
						throw new Exception("The number of client should be positive");
					}
					try {
						port = Integer.parseInt(hostPort.getText());
					} catch(Exception e2) {
						throw new Exception("The number of port should be positive");
					}
					if (port <= 0) {
						throw new Exception("The number of port should be positive");
					}
					serverstart(max, port);
					content.append("Server is successfully starting and the maximum number of clients is " + max + " port: "
							+ port + "\r\n");
					JOptionPane.showMessageDialog(f, "Server successfully starts");
					connect.setEnabled(false);
                    max_thread.setEnabled(false);
                    hostPort.setEnabled(false);
                    disconnect.setEnabled(true);
				} catch (Exception exc) {
                    JOptionPane.showMessageDialog(f, exc.getMessage(),
                            "error", JOptionPane.ERROR_MESSAGE);
                }
			}
		});
		disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isconnected) {
					JOptionPane.showMessageDialog(f, "Sever didn't start, so you don't need to turn it off", "error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				try {
                    closeServer();
                    connect.setEnabled(true);
                    max_thread.setEnabled(true);
                    hostPort.setEnabled(true);
                    send.setEnabled(false);
                    content.append("Server is shut down!\r\n");
                    JOptionPane.showMessageDialog(f, "Server successfully shut down！");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(f, "Stopping server error！", "error",
                            JOptionPane.ERROR_MESSAGE);
                }
			}
		});
	}
	public void serverstart(int max, int port) throws java.net.BindException {
        try {
            clients = new ArrayList<ClientThread>();
            serverSocket = new ServerSocket(port);
            serverThread = new serverThread(serverSocket, max);
            serverThread.start();
            isconnected = true;
        } catch (BindException e) {
            isconnected = false;
            throw new BindException("port is occupied！");
        } catch (Exception e1) {
            e1.printStackTrace();
            isconnected = false;
            throw new BindException("Error！");
        }
    }
	
	public void closeServer() {
        try {
            if (serverThread != null)
                serverThread.stop();// 停止服务器线程

            for (int i = clients.size() - 1; i >= 0; i--) {
                // 给所有在线用户发送关闭命令
                clients.get(i).getWriter().println("CLOSE");
                clients.get(i).getWriter().flush();
                // 释放资源
                clients.get(i).stop();
                clients.get(i).reader.close();
                clients.get(i).writer.close();
                clients.get(i).socket.close();
                clients.remove(i);
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
            isconnected = false;
        } catch (IOException e) {
            e.printStackTrace();
            isconnected = true;
        }
    }
	
	public void sendServerMessage(String message) {
		for (int i = clients.size()-1;i>=0;i--) {
			clients.get(i).getWriter().println("Server: " + message + "(send to multiple people)");
			clients.get(i).getWriter().flush();
		}
	}
	
	class serverThread extends Thread {
		ServerSocket serverSocket;
		int max;
		public serverThread(ServerSocket serverSocket, int max) {
			this.serverSocket = serverSocket;
			this.max = max;
		}
		public void run() {
			try {
				while (true) {
					Socket socket = serverSocket.accept();
					if (clients.size() == max) {
						BufferedReader r = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
						PrintWriter w = new PrintWriter(socket
								.getOutputStream());
						String inf = r.readLine();
						StringTokenizer st = new StringTokenizer(inf, "@");
						User user = new User(st.nextToken(), st.nextToken());
						w.println("MAX@server：sorry，" + user.getUserName()
							+ user.getUserIp() + "，get to the maximum number！");
						w.flush();
						r.close();
						w.close();
						socket.close();
						continue;
					}
					ClientThread client = new ClientThread(socket);
                    client.start();
                    clients.add(client);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class ClientThread extends Thread {
		Socket socket;
		BufferedReader reader;
		PrintWriter writer;
		User user;
		public BufferedReader getReader() {
            return reader;
        }

        public PrintWriter getWriter() {
            return writer;
        }
		public User getUser() {
			return user;
		}
		public ClientThread(Socket socket) {
			try {
				this.socket = socket;
				reader = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));
				writer = new PrintWriter(socket.getOutputStream());
				String inf = reader.readLine();
				StringTokenizer st = new StringTokenizer(inf, "@");
				user = new User(st.nextToken(), st.nextToken());
				writer.println(user.getUserName() + user.getUserIp() + "successfully connecte to server");
				writer.flush();
				for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println(
                            "ADD@" + user.getUserName() + user.getUserIp());
                    clients.get(i).getWriter().flush();
                }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void run() {
			String message = null;
			while (true) {
				try {
					message = reader.readLine();
					if (message.equals("CLOSE")) {
						content.append(this.getUser().getUserName() + this.getUser().getUserIp() + "out of line");
						reader.close();
						writer.close();
						socket.close();
						for (int i = clients.size() - 1; i >= 0; i--) {
                            if (clients.get(i).getUser() == user) {
                                ClientThread temp = clients.get(i);
                                clients.remove(i);
                                temp.stop();
                                return;
                            }
                        }
					} else {
						dispatcherMessage(message);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		public void dispatcherMessage(String message) {
            StringTokenizer stringTokenizer = new StringTokenizer(message, "@");
            String source = stringTokenizer.nextToken();
            String owner = stringTokenizer.nextToken();
            String contents = stringTokenizer.nextToken();
            message = source + "：" + contents;
            content.append(message + "\r\n");
            if (owner.equals("ALL")) {
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println(message + "(Send to Other people)");
                    clients.get(i).getWriter().flush();
                }
            }
        }
	}
}
