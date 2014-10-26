package network_server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class BigfileHandler implements CompletionHandler<Integer, ByteBuffer> {
	private AsynchronousSocketChannel channel;

	private final int bufferSize = 2048;
	private RandomAccessFile file;
	private FileChannel fileChannel;
	private long fileSize = 0;
	private long fileCount = 0;
	private long curCount = 0;
	private long fileLeft = 0;

	private HashMap<String, String> headerMap = new HashMap<String, String>();

	public BigfileHandler(AsynchronousSocketChannel channel) {
		this.channel = channel;
		try {
			this.file = new RandomAccessFile("addNum.go", "r");
			// this.file = new RandomAccessFile(
			// "Breaking.Bad.S01E01.720p.HDTV.x264-BiA.mkv", "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.fileChannel = this.file.getChannel();
		try {
			this.fileSize = this.fileChannel.size();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.fileCount = this.fileSize / this.bufferSize;
		this.fileLeft = this.fileSize % this.bufferSize;
		initResponseHeader();
	}

	private void initResponseHeader() {
		// this.headerMap.put("status", "HTTP/1.1 200 OK");
		this.headerMap.put("Date", getServerTime());
		this.headerMap.put("Server", "Young");
		this.headerMap.put("Content-Type", "application/json");
		this.headerMap.put("Content-Length", Long.toString(this.fileSize));
	};

	private String getServerTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));
		return dateFormat.format(calendar.getTime());
	}

	public void sendHeader() {
		String header;
		StringBuffer buffer = new StringBuffer();

		buffer.append("HTTP/1.1 200 OK\r\n");

		Set<String> keys = this.headerMap.keySet();
		for (String key : keys) {
			buffer.append(key);
			buffer.append(": ");
			buffer.append(this.headerMap.get(key));
			buffer.append("\r\n");
		}

		header = buffer.toString();

		ByteBuffer headerBuffer = ByteBuffer.allocate(header.getBytes().length);
		headerBuffer.put(header.getBytes());

		this.channel.write(headerBuffer, headerBuffer, this);
	}

	public void fileSend() throws IOException {
		if (this.curCount < this.fileCount) {
			ByteBuffer buffer = ByteBuffer.allocate(this.bufferSize);
			this.fileChannel.read(buffer);
			buffer.flip();
			this.curCount++;
			this.channel.write(buffer, buffer, this);
		} else if (this.curCount == this.fileCount && this.fileLeft == 0) {
			return;
		} else if (this.curCount == this.fileCount && this.fileLeft != 0) {
			ByteBuffer buffer = ByteBuffer.allocate((int) this.fileLeft);
			this.fileChannel.read(buffer);
			buffer.flip();
			this.fileLeft = 0;
			this.channel.write(buffer, buffer, this);
			this.fileChannel.close();
			this.file.close();
		}
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
			try {
				fileSend();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		// TODO Auto-generated method stub
	}
}
