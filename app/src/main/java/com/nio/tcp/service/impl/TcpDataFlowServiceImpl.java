package com.nio.tcp.service.impl;

import static com.nio.tcp.constants.TcpFlowConstants.BUFFER_CAPACITY;
import static com.nio.tcp.constants.TcpFlowConstants.CONTROL_PORT;
import static com.nio.tcp.constants.TcpFlowConstants.DATA_PORT;
import static com.nio.tcp.constants.TcpFlowConstants.HOSTNAME;
import static com.nio.tcp.constants.TcpFlowConstants.PORTS;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.nio.tcp.service.CommandService;
import com.nio.tcp.service.DataProcessingService;
import com.nio.tcp.service.TcpDataService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

public class TcpDataFlowServiceImpl implements TcpDataService {

  private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);
  private final Multimap<Integer, SocketChannel> clients = ArrayListMultimap.create();
  private final Selector selector;
  private final DataProcessingService dataProcessingService;
  private final CommandService commandService;

  public TcpDataFlowServiceImpl() throws IOException {
    selector = Selector.open();
    this.dataProcessingService = new DataProcessingServiceImpl();
    this.commandService = new CommandServiceImpl();
  }

  @Override
  public void runTcpDataFlow() throws IOException {
    System.out.printf("Tcp Data Flow Example started at %s:%s%n", HOSTNAME, Arrays.toString(PORTS));
    bindPorts();

    while (!Thread.currentThread().isInterrupted()) {
      System.out.printf("Wait new events..%n");
      selector.select();
      final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
      processSelectorKeys(iterator);
    }
  }

  private void processSelectorKeys(Iterator<SelectionKey> iterator) {
    iterator.forEachRemaining(selectionKey -> {
      try {
        acceptNewConnection(selectionKey);
        processIfClientKeyIsReadable(selectionKey);
        iterator.remove();
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  private void bindPorts() throws IOException {
    for (final int port : PORTS) {
      final ServerSocketChannel serverSocket = ServerSocketChannel.open();
      serverSocket.bind(new InetSocketAddress(HOSTNAME, port));
      serverSocket.configureBlocking(false);
      serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }
  }

  private void acceptNewConnection(SelectionKey selectionKey) throws IOException {
    if (selectionKey.isAcceptable()) {
      System.out.println("Handle New Connection");
      final ServerSocketChannel server = ((ServerSocketChannel) selectionKey.channel());
      final SocketChannel client = server.accept();
      client.configureBlocking(false);
      System.out.printf("New connection accepted: %s%n", client);

      clients.put(server.socket().getLocalPort(), client);

      client.register(selector, SelectionKey.OP_READ);
    }
  }

  private void processIfClientKeyIsReadable(SelectionKey selectionKey) throws IOException {
    if (selectionKey.isReadable()) {
      System.out.println("Handle READ event");
      final SocketChannel client = (SocketChannel) selectionKey.channel();
      final int read = client.read(buffer);

      if (read == -1) {
        closeClientConnection(client);
        return;
      }
      handleEventByClientPort(client);
    }
  }

  private void closeClientConnection(SocketChannel client) throws IOException {
    client.close();
    client.keyFor(selector).cancel();
    System.out.printf("The connection was closed: %s%n", client);
  }

  private void handleEventByClientPort(SocketChannel client) throws IOException {
    buffer.flip();
    int currentPort = client.socket().getLocalPort();
    switch (currentPort) {
      case DATA_PORT:
        client.write(dataProcessingService.convertInputToUppercase(buffer));
        break;
      case CONTROL_PORT:
        String command = commandService.getCommand(buffer);
        commandService.processCommand(client, command).ifPresent(this::registerEvent);
        break;
    }
    buffer.clear();
  }

  private void registerEvent(final int operation) {
    clients.get(DATA_PORT)
        .forEach(client -> {
          try {
            client.register(selector, operation);
          } catch (Exception e) {
            e.printStackTrace();
          }
        });
  }

}
