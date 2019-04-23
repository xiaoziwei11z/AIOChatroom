package Handler;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class UploadAcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
	private AsynchronousServerSocketChannel upServerSocketChannel;
	
	public UploadAcceptHandler(AsynchronousServerSocketChannel upServerSocketChannel) {
		super();
		this.upServerSocketChannel = upServerSocketChannel;
	}

	@Override
	public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {
		upServerSocketChannel.accept(null,this);
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		socketChannel.read(byteBuffer, null, new ReadFileHandler(byteBuffer, socketChannel));
	}

	@Override
	public void failed(Throwable exc, Object attachment) {
		System.out.println(exc);
	}
}
