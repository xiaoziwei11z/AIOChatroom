package controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;

import main.Client;

public class DownloadController {

	public static void readFile(String fileName,String path) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		Client.downloadSocketChannel.read(buffer, null,new CompletionHandler<Integer, Object>() {
			@Override
			public void completed(Integer result, Object attachment) {
				try {
					buffer.flip();
					String fileLength = StandardCharsets.UTF_8.decode(buffer).toString();
					String filePath = path + "/" + fileName;
					File file = new File(filePath);
					int i=1;
					String newPath = null;
					while(file.exists()){
						newPath = filePath;
						newPath = filePath.substring(0,filePath.lastIndexOf(".")) + "(" + i + ")"
									+ filePath.substring(filePath.lastIndexOf("."));
						file = new File(newPath);
						i++;
					}
					file.createNewFile();
					Client.textArea.append("系统提示：开始文件下载\n");
					
					ByteBuffer fileBuffer = ByteBuffer.allocate(1024);
					AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open
							(Paths.get(file.getAbsolutePath()), StandardOpenOption.WRITE);
					Client.downloadSocketChannel.read(fileBuffer, (long)0, new CompletionHandler<Integer, Long>() {
						@Override
						public void completed(Integer result, Long attachment) {
							fileBuffer.flip();
							try {
								fileChannel.write(fileBuffer, attachment).get();
							} catch (Exception e) {
								e.printStackTrace();
							}
							attachment += result;
							if(attachment >= Long.parseLong(fileLength)){
								Client.textArea.append("系统提示：文件下载完成！文件大小：" + fileLength + "B\n");
								Client.downButton.setEnabled(true);
								Client.upButton.setEnabled(true);
								return;
							}
							
							fileBuffer.clear();
							Client.downloadSocketChannel.read(fileBuffer, attachment, this);
						}
						@Override
						public void failed(Throwable exc, Long attachment) {
							System.out.println(exc);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			@Override
			public void failed(Throwable exc, Object attachment) {
				System.out.println(exc);
			}
		});
	}

	public static void download(String fileName) {
		try {
			Client.downloadSocketChannel.write(ByteBuffer.wrap(("download:"+fileName).getBytes("utf-8"))).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
