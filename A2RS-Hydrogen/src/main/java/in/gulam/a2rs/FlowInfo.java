package in.gulam.a2rs;

import java.net.InetAddress;

import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;

public class FlowInfo
{
  
  public static final int TRANS_PROTO_UNKNOWN = 0;
  public static final int TRANS_PROTO_TCP = 1;
  public static final int TRANS_PROTO_UDP = 2;

  public static final int APP_PROTO_UNKNOWN = 0;
  public static final int APP_PROTO_HTTP = 1;
  public static final int APP_PROTO_FTP = 2;
  
  
  public static final int FLOW_CLASS_UNKNOWN = 0;
  public static final int FLOW_CLASS_WEB = 1;
  public static final int FLOW_CLASS_WEB_TEXT = 2;
  public static final int FLOW_CLASS_BROWSER_APP = 3;
  public static final int FLOW_CLASS_AUDIO = 4;
  public static final int FLOW_CLASS_VIDEO = 5;
  public static final int FLOW_CLASS_GAMES = 6;
  public static final int FLOW_CLASS_LIVE_STREAM = 7;
  public static final int FLOW_CLASS_FILE_TRANSFER = 8;
  

  private NodeConnector ingressNodeConnector;
  private Node node;

  private InetAddress SIP,DIP;
  private int transportProto;
  private int transportSrcPort;
  private int transportDstPort;

  private boolean isAppProtoEncrypted;
  private int appProto;
  private String MIME_Type;

  private int flowClassifier;

  public FlowInfo()
  {
	  try
	  {
		  ingressNodeConnector = null;
		  node = null;
		  SIP = InetAddress.getByName("0.0.0.0");
		  DIP = InetAddress.getByName("0.0.0.0");
		  transportProto = TRANS_PROTO_UNKNOWN;
		  transportSrcPort = 0;
		  transportDstPort = 0;
		  appProto = APP_PROTO_UNKNOWN;
		  MIME_Type = null;
		  flowClassifier = FLOW_CLASS_UNKNOWN;
		  isAppProtoEncrypted = false;  
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
	  }
	  
  }
  public FlowInfo(NodeConnector ingressNodeConnector, Node node, InetAddress sIP,
		InetAddress dIP, int transportProto, int transportSrcPort, int transportDstPort, 
	    int appProto, String mIME_Type) {
	
	this.ingressNodeConnector = ingressNodeConnector;
	this.node = node;
	SIP = sIP;
	DIP = dIP;
	this.transportProto = transportProto;
	this.transportSrcPort = transportSrcPort;
	this.transportDstPort = transportDstPort;
	this.appProto = appProto;
	MIME_Type = mIME_Type;
}


public NodeConnector getIngressNodeConnector() {
	return ingressNodeConnector;
}

public void setIngressNodeConnector(NodeConnector ingressNodeConnector) {
	this.ingressNodeConnector = ingressNodeConnector;
}

public Node getNode() {
	return node;
}

public void setNode(Node node) {
	this.node = node;
}

public InetAddress getSIP() {
	return SIP;
}

public void setSIP(InetAddress sIP) {
	SIP = sIP;
}

public InetAddress getDIP() {
	return DIP;
}

public void setDIP(InetAddress dIP) {
	DIP = dIP;
}

public int getTransportProto() {
	return transportProto;
}

public void setTransportProto(int transportProto) {
	this.transportProto = transportProto;
}

public int getTransportSrcPort() {
	return transportSrcPort;
}

public int getTransportDstPort() {
	return transportDstPort;
}

public void setTransportSrcPort(int transportSrcPort) {
	this.transportSrcPort = transportSrcPort;
}

public void setTransportDstPort(int transportDstPort) {
	this.transportDstPort = transportDstPort;
}

public int getAppProto() {
	return appProto;
}

public void setAppProto(int appProto) {
	this.appProto = appProto;
}

public String getMIME_Type() {
	return MIME_Type;
}

public void setMIME_Type(String mIME_Type) {
	MIME_Type = mIME_Type;
}

public int getFlowClassifier() {
	return flowClassifier;
}

public void setFlowClassifier(int flowClassifier) {
	this.flowClassifier = flowClassifier;
}

public boolean isAppProtoEncrypted() {
	return isAppProtoEncrypted;
}
public void setIsAppProtoEncrypted(boolean isAppProtoEncrypted) {
	this.isAppProtoEncrypted = isAppProtoEncrypted;
}


public void dump()
{
  System.out.println("======== Flow Info ===========");
  System.out.println("Node : " + node.getNodeIDString());
  System.out.println("SIP : " + SIP);
  System.out.println("DIP : "+ DIP);
  System.out.print("Transport Protocol : ");
  if(transportProto == TRANS_PROTO_TCP)
	  System.out.println("TCP");
  else if(transportProto == TRANS_PROTO_UDP)
	  System.out.println("UDP");
  else
	  System.out.println("UNKNOWN");
  System.out.println("Transport SrcPort : " + transportSrcPort);
  System.out.println("Transport DstPort : " + transportDstPort);
  System.out.print("Flow Class : ");
  if(flowClassifier == FLOW_CLASS_WEB_TEXT)
	  System.out.println("Web Traffic - Text");
  else if(flowClassifier == FLOW_CLASS_BROWSER_APP)
      System.out.println("Web Traffic - Application");
  else if(flowClassifier == FLOW_CLASS_AUDIO)
      System.out.println("Web Traffic - Audio");
  else if(flowClassifier == FLOW_CLASS_VIDEO)
      System.out.println("Web Traffic - Video");
  else if(flowClassifier == FLOW_CLASS_WEB)
      System.out.println("Web Traffic - Unclassified");
  else if(flowClassifier == FLOW_CLASS_FILE_TRANSFER)
	  System.out.println("File Transfer");
  else
	  System.out.println("Unknown Traffic");
  System.out.println("==============================");
}


}
