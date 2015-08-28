import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Class for declaration and access to attributes of Message exchanged between hosts
 * Implements Serializable
 */
public class Message implements Serializable{
	private String msgType; //msgtypes -> {"RoutingTable", "FarthestNode",  "DegreeInfo", "REJECT"} ;
	private String msgSender;
	private String msgContent ;
	private HashMap<String, DistanceVector> routingTable ;

	public String getMsgSender() {
		return msgSender;
	}

	public void setMsgSender(String msgSender) {
		this.msgSender = msgSender;
	}
	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public HashMap<String, DistanceVector> getRoutingTable() {
		return routingTable;
	}

	public void setRoutingTable(HashMap<String, DistanceVector> routingTable) {
		this.routingTable = routingTable;
	}

	public Message(String msgType, String msgSender,String msgContent,
			HashMap<String, DistanceVector> routingTable) {
		super();
		this.msgType = msgType;
		this.msgSender = msgSender;
		this.msgContent = msgContent;
		this.routingTable = routingTable;
	} 

	/**
	 *  Function to serialize objects
	 * @param obj , object to serialize
	 * @return byte array
	 * @throws IOException
	 * @throws NullPointerException
	 */
	public static byte[] serialize(Object obj) throws IOException, NullPointerException {
		if(obj==null)
			return (new ByteArrayOutputStream()).toByteArray();

		ObjectOutputStream out;// = new ObjectOutputStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		out = new ObjectOutputStream(bos) ;
		out.writeObject(obj);
		return bos.toByteArray();
	}

	/**
	 *  Function to deserialize objects
	 * @param obj, object to deserialize
	 * @return byte array
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NullPointerException
	 */
	public static Object deserialize(byte[] obj) throws IOException, ClassNotFoundException, NullPointerException {
		if(obj==null)
			return new Object();

		ObjectInputStream in;// = new ObjectOutputStream();
		ByteArrayInputStream bos = new ByteArrayInputStream(obj) ;
		in = new ObjectInputStream(bos) ;
		return in.readObject();

	}


}
