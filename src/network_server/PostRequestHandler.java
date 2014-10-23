package network_server;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;

public class PostRequestHandler {
	private AsynchronousSocketChannel channel;
	private String method = "PUT";
	private String url;
	private HashMap<String, String> headerMap = new HashMap<String, String>();
	private String requestHeader;
	private String requestBody;

	public PostRequestHandler(AsynchronousSocketChannel channel,
			HashMap<String, String> headerMap, String requestHeader,
			String requestBody) {
		this.channel = channel;
		this.headerMap = headerMap;
		this.requestHeader = requestHeader;
		this.requestBody = requestBody;
		this.url = requestHeader.split(System.getProperty("line.separator"))[0]
				.split(" ")[1].substring(1);
	}

	public void routingByUrl() {
		System.out.println(requestBody);
	}
}
