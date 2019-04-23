package server;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Handler.ChatAcceptHandler;
import Handler.DownloadAcceptHandler;
import Handler.UploadAcceptHandler;

public class AIOServer {
	static final int PORT = 33333;
	static final int DOWNPORT = 33334;
	static final int UPPORT = 33335;
	public static List<AsynchronousSocketChannel> channelList = new CopyOnWriteArrayList<AsynchronousSocketChannel>();
	//任务队列
	public static TaskThread processor = new TaskThread();
	
	public static void listen() throws Exception{
		//以线程池的方式创建serverSocketChannel
		ExecutorService executor = Executors.newFixedThreadPool(80);
		AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(executor);
		AsynchronousServerSocketChannel serverSocketChannel = AsynchronousServerSocketChannel.open(group);
		AsynchronousServerSocketChannel downServerSocketChannel = AsynchronousServerSocketChannel.open(group);
		AsynchronousServerSocketChannel upServerSocketChannel = AsynchronousServerSocketChannel.open(group);
		
		processor.start();
		//监听本机端口
		serverSocketChannel.bind(new InetSocketAddress("0.0.0.0", PORT));
		serverSocketChannel.accept(null,new ChatAcceptHandler(serverSocketChannel) );
		downServerSocketChannel.bind(new InetSocketAddress("0.0.0.0", DOWNPORT));
		downServerSocketChannel.accept(null,new DownloadAcceptHandler(downServerSocketChannel));
		upServerSocketChannel.bind(new InetSocketAddress("0.0.0.0",UPPORT));
		upServerSocketChannel.accept(null, new UploadAcceptHandler(upServerSocketChannel));
	}
	
	public static void main(String[] args) throws Exception {
		listen();
		while(true){
			Thread.sleep(2000);
		}
	}
}
