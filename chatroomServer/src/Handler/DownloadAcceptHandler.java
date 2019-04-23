package Handler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class DownloadAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
	private AsynchronousServerSocketChannel fileServerSocketChannel = null;
	private String filePath = null;
	
	public DownloadAcceptHandler(AsynchronousServerSocketChannel fileServerSocketChannel) {
		super();
		this.fileServerSocketChannel = fileServerSocketChannel;
		Properties properties = new Properties();
		try {
			properties.load(this.getClass().getResourceAsStream("/conf/path.properties"));
			filePath = properties.getProperty("dirPath");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {
		fileServerSocketChannel.accept(null, this);
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.clear();
		socketChannel.read(buffer, null, new CompletionHandler<Integer, Object>() {
			@Override
			public void completed(Integer result, Object attachment) {
				buffer.flip();
				String content = StandardCharsets.UTF_8.decode(buffer).toString();
				if(content.equals("select")){
					File directory = new File(filePath);
					File[] fileList = directory.listFiles();
					for (File file : fileList) {
						if(file.isFile()){
							try {
								socketChannel.write(ByteBuffer.wrap(file.getName().getBytes("UTF-8"))).get();
								Thread.sleep(10);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				if(content.contains("download")){
					String fileName = content.substring(content.indexOf(":")+1);
					File file = new File(filePath + fileName);
					try {
						socketChannel.write(ByteBuffer.wrap((file.length()+"").getBytes("utf-8"))).get();
						AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(Paths.get(file.getAbsolutePath()));
						ByteBuffer fileBuffer = ByteBuffer.allocate(1024);
						fileBuffer.clear();
						fileChannel.read(fileBuffer, (long)0,(long)0, new CompletionHandler<Integer, Long>() {
							@Override
							public void completed(Integer result, Long attachment) {
								fileBuffer.flip();
								try {
									socketChannel.write(fileBuffer).get();
								} catch (Exception e) {
									e.printStackTrace();
								}
								attachment += result;
								if(attachment>=file.length()){
									return;
								}
								fileBuffer.clear();
								fileChannel.read(fileBuffer, attachment, attachment, this);
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
				buffer.clear();
				socketChannel.read(buffer, null, this);
			}

			@Override
			public void failed(Throwable exc, Object attachment) {
				System.out.println(exc);
			}
		});
		
	}

	@Override
	public void failed(Throwable exc, Object attachment) {
		System.out.println(exc);
	}
}
