package network_server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProactorServer {
	// default port = 80
	private int port = 80;
	private final static int totalThreadPoolSize = 10;
	private final static int asyncThreadPoolSize = 7;
	// maximum number of pending connection
	private final static int backlog = 20;

	public ProactorServer(int port) {
		this.port = port;
	}

	public void startServer() {
		ExecutorService executor = Executors
				.newFixedThreadPool(totalThreadPoolSize);

		try {
			// executor에서 생성한 thread를 이용하여 AsynchronousChannelGroup에서 사용할
			// thread를 할당한다.
			AsynchronousChannelGroup group = AsynchronousChannelGroup
					.withCachedThreadPool(executor, asyncThreadPoolSize);

			// 스트림 지향의 리스닝 소켓을 위한 비동기 채널
			AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel
					.open(group);
			listener.bind(new InetSocketAddress(port), backlog);

			// 접속의 결과를 CompletionHandler으로 비동기 IO작업에 콜백 형식으로 작업 결과를 받는다.
			listener.accept(listener, new Dispatcher());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
