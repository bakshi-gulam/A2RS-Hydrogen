package in.gulam.a2rs;

import java.net.InetAddress;

public class MatchFields 
{
	public InetAddress SIP,DIP;
	public int transportProto;
	public int transportSrcPort, transportDstPort;
    public int validMatches;
    
	public MatchFields(InetAddress SIP, InetAddress DIP, int transportProto,
			int transportSrcPort, int transportDstPort) 
	{
		this.SIP = SIP;
		this.DIP = DIP;
		this.transportProto = transportProto;
		this.transportSrcPort = transportSrcPort;
		this.transportDstPort = transportDstPort;
		validMatches = 0;
		try
		{
			if(SIP.equals(InetAddress.getByName("0.0.0.0")) == false)
			{
				validMatches++;
			}	
			if(DIP.equals(InetAddress.getByName("0.0.0.0")) == false)
			{
				validMatches++;
			}
			if(transportProto != FlowInfo.TRANS_PROTO_UNKNOWN)
			{
				validMatches++;
			}
			if(transportSrcPort != 0)
			{
				validMatches++;
			}
			if(transportDstPort != 0)
			{
				validMatches++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
    
}
