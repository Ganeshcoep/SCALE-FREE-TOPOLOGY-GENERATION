import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * UDP listener class to listens to queries over UDP
 * and send required response
 */
public class UDPListener extends Thread{
	private static String myHostName;

	UDPListener(String myHostName){
		this.myHostName = myHostName ;
	}

	public void run(){
		int breaker = 0 ;
		DatagramSocket serverSocket = null;
		try {
			serverSocket = new DatagramSocket(Node.myUdpPort);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		while(true){
			byte[] receiveData = new byte[1024];
			byte[] sendData = new byte[1024];
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				serverSocket.receive(receivePacket);

				String query = new String( receivePacket.getData());
				query = query.trim() ;

				System.out.println("RECEIVED QUERY : " + query);
				InetAddress IPAddress = receivePacket.getAddress();
				int port = receivePacket.getPort();
				Message msg = null ;

				switch(query){

				case "QUERY_ROUTING_TABLE":
					synchronized(Node.routingTable){
						msg = new Message("RoutingTable",myHostName,"Hello",Node.routingTable) ;

					}
					break;

				case "JOIN_REQ":

					synchronized(Node.routingTable){

						if(Node.routingTable.size()==Node.n)
							msg = new Message("REJECT",myHostName,"Reject",null) ;
						else 
							msg = new Message("RoutingTable",myHostName,"Hello",Node.routingTable) ;

					}
					break;

				case "QUERY_DEGREE":
					synchronized(Node.routingTable){

						msg = new Message("DegreeInfo",myHostName,Integer.valueOf(Node.routingTable.get(myHostName).getDegree()).toString(),null) ;
					}
					break;

				case "QUERY_FARTHEST_NODES":
					synchronized(Node.routingTable){
						String far = findMaxCost() ;
						msg = new Message("FarthestNode",myHostName,far,null) ;
					}
					break;

				default:
					System.out.println("\nInvalid Query!!");				
				}
				query = "" ;
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
                ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
                os.flush();
                os.writeObject((Object)msg);
                os.flush();
                //retrieves byte array
                byte[] sendBuf = byteStream.toByteArray();
                
                DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, IPAddress, port);
                serverSocket.send(sendPacket);
                os.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
			if(breaker == 1)
				break ;
		}
		serverSocket.close() ;
	}

	/**
	 * Function to find the farthest node from the queried node
	 * i.e finding node with max cost
	 * @return hostname of the farthest node
	 */
	private String findMaxCost() {
		int max = -1;
		String far = "\n";

		synchronized (Node.routingTable) {
			Set<Entry<String, DistanceVector>> set = Node.routingTable.entrySet();
			Iterator<Entry<String, DistanceVector>> i = set.iterator();
			while(i.hasNext())
			{
				Map.Entry<String,DistanceVector> me=(Map.Entry<String, DistanceVector>)i.next();
				if(me.getValue().getCost() > max){
					max = me.getValue().getCost() ;
					far = "\n" ;
					far = far.concat(me.getValue().getDestination()) ;
				}
				else if(me.getValue().getCost() == max){
					far = far.concat("\n") ;
					far = far.concat(me.getValue().getDestination()) ; 
				}
			}
		}
		return far ;
	}
}
