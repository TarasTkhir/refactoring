package com.nio.tcp.constants;

public interface TcpFlowConstants {

  String HOSTNAME = "0.0.0.0";
  int CONTROL_PORT = 4444;
  int DATA_PORT = 5555;
  int[] PORTS = new int[]{DATA_PORT, CONTROL_PORT};
  int BUFFER_CAPACITY = 65535;
}
