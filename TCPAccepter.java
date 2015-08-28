import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TCP Accepter thread class
 * For accepting incoming TCP connections from clients
 */
public class TCPAccepter extends Thread{
	private static String myHostName ;

	public TCPAccepter(String myHostName) {
		super();
		this.myHostName = myHostName;
	}

	/**
	 * Function for accepting incoming connections
	 * @throws IOException
	 */
	private void acceptConn() throws IOException{
		ServerSocket serverSoc = new ServerSocket(Node.myTcpPort) ;
		System.out.println("Server Receiver Thread started...accepting connections");
		int cnt=0;
		
		while(true){
			try{
				// for testing
				if(cnt==1)
					break;

				Socket clientSoc = serverSoc.accept() ;
				updateMyDegree() ;
				synchronized (Node.tcpSockets) {
					Node.tcpSockets.put(clientSoc.getInetAddress().getHostName(), clientSoc);

				}

				System.out.println("Accepted connection ....");

				synchronized (TCPSender.startDV) {

					TCPSender.startDV = 1;

				}

			}catch (IOException e) {
				System.out.println("\nConnection Failed !!");
				e.printStackTrace() ;
			}finally{

			}
		}
		serverSoc.close() ;
	}

	public void run(){
		try {
			acceptConn() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Function to update node's degree after it gets connected to another node
	 */
	public static void updateMyDegree(){
		synchronized(Node.routingTable){
			DistanceVector dvObj = Node.routingTable.get(myHostName) ;
			dvObj.setDegree(dvObj.getDegree()+1) ;
		}
	}
}
