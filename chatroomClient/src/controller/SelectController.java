package controller;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import main.Client;
import main.DownloadDialog;

public class SelectController {

	public static void readList() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		Client.selectSocketChannel.read(buffer, null, new CompletionHandler<Integer, Object>() {
			@Override
			public void completed(Integer result, Object attachment) {
				buffer.flip();
				String fileName = StandardCharsets.UTF_8.decode(buffer).toString();
				DownloadDialog.model.addElement(fileName);
				buffer.clear();
				Client.selectSocketChannel.read(buffer, null, this);
			}
			@Override
			public void failed(Throwable exc, Object attachment) {
				System.out.println(exc);
			}
		});
	}

	public static void send() {
		try {
			Client.selectSocketChannel.write(ByteBuffer.wrap("select".getBytes("UTF-8"))).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
