# Application-Aware Routing System (A2RS)
A2RS is developed as a plugin for Open Daylight (ODL) SDN Controller (Hydrogen Release). A2RS is developed as a PoC and attempts to route traffic differently based on type of application to which the traffic belongs to. I believe deploying this kind of SDN application will help service providers to dynamically provision the network switches based on application characteristics and requirements, leading to a better overall user experience and reduction in bandwidth wastage in an SDN environment.

Design
------

    ---------------------------------------------------------------
    |    ----------------------        ------------------------   |
    |    | Traffic Classifier |        | Differential Routing |   |
    |    |                    |        |      Engine          |   |
    |    ----------------------        ------------------------   |
    |                            A2RS                             |
    ---------------------------------------------------------------
    
As soon as a packet is lifted to controller, it is parsed and the FlowInfo and (pre-defined)FlowRules are passed to FlowClassifier which classifies the flow. Then the network map is obtained from ODL's Topology Manager and Yen K-Top shortest paths are calculated using the library available at [https://code.google.com/p/k-shortest-paths/ OR https://github.com/yan-qi/k-shortest-paths-java-version]. If more than one shortest path is available, then depending upon the pre-defined rules, traffic is distributed among different paths.

