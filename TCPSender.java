import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * TCP Sender thread class
 */
public class TCPSender extends Thread{
	private String myHostName;
	public static Integer startDV = 1 ; 

	public TCPSender(String myHostName) {
		super();
		this.myHostName = myHostName;
	}

	/**
	 * Function where node sends its own routing table as part of Distance Vector protocol
	 * when a change in the network is detected
	 */
	public void run(){

		Integer compareDV = 1 ;

		while(true){

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}


			boolean flag=false;
			synchronized (TCPSender.startDV){
				if(TCPSender.startDV.equals(compareDV)){
					//System.out.println("Comparison passed...");
					flag=true;
				}

			}
			if(flag){

				HashMap<String, DistanceVector> myDV = null;

				synchronized (Node.routingTable) {					
					myDV=Node.routingTable;
				}

				synchronized (Node.tcpSockets) {

					Iterator<Entry<String, Socket>>  it= Node.tcpSockets.entrySet().iterator();

					while(it.hasNext()){

						Map.Entry<String, Socket> pairs=(Map.Entry<String, Socket>)it.next();

						Message m=null;
						m = new Message("RoutingTable",myHostName,"Hello",myDV);
						DataOutputStream outToServer;
						try {
							outToServer = new DataOutputStream(pairs.getValue().getOutputStream());
							outToServer.write(Message.serialize(m));
							System.out.println("TCPSender:Message sent successfully .");
						} catch (IOException e) {
							e.printStackTrace();
						}


					}		
				}
				printDV() ;
			}

			synchronized (TCPSender.startDV) {
				TCPSender.startDV = 0 ;	
			}			

		}
	}

	/**
	 * Function to display current routing table information on screen
	 */
	public void printDV(){

		System.out.println("\n\nRouting Table::");

		synchronized (Node.routingTable) {
			Set<Entry<String, DistanceVector>> set = Node.routingTable.entrySet();
			Iterator<Entry<String, DistanceVector>> i = set.iterator();
			while(i.hasNext())
			{
				Map.Entry<String,DistanceVector> me=(Map.Entry<String, DistanceVector>)i.next();
				System.out.println("--------------------------------------------------------------------------------");
				System.out.println("	Node- "+me.getKey());
				System.out.println("	Destination	  NextHop	  Cost  Degree");
				System.out.println("   "+me.getValue().getDestination()+"  "+me.getValue().getNextHop()+"  "+me.getValue().getCost()+"    "+me.getValue().getDegree());
			}
			System.out.println();
		}
	}
}

