package in.gulam.a2rs; 

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;

import edu.asu.emit.qyan.alg.control.DijkstraShortestPathAlg;
import edu.asu.emit.qyan.alg.control.YenTopKShortestPathsAlg;
import edu.asu.emit.qyan.alg.model.Graph;
import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.VariableGraph;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;
import edu.asu.emit.qyan.alg.model.Vertex;

import org.opendaylight.controller.sal.action.Action;
import org.opendaylight.controller.sal.action.ActionType;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.core.Host;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Edge;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.flowprogrammer.Flow;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.match.Match;
import org.opendaylight.controller.sal.match.MatchType;
import org.opendaylight.controller.sal.packet.Ethernet;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.IPv4;
import org.opendaylight.controller.sal.packet.TCP;
import org.opendaylight.controller.sal.packet.UDP;
import org.opendaylight.controller.sal.packet.Packet;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.topology.TopoEdgeUpdate;
import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.IPProtocols;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.opendaylight.controller.topologymanager.ITopologyManagerClusterWideAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class A2RS implements IListenDataPacket, ITopologyManagerClusterWideAware {

	private static final Logger log = LoggerFactory.getLogger(A2RS.class);
	private IDataPacketService dataPacketService;
	private ITopologyManager topologyManager;
	private IFlowProgrammerService programmer = null;

	ArrayList<FlowRule> flowRuleList;
	
	private static final int K_SHORTEST_PATHS = 2;
	
	void init()
	{
		flowRuleList = new ArrayList<FlowRule>();
		try
		{
		  flowRuleList.add(new FlowRule(new MatchFields(InetAddress.getByName("0.0.0.0"), InetAddress.getByName("0.0.0.0"), FlowInfo.TRANS_PROTO_TCP, 80, 0), FlowInfo.FLOW_CLASS_WEB)); /* HTTP Response */	
		  flowRuleList.add(new FlowRule(new MatchFields(InetAddress.getByName("0.0.0.0"), InetAddress.getByName("0.0.0.0"), FlowInfo.TRANS_PROTO_TCP, 0, 80), FlowInfo.FLOW_CLASS_WEB)); /* HTTP Request */
		  flowRuleList.add(new FlowRule(new MatchFields(InetAddress.getByName("0.0.0.0"), InetAddress.getByName("0.0.0.0"), FlowInfo.TRANS_PROTO_TCP, 20, 0), FlowInfo.FLOW_CLASS_FILE_TRANSFER)); /* Active FTP Data from Server */
		  flowRuleList.add(new FlowRule(new MatchFields(InetAddress.getByName("0.0.0.0"), InetAddress.getByName("0.0.0.0"), FlowInfo.TRANS_PROTO_TCP, 0, 20), FlowInfo.FLOW_CLASS_FILE_TRANSFER)); /* Active FTP Data to Server */
		  flowRuleList.add(new FlowRule(new MatchFields(InetAddress.getByName("0.0.0.0"), InetAddress.getByName("0.0.0.0"), FlowInfo.TRANS_PROTO_TCP, 21, 0), FlowInfo.FLOW_CLASS_FILE_TRANSFER)); /* Active FTP Control from Server */
		  flowRuleList.add(new FlowRule(new MatchFields(InetAddress.getByName("0.0.0.0"), InetAddress.getByName("0.0.0.0"), FlowInfo.TRANS_PROTO_TCP, 0, 21), FlowInfo.FLOW_CLASS_FILE_TRANSFER)); /* Active FTP Control to Server */
		  flowRuleList.add(new FlowRule(new MatchFields(InetAddress.getByName("0.0.0.0"), InetAddress.getByName("0.0.0.0"), FlowInfo.TRANS_PROTO_TCP, 1935, 0), FlowInfo.FLOW_CLASS_VIDEO)); /* RTMP Traffic from Server */
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	static private InetAddress intToInetAddress(int i) {
		byte b[] = new byte[] { (byte) ((i>>24)&0xff), (byte) ((i>>16)&0xff), (byte) ((i>>8)&0xff), (byte) (i&0xff) };
		InetAddress addr;
		try {
			addr = InetAddress.getByAddress(b);
		} catch (UnknownHostException e) {
			return null;
		}

		return addr;
	}

	/*
	 * Sets a reference to the requested DataPacketService
	 * See Activator.configureInstance(...):
	 * c.add(createContainerServiceDependency(containerName).setService(
	 * IDataPacketService.class).setCallbacks(
	 * "setDataPacketService", "unsetDataPacketService")
	 * .setRequired(true));
	 */
	void setDataPacketService(IDataPacketService s) {
		log.trace("Set DataPacketService.");

		dataPacketService = s;
		init();
	}

	/*
	 * Unsets DataPacketService
	 * See Activator.configureInstance(...):
	 * c.add(createContainerServiceDependency(containerName).setService(
	 * IDataPacketService.class).setCallbacks(
	 * "setDataPacketService", "unsetDataPacketService")
	 * .setRequired(true));
	 */
	void unsetDataPacketService(IDataPacketService s) {
		log.trace("Removed DataPacketService.");

		if (dataPacketService == s) {
			dataPacketService = null;
		}
	}
	
	public void setTopologyManager(ITopologyManager tm) {
        this.topologyManager = tm;
    }

    public void unsetTopologyManager(ITopologyManager tm) {
        if (this.topologyManager == tm) {
            this.topologyManager = null;
        }
    }

    public void setFlowProgrammerService(IFlowProgrammerService s) {
    	this.programmer = s;
    	}

    public void unsetFlowProgrammerService(IFlowProgrammerService s) {
    	if (this.programmer == s) {
    	this.programmer = null;
    	}
    }
    	
	public PacketResult receiveDataPacket(RawPacket inPkt) {
			log.trace("Received data packet.");
			// System.out.println("A2RS: Received data packet..."); 
		try
		{
			FlowInfo flowInfo = new FlowInfo();
			
			// The connector, the packet came from ("port")
			NodeConnector ingressNodeConnector = inPkt.getIncomingNodeConnector();
			flowInfo.setIngressNodeConnector(ingressNodeConnector);
			
			// The node that received the packet ("switch")
			Node node = ingressNodeConnector.getNode();
			flowInfo.setNode(node);

			// Use DataPacketService to decode the packet.
			Packet l2pkt = dataPacketService.decodeDataPacket(inPkt);

			if (l2pkt instanceof Ethernet) {
				Object l3Pkt = l2pkt.getPayload();
				if (l3Pkt instanceof IPv4) {
					IPv4 ipv4Pkt = (IPv4) l3Pkt;

					//extract Source IP
					int srcAddr = ipv4Pkt.getSourceAddress();
					InetAddress SIP = intToInetAddress(srcAddr);
					flowInfo.setSIP(SIP);
					
					int dstAddr = ipv4Pkt.getDestinationAddress();
					InetAddress DIP = intToInetAddress(dstAddr);
					flowInfo.setDIP(DIP);
					
				//	flowInfo.setTransportProto(ipv4Pkt.getProtocol());
					
					Object l4Pkt = ((IPv4) l3Pkt).getPayload();
					if(l4Pkt instanceof TCP)
					{
						TCP tcpPkt = (TCP)l4Pkt;
						flowInfo.setTransportProto(FlowInfo.TRANS_PROTO_TCP);
						flowInfo.setTransportSrcPort(tcpPkt.getSourcePort());
						flowInfo.setTransportDstPort(tcpPkt.getDestinationPort());
						
				/*		byte rawPayload[] = tcpPkt.getRawPayload();
						System.out.println("tcpPkt's rawPayload size : "+rawPayload.length);
						System.out.print("tcpPkt's rawPayload : ");
						for(int i=0;i<rawPayload.length;i++)
						{
							System.out.print((char)rawPayload[i]);
						}
				*/		
					}
					else if(l4Pkt instanceof UDP)
					{
						UDP udpPkt = (UDP)l4Pkt;
						flowInfo.setTransportProto(FlowInfo.TRANS_PROTO_UDP);
						flowInfo.setTransportSrcPort(udpPkt.getSourcePort());
						flowInfo.setTransportDstPort(udpPkt.getDestinationPort());
					}
					
					
					flowInfo = FlowClassifier.classifyFlow(flowRuleList, flowInfo);
					
					flowInfo.dump();

					Map<Edge, Set<Property>> edges = topologyManager.getEdges();
					
					Iterator<Property> iterator;
					double bandwidth = 0;
					int head = 0,tail = 0;
					Long temp = null;
					Property property = null;
					String content = null;
					Set<Integer> vertices = new HashSet<Integer>();
					
					if(edges != null)
					{
						File tmpFile = new File("graph.txt~");
            			tmpFile.delete();
            			
						File file = new File("graph.txt~");
	                     
            			// if file doesn't exists, then create it
            			if (!file.exists()) {
            				file.createNewFile();
            			}
             
            			FileWriter fw = new FileWriter(file.getAbsoluteFile());
            			BufferedWriter bw = new BufferedWriter(fw);
            		    
            			//content = String.format("%d\n\n", edges.size());
            		    //bw.write(content);
            		    
            			for(Map.Entry<Edge, Set<Property>> entry : edges.entrySet())
						{
							temp = (Long)entry.getKey().getHeadNodeConnector().getNode().getID();
							head = temp.intValue();
							temp = (Long)entry.getKey().getTailNodeConnector().getNode().getID();
						    tail = temp.intValue();
						
						    vertices.add(new Integer(head));
						    vertices.add(new Integer(tail));

							iterator = entry.getValue().iterator();
					        
							while(iterator.hasNext())
							{
								property = iterator.next();
							
								if(property.getName().equals("bandwidth"))
								{
									bandwidth = bandwidthStringValueToDouble(property.getStringValue());
									break;
								}
							}
                            content = String.format("%d %d %.15f\n", (head-1), (tail-1), 1/bandwidth);
                            
							//graph.add_edge(head,tail,bandwidth);
                            
                     
                    			bw.write(content);
                     
                    
                            
						} // end of for() loop
	
            			bw.close();
            			//delete graph.txt before writing to it
            			tmpFile = new File("graph.txt");
            			tmpFile.delete();
            			
            			RandomAccessFile rafile = new RandomAccessFile("graph.txt", "rw");
            			RandomAccessFile rafileTemp = new RandomAccessFile("graph.txt~", "rw");
            			FileChannel targetChannel = rafile.getChannel();
            			FileChannel sourceChannel = rafileTemp.getChannel();

            			rafile.seek(0);
            			rafile.write(String.format("%d\n\n", vertices.size()).getBytes());
            			sourceChannel.position(0L);
            			sourceChannel.transferTo(0L, rafileTemp.length(), targetChannel);
            			sourceChannel.close();
            			targetChannel.close();
            			//rafile.close();
            			
            			System.out.println("Topology written to file.");
    
					} // end of if()
					//System.out.println("Graph :" + graph.toString());
				    
					Graph graph = new VariableGraph("graph.txt");
					YenTopKShortestPathsAlg yenAlg = new YenTopKShortestPathsAlg(graph);
					List<Path> shortest_paths_list = yenAlg.get_shortest_paths(
							graph.get_vertex(getNodeConnectedToHost(flowInfo.getSIP())-1), 
							graph.get_vertex(getNodeConnectedToHost(flowInfo.getDIP())-1), K_SHORTEST_PATHS);
					System.out.println("possible routes:"+shortest_paths_list);
					
					Path route = selectBestRoute(shortest_paths_list, flowInfo.getFlowClassifier());
					installRouteOnNetwork(edges, route, flowInfo);
					return PacketResult.KEEP_PROCESSING;
				}
			}
			
			}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		 // We did not process the packet -> let someone else do the job.
		return PacketResult.IGNORED;
	
		}
	
	
	Path selectBestRoute(List<Path> shortest_paths_list, int flowClass)
	{
	  Path route = null;
	  
	  if(shortest_paths_list.size() == 1)
	  {
		  route = shortest_paths_list.get(0);
	  }
	  else if(shortest_paths_list.size() == 2)
	  {
		  if(flowClass == FlowInfo.FLOW_CLASS_AUDIO ||
			 flowClass == FlowInfo.FLOW_CLASS_VIDEO ||
			 flowClass == FlowInfo.FLOW_CLASS_LIVE_STREAM ||
			 flowClass == FlowInfo.FLOW_CLASS_GAMES || 
			 flowClass == FlowInfo.FLOW_CLASS_FILE_TRANSFER)
		  {
			  route = (shortest_paths_list.get(0).get_weight() < shortest_paths_list.get(1).get_weight())?
					   shortest_paths_list.get(0):shortest_paths_list.get(1);
		  }
		  else
		  {
			  route = (shortest_paths_list.get(0).get_weight() > shortest_paths_list.get(1).get_weight())?
					   shortest_paths_list.get(0):shortest_paths_list.get(1);
		  }
	  }
	  
	  return route;
	}
	
	void installRouteOnNetwork(Map<Edge, Set<Property>> edges, Path route, FlowInfo flowInfo)
	{
		Flow flow = new Flow();
		Match match = new Match();
		Status rv = null;
		
		List<BaseVertex> vertices = null;
		BaseVertex currentVertex = null, nextVertex = null;
		Long temp = null;
		int head = -1, tail = -1;
		
		match.setField(MatchType.DL_TYPE, EtherTypes.IPv4.shortValue());
		match.setField(MatchType.NW_SRC, flowInfo.getSIP());
		match.setField(MatchType.NW_DST, flowInfo.getDIP());
		if(flowInfo.getTransportProto() == FlowInfo.TRANS_PROTO_TCP)
		{
			match.setField(MatchType.NW_PROTO, IPProtocols.TCP.byteValue());
			if(isStandardTCPPort(flowInfo.getTransportSrcPort()))
			{
				match.setField(MatchType.TP_SRC, (short)flowInfo.getTransportSrcPort());	
			}
			if(isStandardTCPPort(flowInfo.getTransportDstPort()))
			{
				match.setField(MatchType.TP_DST, (short)flowInfo.getTransportDstPort());	
			}
		}
		
		flow.setMatch(match);
		flow.setActions(new ArrayList<Action>());
		
		vertices = route.get_vertices();
		for(int i = 0; i < vertices.size(); i++)
		{
			currentVertex = vertices.get(i);
			if(i != (vertices.size() - 1))
			{
				nextVertex = vertices.get(i+1);
			}
			else
			{
				nextVertex = null;
			}
			
			if(null != flow.getActions())
			{
				  flow.removeAction(ActionType.OUTPUT);
			}
					
			if (nextVertex != null)
			{
     			for(Map.Entry<Edge, Set<Property>> entry : edges.entrySet())
				{
     				temp = (Long)entry.getKey().getHeadNodeConnector().getNode().getID();
     				head = temp.intValue();
     				temp = (Long)entry.getKey().getTailNodeConnector().getNode().getID();
     				tail = temp.intValue();
     				
     				if(head == (currentVertex.get_id() + 1) &&
					   tail == (nextVertex.get_id() + 1) )
					{
						flow.addAction(new Output(entry.getKey().getHeadNodeConnector()));
						rv = programmer.addFlow(entry.getKey().getHeadNodeConnector().getNode(), flow);
						System.out.println("addFlow returned " + rv.getDescription());
						break;
					}
				}
			}
			else
			{
				// install the flow on last node on the route
				flow.addAction(new Output(getNodeConnectorConnectedToHost(flowInfo.getDIP())));
				rv = programmer.addFlow(getNodeConnectorConnectedToHost(flowInfo.getDIP()).getNode(), flow);
				System.out.println("addFlow returned " + rv.getDescription());
				
			}
		}
		
		
		
	}
	
	private NodeConnector getNodeConnectorConnectedToHost(InetAddress ip)
	{

		Set<NodeConnector> nodeConnectorSet = topologyManager.getNodeConnectorWithHost();
		
		List<Host> hostList = null;
		NodeConnector nodeConnector = null;
		Host host = null;
		Iterator<Host> iteratorHost = null;
		Iterator<NodeConnector> iteratorNodeConnector = nodeConnectorSet.iterator();
		while(iteratorNodeConnector.hasNext())
		{
			nodeConnector = iteratorNodeConnector.next();
			hostList = topologyManager.getHostsAttachedToNodeConnector(nodeConnector);
			
			iteratorHost = hostList.iterator();
			while(iteratorHost.hasNext())
			{
				host = iteratorHost.next();
				if(host.getNetworkAddress().equals(ip))
				{
					return nodeConnector;
				}
			}
		}
		//should not be hitting this
		System.out.println("getNodeConnectorConnectedToHost returning null...");
		return null;
	}
	
	private int getNodeConnectedToHost(InetAddress ip)
	{
	  	// replace this definition with actually retrieving node info from topology manager
		NodeConnector nodeConnector = getNodeConnectorConnectedToHost(ip);
		Long tmp = null;
		if(null != nodeConnector)
		{
			tmp = (Long)nodeConnector.getNode().getID();
			return tmp.intValue();
		}
		return -1;
	}

	private double bandwidthStringValueToDouble(String bandwidthStringValue) 
	{
	    double multiplier = 1;

	    if(bandwidthStringValue.matches("(.*)Kbps(.*)"))
	    	multiplier = 1000;
	    else if(bandwidthStringValue.matches("(.*)Mbps(.*)"))
	    	multiplier = 1000000;
	    else if(bandwidthStringValue.matches("(.*)Gbps(.*)"))
	    	multiplier = 1000000000;
	    
	    bandwidthStringValue = bandwidthStringValue.replaceAll("[^0-9]", "");
		return (Double.parseDouble(bandwidthStringValue) * multiplier);
	}
	
	public boolean isStandardTCPPort(int port)
	{
		if((port == 80) || (port == 21) || (port == 20))
		{
			return true;
		}
		else
			return false;
	}
	
	public void 	edgeUtilBackToNormal(Edge edge)
	{
		// just to avoid compilation error
	}
	
	public void 	edgeOverUtilized(Edge edge)
	{
		// just to avoid compilation error
	}
	public void 	edgeUpdate(List<TopoEdgeUpdate> topoedgeupdateList)
	{
		// just to avoid compilation error
	}
	
	
}
