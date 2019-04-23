package controller;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import main.Client;

public class ChatController {

	public static void sendText(String content) {
		try {
			//通过channel传输消息到服务端
			Client.socketChannel.write(ByteBuffer.wrap(content.getBytes("utf-8"))).get();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String date = sdf.format(new Date());
			Client.textArea.append("我 " + date + " : " + content + "\n");
		} catch (UnsupportedEncodingException | InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}
	}

	public static void readText() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		//读取服务端传来的消息
		Client.socketChannel.read(buffer, null, new CompletionHandler<Integer, Object>() {
			@Override
			public void completed(Integer arg0, Object arg1) {
				buffer.flip();
				String content = StandardCharsets.UTF_8.decode(buffer).toString();
				Client.textArea.append(content);
				buffer.clear();
				Client.socketChannel.read(buffer, null, this);
			}

			@Override
			public void failed(Throwable arg0, Object arg1) {
				System.out.println("读取数据失败" + arg0);
			}
		});
	}

}
