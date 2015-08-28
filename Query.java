/**
 * Class for Query Details
 */
public class Query {

	private String queryType ; //"QUERY_ROUTING_TABLE", "QUERY_DEGREE" , "QUERY_FARTHEST_NODES"
	private String queryToNode ;
	private int udpPort ;
	
	public Query(String queryType, String queryToNode, int udpPort) {
		super();
		this.queryType = queryType;
		this.queryToNode = queryToNode;
		this.udpPort = udpPort;
	}

	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	public String getQueryToNode() {
		return queryToNode;
	}
	public void setQueryToNode(String queryToNode) {
		this.queryToNode = queryToNode;
	}
	public int getUdpPort() {
		return udpPort;
	}
	public void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}
}
