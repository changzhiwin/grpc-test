# A problem
I want implement a grpc proxy using Camel, sloving little different between two proro files. But I am failed.

# What I used
I use the [routeguide](https://grpc.io/docs/languages/java/basics/) example for test (especially client). 
```
git clone -b v1.41.0 https://github.com/grpc/grpc-java.git
```
The access model of this test is below
```
     --------------------------------------------------------------------
    | $./build/install/examples/bin/route-guide-client(access port:9991)  |  
     --------------------------------------------------------------------
                                      |
                                      |
                                      v
             --------------------------------------------------
            |   (listen:9991)  Camel Proxy (access port:8980)  |
             --------------------------------------------------
                                      |
                                      |(not implement)
                                      v
        ----------------------------------------------------------------
      | (listen:8980) $./build/install/examples/bin/route-guide-server  |
        ----------------------------------------------------------------
```

# Some condition
- For demo reason, use the same route_guide.proto file
- For some error occur, not implement backend (Camle to route-guide-server)

# Method
Setting routeControlledStreamObserver to `true`, expect to get response observer object. 
Logging properties and headers of `Camel Message`, to see `GrpcConstants.GRPC_RESPONSE_OBSERVER` and `CamelGrpcEventType`.

# Result
There four type of call in the `route_guide.proto`. So test each of them.

- simple RPC
<table>
  <tr>
    <td><b>method name of RPC</b></td>
    <td>getFeature</td>
  </tr>
  <tr>
    <td><b>GrpcConstants.GRPC_RESPONSE_OBSERVER</b></td>
    <td>Object</td>
  </tr>
  <tr>
    <td><b>CamelGrpcEventType</b></td>
    <td>null</td>
  </tr>
</table>

```
Exchange[ExchangePattern: InOnly, Properties: {grpcResponseObserver=io.grpc.stub.ServerCalls$ServerCallStreamObserverImpl@79ccc8c6}, Headers: {CamelGrpcMethodName=getFeature, CamelGrpcUserAgent=grpc-java-netty/1.40.1, Content-Type=application/grpc}, BodyType: com.example.demo.Point, Body: latitude: 409146138longitude: -746188906]
```

- server-to-client streaming RPC
<table>
  <tr>
    <td><b>method name of RPC</b></td>
    <td>listFeatures</td>
  </tr>
  <tr>
    <td><b>GrpcConstants.GRPC_RESPONSE_OBSERVER</b></td>
    <td>Object</td>
  </tr>
  <tr>
    <td><b>CamelGrpcEventType</b></td>
    <td>null</td>
  </tr>
</table>

```
Exchange[ExchangePattern: InOnly, Properties: {grpcResponseObserver=io.grpc.stub.ServerCalls$ServerCallStreamObserverImpl@524ee9dd}, Headers: {CamelGrpcMethodName=listFeatures, CamelGrpcUserAgent=grpc-java-netty/1.40.1, Content-Type=application/grpc}, BodyType: com.example.demo.Rectangle, Body: lo {  latitude: 400000000  longitude: -750000000}hi {  latitude: 420000000  longitude: -730000000}]
```

- client-to-server streaming RPC
<table>
  <tr>
    <td><b>method name of RPC</b></td>
    <td>recordRoute</td>
  </tr>
  <tr>
    <td><b>GrpcConstants.GRPC_RESPONSE_OBSERVER</b></td>
    <td>null</td>
  </tr>
  <tr>
    <td><b>CamelGrpcEventType</b></td>
    <td>onNext</td>
  </tr>
</table>

```
Exchange[ExchangePattern: InOnly, Properties: {}, Headers: {CamelGrpcEventType=onNext, CamelGrpcMethodName=recordRoute, CamelGrpcUserAgent=grpc-java-netty/1.40.1, Content-Type=application/grpc}, BodyType: com.example.demo.Point, Body: latitude: 408472324longitude: -740726046]
```

- A Bidirectional streaming RPC
<table>
  <tr>
    <td><b>method name of RPC</b></td>
    <td>routeChat</td>
  </tr>
  <tr>
    <td><b>GrpcConstants.GRPC_RESPONSE_OBSERVER</b></td>
    <td>null</td>
  </tr>
  <tr>
    <td><b>CamelGrpcEventType</b></td>
    <td>onNext</td>
  </tr>
</table>

```
Exchange[ExchangePattern: InOnly, Properties: {}, Headers: {CamelGrpcEventType=onNext, CamelGrpcMethodName=routeChat, CamelGrpcUserAgent=grpc-java-netty/1.40.1, Content-Type=application/grpc}, BodyType: com.example.demo.RouteNote, Body: location {}message: "First message"]
```

# Error take place
When the `recordRoute` called, a error occurs, like this
```
2021-10-29 15:14:47.252  WARN 88138 --- [ault-executor-0] io.grpc.internal.ServerCallImpl          : Cancelling the stream with status Status{code=INTERNAL, description=Too many responses, cause=null}
```
I guess the producer of Camel GRPC EndPoint send finish event after frist request, immediately. 
But I don't know how to prevent this default manner.