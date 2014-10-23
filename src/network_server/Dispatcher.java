package network_server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Dispatcher
		implements
		CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {
	
	//search engines maximum url size
	private final int bufferSize = 2048;
	
	Router router = new Router();

	@Override
	public void completed(AsynchronousSocketChannel channel,
			AsynchronousServerSocketChannel listener) {
		// listener로 받아와서 connect 되면 channel이 생긴다.
		listener.accept(listener, this);
		System.out.println("Client connected.");
		
		router.initialize(channel);

		ByteBuffer buffer = ByteBuffer.allocate(bufferSize);		
		channel.read(buffer, buffer, router);
	}

	@Override
	public void failed(Throwable exc, AsynchronousServerSocketChannel attachment) {
		// TODO Auto-generated method stub
	}
}
