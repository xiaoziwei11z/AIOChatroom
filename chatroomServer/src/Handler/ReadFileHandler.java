package Handler;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class ReadFileHandler implements CompletionHandler<Integer, Object> {
	private ByteBuffer byteBuffer;
	private AsynchronousSocketChannel socketChannel;
	private static String dirPath = null;
	
	public ReadFileHandler(ByteBuffer byteBuffer, AsynchronousSocketChannel socketChannel) {
		super();
		this.byteBuffer = byteBuffer;
		this.socketChannel = socketChannel;
		Properties properties = new Properties();
		try {
			properties.load(this.getClass().getResourceAsStream("/conf/path.properties"));
			dirPath = properties.getProperty("dirPath");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void completed(Integer result, Object attachment) {
		try {
			byteBuffer.flip();
			String content = StandardCharsets.UTF_8.decode(byteBuffer).toString();
			String fileName = content.substring(0,content.lastIndexOf(","));
			String fileLength = content.substring(content.lastIndexOf(",")+1);
			String path = dirPath + fileName;
			File file = new File(path);
			int i=1;
			String newPath = path;
			while(file.exists()){
				newPath = path;
				newPath = path.substring(0,path.lastIndexOf(".")) + "(" + i + ")"
							+ path.substring(path.lastIndexOf("."));
				file = new File(newPath);
				i++;
			}
			String absolutePath = newPath;
			
			file.createNewFile();
			AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open
					(Paths.get(absolutePath),StandardOpenOption.WRITE);
			
			ByteBuffer fileBuffer = ByteBuffer.allocate(1024);
			//开始接收文件
			socketChannel.read(fileBuffer, (long)0, new CompletionHandler<Integer, Long>() {
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
							try {
								socketChannel.write(ByteBuffer.wrap("over".getBytes("utf-8"))).get();
							} catch (Exception e) {
								e.printStackTrace();
							}
							byteBuffer.clear();
							socketChannel.read(byteBuffer, null, new ReadFileHandler(byteBuffer, socketChannel));
							return;
						}
						
						fileBuffer.clear();
						socketChannel.read(fileBuffer, attachment, this);
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

}
