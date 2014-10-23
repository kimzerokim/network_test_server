package network_server;

public class ServerInitializer {
	private static int PORT = 5000;

	public static void main(String[] args) {
		System.out.println("Server ON : " + PORT);
		ProactorServer server = new ProactorServer(PORT);
		server.startServer();
	}
}
