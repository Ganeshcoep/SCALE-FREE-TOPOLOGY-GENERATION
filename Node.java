import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * Node class declares and implements methods that handles
 * Initialization of incoming node with default values
 * initialization of routing table with entry for each new node ,if not exceeding n
 * Building initial topology ..connections among m0 nodes when m0th node comes 
 * Accepting input from user whether to perform Query / Join
 * Preferential attachment for Join request 
 * Displaying of menu for UDP queries and sending the requested query
 * initialization of routing table
 */
public class Node {

	public static int myID = -1;
	public static String myHostName = null;
	public static int myTcpPort = 5052;
	public static int myUdpPort = 9091;

	public static HashMap<String,Socket> tcpSockets = new HashMap<>();
	public static ArrayList<Host> Hosts = new ArrayList<>();

	public static int m;
	public static int m0;
	public static int n;	

	public static HashMap<String, DistanceVector> routingTable = new HashMap<>();

	/**
	 * Function to Initialize each new node and put in routing table
	 */
	private static void initializeRoutingTable(){

		DistanceVector dv=null;
		synchronized (Node.tcpSockets) {
			dv= new DistanceVector(myHostName,myHostName, 0, Node.tcpSockets.size());	
		}

		synchronized (Node.routingTable) {
			routingTable.put(myHostName,dv );	
		}
	}

	/**
	 * Function to read config.txt file
	 * @param NodeId
	 */
	private static void readConfig(int NodeId) {
		// Read config file and store the details in the routing Table HashMap 

		try {
			BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream("Config/config.txt")));
			String line=buf.readLine();
			String temp[]=new String[4];

			if(line!=null){
				temp=line.split(" ");
				m = Integer.parseInt(temp[0]) ;
				m0 = Integer.parseInt(temp[1]) ;
				n = Integer.parseInt(temp[2]) ;
				line=buf.readLine();
				//System.out.println(m+":"+m0+":"+n);
			}

			while(line!=null){

				temp=line.split(" ");

				final Host newhost = new Host();

				// Add details to the newhost and then insert it into host details array 
				newhost.hostId=Integer.parseInt(temp[0]);
				newhost.hostName=temp[1];
				newhost.tcpPort=Integer.parseInt(temp[2]);
				newhost.udpPort=Integer.parseInt(temp[3]);
				Hosts.add(newhost);

				if(Integer.parseInt(temp[0])==NodeId){
					myID=Integer.parseInt(temp[0]);
					myHostName=temp[1];
					myTcpPort = Integer.parseInt(temp[2]) ;
					myUdpPort = Integer.parseInt(temp[3]) ;
				}
				//System.out.println("\nLine"+ line);
				line = buf.readLine();
			}			
			buf.close();	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Values :::M:"+m+":m0:" + m0 + ":n:"+n+ ":myid" + myID);
	}

	/**
	 * Function to make TCP connections among m0 nodes
	 */
	private static void makeConnections(){

		Iterator<Host> i = Hosts.iterator();

		//System.out.println("In make connection");
		while(i.hasNext()){
			Host h=	i.next();
			if(h.hostId<myID){
				try{
					Socket client = new Socket(h.hostName,h.tcpPort);
					System.out.println("\nConnected to: " + h.hostName);

					updateMyDegree() ;

					tcpSockets.put(h.hostName, client) ;
				}catch(UnknownHostException e){
					//e.printStackTrace() ;
					System.out.println("\nNot Connected to: " + h.hostName);
				}catch(IOException e){
					//e.printStackTrace() ;
					System.out.println("\nIOException: " + h.hostName);
				}


			}
		}
	}

	/**
	 * Function to start all threads
	 */
	public static void joinNetwork(){

		initializeRoutingTable();

		//starting threads
		Thread th1 = new TCPAccepter(myHostName);
		th1.start() ;

		if(myID == (m0-1)){
			//System.out.println("calling make connections");
			makeConnections() ;
		}

		Thread th3 = new TCPReceiver();
		th3.start() ;

		Thread th2 = new TCPSender(myHostName);
		th2.start() ;

		Thread th4 = new UDPListener(myHostName);
		th4.start() ;

		Thread th5 = new TriggerUpdateOfDVTables();
		th5.start() ;


		try {
			th1.join();
			th2.join();
			th3.join();
			th4.join();
			th5.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


	}

	/**
	 * Computing total degree of the current connected network
	 * @return total degree
	 */
	private static int CalculateTotalDegree(){
		Set<Entry<String, DistanceVector>> set = Node.routingTable.entrySet();
		Iterator<Entry<String, DistanceVector>> i = set.iterator();
		int Degree=0, Sum=0;
		while(i.hasNext())
		{
			Map.Entry<String,DistanceVector> me = (Map.Entry<String, DistanceVector>)i.next();
			Degree = me.getValue().getDegree();
			Sum = Sum + Degree;
		}
		return Sum;
	}

	/**
	 * Function which gives m nodes to which the new node (not among m0 nodes) can make connections to
	 * @return HashMap with m entries representing m nodes, 
	 * with key as node hostname and value as its TCP port on which TCP connection can be made 
	 */
	private  static HashMap<String,Integer> GetNodesToConnectTo(){

		int TotalDegreeInNetwork = CalculateTotalDegree();
		String hostArray[] = new String[TotalDegreeInNetwork];

		Set<Entry<String, DistanceVector>> set = Node.routingTable.entrySet();
		Iterator<Entry<String, DistanceVector>> i = set.iterator();
		int cnt=0;
		while(i.hasNext())
		{
			Map.Entry<String,DistanceVector> me=(Map.Entry<String, DistanceVector>)i.next();
			String key=me.getKey();
			int Degree=me.getValue().getDegree();

			for(int j=0;j<Degree;j++){
				hostArray[cnt]=key;
				cnt++;
			}

		}

		//randomly choosing m nodes to which the new node sending JOIN request can connect to
		//probability of high degree nodes getting chosen would be high
		HashMap<String, Integer> NodesToConnectTo = new HashMap<String, Integer>();
		Random genRandom = new Random();
		int selectIndex, count = 0;
		while(count < m) {
			selectIndex = genRandom.nextInt(TotalDegreeInNetwork);
			//check if already chosen node is again chosen by the random function
			if(NodesToConnectTo.containsKey(hostArray[selectIndex]))
				continue;
			NodesToConnectTo.put(hostArray[selectIndex], myTcpPort);
			count++;
			System.out.println("Selected node :"+hostArray[selectIndex]);	
		}

		return NodesToConnectTo;		

	}

	/**
	 * Function to make connections with m nodes returned from the function GetNodesToConnectTo()
	 * @param Nodes to connect to
	 */
	private static void makeConnections(HashMap<String, Integer> Nodes){

		Set<Entry<String, Integer>> set = Nodes.entrySet();
		Iterator<Entry<String, Integer>> i = set.iterator();
		//System.out.println("In make connection of new node");

		Node.routingTable.clear();
		initializeRoutingTable() ;

		while(i.hasNext()){
			Map.Entry<String,Integer> me=(Map.Entry<String, Integer>)i.next();

			//System.out.println("In make connection in loop");
			try{
				Socket client = new Socket(me.getKey(),myTcpPort);
				System.out.println("\nConnected to: " + me.getKey());

				updateMyDegree() ;

				//Increase the cost of the neighbours to which  the new node is connected
				//DistanceVector dv=new DistanceVector(me.getKey(),myHostName,1,1);
				//Node.routingTable.put(me.getKey(),dv);
				tcpSockets.put(me.getKey(), client) ;

			}catch(UnknownHostException e){
				//e.printStackTrace() ;
				System.out.println("\nNot Connected to: " + me.getKey());
			}catch(IOException e){
				//e.printStackTrace() ;
				System.out.println("\nIOException: " + me.getKey());
			}

		}

		System.out.println("Connections to new neighbors done!");

		//Start Distance vector as topology has changed
		synchronized(TCPSender.startDV){
			TCPSender.startDV=1;

		}

	}

	/**
	 * Function to randomly choose m0 node and get current routing table info from it
	 * @return true if routing table returned , false if there was a Reject Message
	 */
	private static boolean getInitialTopology(){

		Random genRandom = new Random();
		int selectIndex = genRandom.nextInt(m0);

		DatagramSocket clientSocket=null;
		InetAddress IPAddress=null;
		try {
			clientSocket = new DatagramSocket();
			IPAddress = InetAddress.getByName(Node.Hosts.get(selectIndex).hostName);
		} catch (UnknownHostException | SocketException e) {
			e.printStackTrace();
		}
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[10000];
		String sentence="JOIN_REQ";
		sendData = sentence.getBytes();

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, myUdpPort);
		try {
			//Message incomingMsg=null;	 
			clientSocket.send(sendPacket);
			
			byte[] recvBuf = new byte[50000];
			DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
			clientSocket.receive(packet);
			//int byteCount = packet.getLength();
			ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
			Message incomingMsg= (Message)is.readObject();
			is.close();
			      
			//  System.out.println(incomingMsg.getMsgContent()+"------------");
			
			if(incomingMsg!= null && incomingMsg.getMsgType().equals("REJECT"))
				return false;
			else
			{
				Node.routingTable = incomingMsg.getRoutingTable() ;

				return true;
			}
		}catch(Exception e){
			e.printStackTrace() ;
		}
		clientSocket.close();
		return false;
	}
	
	/**
	 * Function to update node degree after connection 
	 */
	public static void updateMyDegree(){
		synchronized(Node.routingTable){
			DistanceVector dvObj = Node.routingTable.get(myHostName) ;
			dvObj.setDegree(dvObj.getDegree()+1) ;

		}
	}

	/**
	 * Function to check if the node to be queried is present in the connected network 
	 * @param nodeToBeQueried
	 * @return
	 */
	public static boolean isNodePresent(String nodeToBeQueried){
		
		boolean flag = false;
		try {
			BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream("Config/config.txt")));
			String line=buf.readLine();
			String temp[]=new String[4];

			if(line!=null){
				line = buf.readLine();
			}

			while(line!=null){

				temp=line.split(" ");

				if(temp[1].equals(nodeToBeQueried)){
					flag = true ;
					break ;
				}
				line = buf.readLine();
			}			
			buf.close();	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return flag ;
	}
	
	/*Main Function*/
	public static void main(String[] args) {

		int myID = Integer.parseInt(args[0]) ;
		readConfig(myID) ;

		if(Node.myID==-1) // if the Node is not part of m0 nodes (new node)
		{
			//Set new node's parameters
			Node.myID = myID;
			try {
				Node.myHostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}


			Scanner sc=new Scanner(System.in);
			while(true) {
				String nodeToBeQueried = ""; int queryChoice = 0 ;
				System.out.println("Enter Choice(Query/Join):");
				String userChoice=sc.nextLine();

				if(userChoice.equals("Query")){ 	// Query the Nodes
					Scanner sc2 = new Scanner(System.in);
					do {
						System.out.println("---MENU---");
						System.out.println("1. QUERY_ROUTING_TABLE");
						System.out.println("2. QUERY_DEGREE");
						System.out.println("3. QUERY_FARTHEST_NODES");
						System.out.println("------------------------");
						try{
							System.out.println("Enter Choice: ");
							queryChoice = sc2.nextInt();
	
							System.out.println("Enter the host name: ");
							nodeToBeQueried = sc.nextLine();

						 // if(isNodePresent(nodeToBeQueried)){
							DatagramSocket clientSocket=null;
							InetAddress IPAddress=null;
							try {
								clientSocket = new DatagramSocket();
								IPAddress = InetAddress.getByName(nodeToBeQueried);
								
							} catch (UnknownHostException | SocketException e) {
								e.printStackTrace();
							}
						
							byte[] sendData = new byte[1024];
							byte[] receiveData = new byte[10000];
							String sentence="";
	
							switch(queryChoice){
							case 1:
								sentence = "QUERY_ROUTING_TABLE";
								break;
	
							case 2:
								sentence = "QUERY_DEGREE";
								break;
	
							case 3:
								sentence = "QUERY_FARTHEST_NODES";
								break;
	
							default:
								queryChoice = 0 ;
								System.out.println("Enter appropriate choice!");
								break;
							}
						
							if(queryChoice == 1 || queryChoice == 2 || queryChoice == 3)
							{	//sending query over UDP
								sendData = sentence.getBytes();
								DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, myUdpPort);
								try {
									clientSocket.send(sendPacket);
									System.out.println("Query Sent on UDP...." + sentence); 
								     sentence = "" ;
									clientSocket.send(sendPacket);
									
									byte[] recvBuf = new byte[50000];
								    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
								    clientSocket.receive(packet);
								    //int byteCount = packet.getLength();
								    ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
								    ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
								    Message incomingMsg = (Message)is.readObject();
								    is.close();
								      
								    System.out.println(incomingMsg.getMsgContent()+"------------");
	
									int msgChoice = 0;
									if(incomingMsg!=null){
	
										if(incomingMsg.getMsgType().equals("RoutingTable"))
											msgChoice = 1;
										if(incomingMsg.getMsgType().equals("DegreeInfo"))
											msgChoice = 2;
										if(incomingMsg.getMsgType().equals("FarthestNode"))
											msgChoice = 3;
									}
									switch(msgChoice)
									{
									case 1:
										Node.routingTable = incomingMsg.getRoutingTable();
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
										break;
	
									case 2:
										System.out.println("The Degree of the queried node is: "+ incomingMsg.getMsgContent());
										break;
	
									case 3:
										System.out.println("The Farthest Node(s) from the queried node: "+ incomingMsg.getMsgContent());
										break;
									default: System.out.println("\nNo Data Received !!"); 
									break ;
									}
									clientSocket.close();
								} catch (IOException | ClassNotFoundException e) {
									e.printStackTrace();
								}
							}
						/*}else
							System.out.println("Host not present in the current network!!"); */
							
							System.out.println("Do you want to enter more queries? (Enter 1 for yes ; 0 for no)");
							Scanner scan = new Scanner(System.in) ;
							int choice = scan.nextInt();
							if (choice == 1)
								continue;
							else if (choice == 0)
								break;
							else if (choice != 0){
								System.out.println("Invalid input. Enter either 1 or 0 !!");
								break;
							}
						}catch(InputMismatchException e){
							System.out.println("Input Mismatch(Integer expected)!");
							break;
						}catch(NoSuchElementException e){
							e.printStackTrace() ;
							break;
						}catch(Exception e){
							e.printStackTrace() ;
							break;
						}
						//scan.close() ;
					} while(true);
					//sc2.close();

				}
				else if(userChoice.equals("Join")){ // Join the topology
					// 	Call function which gives us the routing table.
					// if instead of routing table, Reject is received then break the loop 
					// Call function which gives us nodes to make m connections to
					boolean flag = getInitialTopology();
					if(flag)
					{
						HashMap<String,Integer> NewNeighbors = GetNodesToConnectTo();
						//initializeRoutingTable() ;
						makeConnections(NewNeighbors);
						joinNetwork();
					}
					else
					{
						System.out.println("\nReject Message:The Topology has reached the maximum limit for nodes...You are rejected!!");
					}
				}else{
					System.out.println("Invalid Input(Query/Join expected)!!");
				}
				try{
					System.out.println("Do you want to continue? (Enter 1 for yes ; 0 for no)");
					Scanner scan2 =new Scanner(System.in) ;
					int choice = scan2.nextInt();
					if (choice == 1)
						continue;
					else if (choice == 0)
						break;
					else if (choice != 0){
						System.out.println("Invalid input. Enter either 1 or 0 !!");
						break;
					}
				}catch(InputMismatchException e){
					System.out.println("Input Mismatch(Integer expected)!");
				}catch(NoSuchElementException e){
					e.printStackTrace() ;
				}catch(Exception e){
					e.printStackTrace() ;
				}
				//scan2.close() ;
			};

			//sc.close();
		}	
		else // if the Node is part of m0 nodes 
		{
			joinNetwork();
		}


	}

}
