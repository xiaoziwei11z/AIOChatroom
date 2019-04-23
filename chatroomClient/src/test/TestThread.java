package test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestThread implements Runnable {
	private CountDownLatch countDownLatch;
	private int clientIndex;
	
	public TestThread(CountDownLatch countDownLatch,int clientIndex) {
		super();
		this.countDownLatch = countDownLatch;
		this.clientIndex = clientIndex;
	}

	@Override
	public void run() {
		try {
			ExecutorService executor = Executors.newFixedThreadPool(200);
			AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(executor);
			AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(group);
			socketChannel.connect(new InetSocketAddress("127.0.0.1", 33333)).get();
			
			//等待所有线程启动，然后一起发送请求
			countDownLatch.await();
			
			String content = "模拟线程" + clientIndex;
			socketChannel.write(ByteBuffer.wrap(content.getBytes("utf-8"))).get();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
