package Handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import server.AIOServer;

public class ChatAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
	private AsynchronousServerSocketChannel serverSocketChannel = null;

	public ChatAcceptHandler(AsynchronousServerSocketChannel serverSocketChannel) {
		super();
		this.serverSocketChannel = serverSocketChannel;
	}

	@Override
	public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {
		// 准备接收下次客户端连接
		serverSocketChannel.accept(null, this);
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		// 记录新连接的socketChannel
		AIOServer.channelList.add(socketChannel);
		socketChannel.read(buffer, null, new CompletionHandler<Integer, Object>() {
			// 实际IO操作完成时触发
			@Override
			public void completed(Integer result, Object attachment) {
				buffer.flip();
				String content = null;
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
					String date = sdf.format(new Date());
					content = socketChannel.getRemoteAddress().toString() + "  " + date + " : "
							+ StandardCharsets.UTF_8.decode(buffer).toString();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				// 将信息回写入各个channel
				for (AsynchronousSocketChannel socket : AIOServer.channelList) {
					if (socket != socketChannel) {
						try {
							final String finalContent=content;
							//向任务队列线程添加执行任务
							AIOServer.processor.post(new Runnable() {
								@Override
								public void run() {
									try {
										if(socket.isOpen()){
											socket.write(ByteBuffer.wrap((finalContent+"\n").getBytes("utf-8"))).get();
										}
									} catch (Exception e) {
										AIOServer.channelList.remove(socket);
									}
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				buffer.clear();
				// 读取下次数据
				socketChannel.read(buffer, null, this);
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				AIOServer.channelList.remove(socketChannel);
			}
		});
	}

	@Override
	public void failed(Throwable exc, Object attachment) {
		System.out.println("连接失败：" + exc);
	}

}
