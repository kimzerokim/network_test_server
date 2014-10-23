package network_server;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;
import java.util.Set;

public class GetRequestHandler {
	private AsynchronousSocketChannel channel;
	private String method = "GET";
	private String url;
	private HashMap<String, String> headerMap = new HashMap<String, String>();
	private String requestHeader;
	private HashMap<String, String> queryMap = new HashMap<String, String>();

	public GetRequestHandler(AsynchronousSocketChannel channel,
			HashMap<String, String> headerMap, String requestHeader) {
		this.channel = channel;
		this.headerMap = headerMap;
		this.requestHeader = requestHeader;
		this.url = requestHeader.split(System.getProperty("line.separator"))[0]
				.split(" ")[1].substring(1);
	}

	public void routingByUrl() {
		if (url.equals("getfile")) {
			BigfileHandler handler = new BigfileHandler(this.channel);
			try {
				handler.fileSend();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		setQueryMap(url);
	}

	private void setQueryMap(String url) {
		String[] params = url.split("&");
		if (params.length != 1) {
			for (String param : params) {
				String name = param.split("=")[0];
				String value = param.split("=")[1];
				this.queryMap.put(name, value);
			}
			getQueryMap();
		}
	}

	private void getQueryMap() {
		Set<String> keys = this.queryMap.keySet();
		for (String key : keys) {
			System.out.println("Name=" + key);
			System.out.println("Value=" + this.queryMap.get(key));
		}
	}

}
