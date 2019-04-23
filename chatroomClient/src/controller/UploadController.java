package controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import main.Client;

public class UploadController {

	public static void upload(File upFile) {
		String fileName = upFile.getName();
		Long fileLength = upFile.length();
		
		String msg = fileName + "," + fileLength;
		try {
			Client.uploadSocketChannel.write(ByteBuffer.wrap(msg.getBytes("utf-8"))).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Client.textArea.append("系统提示：开始上传文件\n");
		try {
			AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(upFile.getAbsolutePath()));
			ByteBuffer fileBuffer = ByteBuffer.allocate(1024);
			fileChannel.read(fileBuffer, (long)0, (long)0, new CompletionHandler<Integer, Long>() {
				@Override
				public void completed(Integer result, Long attachment) {
					try {
						fileBuffer.flip();
						Client.uploadSocketChannel.write(fileBuffer).get();
						attachment += result;
						
						if(attachment >= fileLength){
							return;
						}
						
						fileBuffer.clear();
						fileChannel.read(fileBuffer, attachment, attachment, this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				@Override
				public void failed(Throwable exc, Long attachment) {
					System.out.println(exc);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ByteBuffer resultBuffer = ByteBuffer.allocate(1024);
		Client.uploadSocketChannel.read(resultBuffer, null, new CompletionHandler<Integer, Object>() {
			@Override
			public void completed(Integer result, Object attachment) {
				Client.textArea.append("系统提示：文件上传成功!文件大小：" + fileLength +"B\n");
				Client.downButton.setEnabled(true);
				Client.upButton.setEnabled(true);
			}
			@Override
			public void failed(Throwable exc, Object attachment) {
				System.out.println(exc);
			}
		});
	}
}
