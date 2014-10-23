package network_server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router implements CompletionHandler<Integer, ByteBuffer> {
	private AsynchronousSocketChannel channel;
	private HashMap<String, String> headerMap = new HashMap<String, String>();
	private String httpRequest;
	private String requestHeader;
	private boolean headerFinish = false;
	private String requestBody;
	private boolean bodyFinish = false;

	private final int bufferSize = 2048;

	public void initialize(AsynchronousSocketChannel channel) {
		this.channel = channel;
		this.headerMap = new HashMap<String, String>();
		this.httpRequest = "";
		this.requestHeader = "";
		this.headerFinish = false;
		this.requestBody = "";
		this.bodyFinish = false;
	}

	@Override
	public void completed(Integer result, ByteBuffer buffer) {
		if (result == -1) {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (result > 0) {
			// reset buffer
			buffer.flip();

			mergeRequest(buffer);

			String[] requestLines = this.httpRequest.split(System
					.getProperty("line.separator"));

			checkRequest(requestLines);

			if (this.headerFinish && this.bodyFinish) {
				routing(requestLines);
			} else {
				ByteBuffer newBuffer = ByteBuffer.allocate(bufferSize);
				channel.read(newBuffer, newBuffer, this);
			}
		}
	}

	private void routing(String[] requestLines) {
		String method = requestLines[0].split(" ")[0];

		routingByMethod(method);

		try {
			System.out.println("Connection closed.");
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void routingByMethod(String method) {
		switch (method) {
		case "GET": {
			GetRequestHandler handler = new GetRequestHandler(
					this.channel, headerMap, requestHeader);
			handler.routingByUrl();
		}
			break;

		case "POST": {
			PostRequestHandler handler = new PostRequestHandler(
					this.channel, headerMap, requestHeader, requestBody);
			handler.routingByUrl();
		}
			break;
		}
	}

	private void mergeRequest(ByteBuffer buffer) {
		StringBuffer requestBuffer = new StringBuffer();
		requestBuffer.append(this.httpRequest);
		requestBuffer.append(new String(buffer.array()));
		this.httpRequest = requestBuffer.toString().trim();
	}

	private void checkRequest(String[] requestLines) {
		if (!this.headerFinish) {
			checkHeaderFinish(requestLines);
		}
		makeHeader(requestLines);
		mappingHeader(requestLines);

		if (requestLines[0].split(" ")[0].equals("POST")) {
			if (!this.bodyFinish) {
				checkBodyFinish(requestLines);
			}
			if (this.headerMap.get("Content-Type").equals("application/json")) {
				makeBody(requestLines);
			}
		} else {
			this.bodyFinish = true;
		}
	}

	private void checkHeaderFinishForGET(String[] requestLines) {
		String lastLine = requestLines[requestLines.length - 1];

		if (!lastLine.matches("\\\\r")) {
			this.headerFinish = true;
		} else {
			this.headerFinish = false;
		}
	}

	private void checkHeaderFinish(String[] requestLines) {
		if (requestLines[0].split(" ")[0].equals("GET")) {
			checkHeaderFinishForGET(requestLines);
		} else {
			for (int i = 0; i < requestLines.length; i++) {
				if (requestLines[i].equals("\r")) {
					this.headerFinish = true;
					break;
				}
				this.headerFinish = false;
			}
		}
	}

	private void makeHeader(String[] requestLines) {
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < requestLines.length; i++) {
			if (requestLines[i].equals("\r")) {
				break;
			}
			buffer.append(requestLines[i] + "\n");
		}

		this.requestHeader = buffer.toString().trim();
	}

	private void mappingHeader(String[] requestLines) {
		for (int i = 1; i < requestLines.length; i++) {
			if (requestLines[i].equals("\r")) {
				break;
			}
			String attribute = requestLines[i].split(": ")[0];
			String value = requestLines[i].split(": ")[1];

			if (!this.headerMap.containsKey(attribute)) {
				this.headerMap.put(attribute, value);
			}
		}
	};

	private void checkBodyFinish(String[] requestLines) {
		int headerContentLength = getContentLength();
		int receiveContentLength = getRequestBody(requestLines).getBytes().length;

		if (headerContentLength == receiveContentLength) {
			this.bodyFinish = true;
		} else {
			this.bodyFinish = false;
		}
	}

	private void makeBody(String[] requestLines) {
		StringBuffer buffer = new StringBuffer();
		int bodyIndex = 0;

		for (int i = 0; i < requestLines.length; i++) {
			if (requestLines[i].equals("\r")) {
				bodyIndex = i;
			}
		}

		for (int i = bodyIndex; i < requestLines.length; i++) {
			buffer.append(requestLines[i] + "\n");
		}

		this.requestBody = buffer.toString().trim();
	}

	private int getContentLength() {
		if (this.headerMap.containsKey("Content-Length")) {
			Pattern intsOnly = Pattern.compile("\\d+");
			Matcher makeMatch = intsOnly.matcher(this.headerMap
					.get("Content-Length"));
			makeMatch.find();

			int contentLength = Integer.parseInt(makeMatch.group());

			return contentLength;
		} else {
			return 0;
		}
	}

	private String getRequestBody(String[] requestLines) {
		String requestBody = null;
		int bodyIndex = 0;

		for (int i = 0; i < requestLines.length; i++) {
			if (requestLines[i].equals("\r")) {
				bodyIndex = i;
			}
		}

		StringBuffer buffer = new StringBuffer();

		for (int i = bodyIndex; i < requestLines.length; i++) {
			buffer.append(requestLines[i] + "\n");
		}

		requestBody = buffer.toString().trim();

		return requestBody;
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		// TODO Auto-generated method stub
	}
}
