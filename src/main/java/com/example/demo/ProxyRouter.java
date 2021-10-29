package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.camel.component.grpc.GrpcConstants;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.example.demo.Rectangle;
import com.example.demo.Point;
import com.example.demo.RouteSummary;
import com.example.demo.Feature;

@Component
public class ProxyRouter extends RouteBuilder {

  private static final Logger logger = LoggerFactory.getLogger(ProxyRouter.class);

  @Override
  public void configure() {

    from("grpc://0.0.0.0:9991/com.example.demo.RouteGuide?consumerStrategy=PROPAGATION&routeControlledStreamObserver=true")
      .to("log://before?showProperties=true&showHeaders=true")
      //.to("grpc://0.0.0.0:8980/com.example.demo.RouteGuide?method=recordRoute")  // error occur, stay comment
      .process(this::process);
  }

  private void process(Exchange exchange) {
    StreamObserver<Object> responseObserver
        = (StreamObserver<Object>) exchange.getProperty(GrpcConstants.GRPC_RESPONSE_OBSERVER);
        
    Message message = exchange.getIn();
    String eventType = (String)message.getHeader("CamelGrpcEventType");

    logger.info( "is Null = " + (responseObserver == null) + ", eventType = " + eventType + ".\n" );

  }
}