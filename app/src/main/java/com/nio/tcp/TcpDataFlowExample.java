package com.nio.tcp;

import com.nio.tcp.service.TcpDataService;
import com.nio.tcp.service.impl.TcpDataFlowServiceImpl;

public final class TcpDataFlowExample {

  public static void main(final String... args) throws Exception {
    TcpDataService service = new TcpDataFlowServiceImpl();
    service.runTcpDataFlow();
  }
}
