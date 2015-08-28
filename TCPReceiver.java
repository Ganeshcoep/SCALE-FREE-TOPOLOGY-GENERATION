import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * TCP Receiver thread class
 */
public class TCPReceiver extends Thread {
	TCPReceiver(){

	}
	
	/**
	 * Function to receive message i.e. receive message with routing table details sent by TCP sender 
	 */
	private void receiveMessage(){

		Integer compareDV = 0 ;
		boolean startDVFlag=false;

		synchronized (TCPSender.startDV) {
			if(TCPSender.startDV.equals(compareDV))
				startDVFlag=true;
		}

		if(startDVFlag){	
			synchronized (Node.tcpSockets) {

				Iterator<Entry<String, Socket>>  it = Node.tcpSockets.entrySet().iterator();

				while(it.hasNext()){
					Map.Entry<String, Socket> pairs = (Map.Entry<String, Socket>)it.next();
					//pairs.getValue().
					DataInputStream in;
					Message m1=null;//new Message();

					try {
						in = new DataInputStream(pairs.getValue().getInputStream());
						//in.readLine();
						byte[] b=new byte[2000];
						in.read(b);
						System.out.println("Received successfully!!");
						try {
							m1=(Message) Message.deserialize(b);
							processMessage(m1);
							//System.out.println("out of Process Message!!");
						} catch (IOException e) {
							e.printStackTrace();
						}finally{

						}
					}catch(Exception e){
						e.printStackTrace() ;
					}

				}
			}
		}

	}


	/**
	 * Function to process the received message(routing table)  
	 * If received message has updated routing table, node updates it own copy of routing table 
	 * as per the newly received routing table
	 * @param m, message received
	 */
	private void processMessage(Message m) {
		//System.out.println("entering Process Message!!");

		int flag=0;

		Iterator<Entry<String,DistanceVector>>  it= m.getRoutingTable().entrySet().iterator();

		while(it.hasNext()){	

			Map.Entry<String,DistanceVector> me=(Map.Entry<String, DistanceVector>)it.next();

			synchronized (Node.routingTable) {

				if(Node.routingTable.containsKey(me.getKey())){
					//comparing received cost with self table entry cost
					if((me.getValue().getCost()+1)< Node.routingTable.get(me.getKey()).getCost()){
						me.getValue().setCost(me.getValue().getCost()+1);
						me.getValue().setNextHop(m.getMsgSender());
						Node.routingTable.remove(me.getKey());
						Node.routingTable.put(me.getKey(),me.getValue());

						flag=1;
					}
					if((me.getValue().getDegree()) > Node.routingTable.get(me.getKey()).getDegree()){

						Node.routingTable.get(me.getKey()).setDegree(me.getValue().getDegree());

						flag=1;
					}
				}
				else{
					me.getValue().setNextHop(m.getMsgSender());
					me.getValue().setCost(me.getValue().getCost()+1);
					Node.routingTable.put(me.getKey(), me.getValue());

					flag=1;

				}		

			}
			//System.out.println("leaving Process Message!!");

		}

		if(flag==1){
			synchronized (TCPSender.startDV) {

				TCPSender.startDV = 1;

			}
		}
	}

	public void run() {

		while (true) {

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			receiveMessage();
		}

	}
}
