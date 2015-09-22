package in.gulam.a2rs;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

public class FlowClassifier 
{

	public static FlowInfo classifyFlow(ArrayList<FlowRule> flowRuleList, FlowInfo flowInfo)
	{
		FlowRule flowRule;
		int totalMatches;
		Iterator<FlowRule> iterator = flowRuleList.iterator();
		/* Generic flows should go first and Specific flows should follow them. 
		 * So that, even if the flow matches some entry, 
		 * it will be overwritten by specific entry later. */
		while(iterator.hasNext())
		{
			
			flowRule = iterator.next();
			totalMatches = 0;
			try
			{
				if(!flowRule.matchFields.SIP.equals(InetAddress.getAllByName("0.0.0.0")))
				{
					if(flowRule.matchFields.SIP.equals(flowInfo.getSIP()))
					  totalMatches++;
				}	
				if(!flowRule.matchFields.DIP.equals(InetAddress.getAllByName("0.0.0.0")))
				{
					if(flowRule.matchFields.DIP.equals(flowInfo.getDIP()))
					  totalMatches++;
				}
				if(flowRule.matchFields.transportProto != FlowInfo.TRANS_PROTO_UNKNOWN)
				{
					if(flowRule.matchFields.transportProto == flowInfo.getTransportProto())
					  totalMatches++;
				}
				if(flowRule.matchFields.transportSrcPort != 0)
				{
					if(flowRule.matchFields.transportSrcPort == flowInfo.getTransportSrcPort())
					  totalMatches++;
				}
				if(flowRule.matchFields.transportDstPort != 0)
				{
					if(flowRule.matchFields.transportDstPort == flowInfo.getTransportDstPort())
					  totalMatches++;
				}	
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			//System.out.println("Total Matches : " + totalMatches + " ValidMatches = " + flowRule.matchFields.validMatches);
			if(totalMatches == flowRule.matchFields.validMatches)
			{
				flowInfo.setFlowClassifier(flowRule.flowClass);
			}
		}
		return flowInfo;
	}

}
