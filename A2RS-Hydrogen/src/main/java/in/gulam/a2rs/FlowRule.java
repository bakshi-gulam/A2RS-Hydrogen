package in.gulam.a2rs;

public class FlowRule 
{
  MatchFields matchFields;
  int flowClass;

  public FlowRule(MatchFields matchFields, int flowClass) 
  {
	this.matchFields = matchFields;
	this.flowClass = flowClass;
  }
  
}
