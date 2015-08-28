import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Class for declaration and access to attributes related to distance vector protocol
 * Implements Serializable
 */
public class DistanceVector implements Serializable{

	private String Destination;
	private String nextHop ;
	private int cost;
	private int degree ;

	public String getDestination() {
		return Destination;
	}
	public void setDestination(String destination) {
		Destination = destination;
	}
	public String getNextHop() {
		return nextHop;
	}
	public void setNextHop(String nextHop) {
		this.nextHop = nextHop;
	}
	public int getCost() {
		return cost;
	}
	public void setCost(int cost) {
		this.cost = cost;
	}
	public int getDegree() {
		return degree;
	}
	public void setDegree(int degree) {
		this.degree = degree;
	}

	public DistanceVector(String destination, String nextHop, int cost,
			int degree) {
		super();
		Destination = destination;
		this.nextHop = nextHop;
		this.cost = cost;
		this.degree = degree;
	}


	/**
	 * Function to serialize objects
	 * @param obj
	 * @return
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
	 * Function to deserialize objects
	 * @param obj
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NullPointerException
	 */
	public static Object deserialize(byte[] obj) throws IOException, ClassNotFoundException, NullPointerException {

		if(obj==null)
			return new Object();
		ObjectInputStream in;
		ByteArrayInputStream bos = new ByteArrayInputStream(obj) ;
		in = new ObjectInputStream(bos) ;
		return in.readObject();
	}

}
