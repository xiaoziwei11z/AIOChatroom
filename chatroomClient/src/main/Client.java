package main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import controller.ChatController;
import controller.DownloadController;
import controller.SelectController;
import controller.UploadController;

public class Client {
	public static final int PORT = 33333;
	public static final int DOWNPORT = 33334;
	public static final int UPPORT = 33335;
	public static AsynchronousSocketChannel socketChannel;
	public static AsynchronousSocketChannel downloadSocketChannel;
	public static AsynchronousSocketChannel selectSocketChannel;
	public static AsynchronousSocketChannel uploadSocketChannel;
	public static JFrame frame = new JFrame("聊天室");
	public static JTextArea textArea = new JTextArea();
	public static JTextField textField = new JTextField();
	public static JButton button = new JButton("发送");
	public static JPanel contentPane = new JPanel();
	public static JScrollPane scrollPane = new JScrollPane();
	public static JButton downButton = new JButton("下载文件");
	public static JButton upButton = new JButton("上传文件");
	
	public static void init(){
		frame.setLayout(new BorderLayout());
		frame.setBounds(100, 100, 552, 368);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//消息显示区域
		textArea.setEditable(false);
		scrollPane.setBounds(0, 0, 536, 256);
		contentPane.add(scrollPane);
		scrollPane.setViewportView(textArea);
		
		//下载文件
		downButton.setBounds(113, 264, 93, 23);
		downButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DownloadDialog().setVisible(true);
				downButton.setEnabled(false);
				upButton.setEnabled(false);
			}
		});
		contentPane.add(downButton);
		
		//上传文件
		upButton.setBounds(10, 264, 93, 23);
		upButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				downButton.setEnabled(false);
				upButton.setEnabled(false);
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int n = fileChooser.showDialog(frame,"请选择文件");
				if(n == JFileChooser.CANCEL_OPTION){
					downButton.setEnabled(true);
					upButton.setEnabled(true);
				}else{
					File upFile = fileChooser.getSelectedFile();
					UploadController.upload(upFile);
				}
			}
		});
		contentPane.add(upButton);
		
		//发送区域
		button.setBounds(433, 296, 93, 23);
		contentPane.add(button);
		textField.setBounds(10, 297, 403, 22);
		contentPane.add(textField);
		textField.setColumns(10);
		
		Action actionListener = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String content = textField.getText();
				if(content!=null && content.trim().length()>0){
					ChatController.sendText(content);
				}
				textField.setText("");
			}
		};
		button.addActionListener(actionListener);
		textField.getInputMap().put(KeyStroke.getKeyStroke('\n'), "send");
		textField.getActionMap().put("send", actionListener);
		
		frame.setLocation(700,250);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	public static void connect() throws Exception{
		ExecutorService executor = Executors.newFixedThreadPool(20);
		AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(executor);
		socketChannel = AsynchronousSocketChannel.open(group);
		downloadSocketChannel = AsynchronousSocketChannel.open(group);
		selectSocketChannel = AsynchronousSocketChannel.open(group);
		uploadSocketChannel = AsynchronousSocketChannel.open(group);
		
		socketChannel.connect(new InetSocketAddress("127.0.0.1", PORT)).get();
		selectSocketChannel.connect(new InetSocketAddress("127.0.0.1", DOWNPORT)).get();
		downloadSocketChannel.connect(new InetSocketAddress("127.0.0.1", DOWNPORT)).get();
		uploadSocketChannel.connect(new InetSocketAddress("127.0.0.1",UPPORT)).get();
		textArea.append("------与服务器连接成功------\n");

		ChatController.readText();
		SelectController.readList();
	}
	
	public static void main(String[] args) throws Exception {
		init();
		connect();
	}

}
