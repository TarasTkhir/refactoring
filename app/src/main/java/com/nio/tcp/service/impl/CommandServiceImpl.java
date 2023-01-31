package com.nio.tcp.service.impl;

import com.nio.tcp.service.CommandService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class CommandServiceImpl implements CommandService {

  private final String START_READ_COMMAND = "start-read";
  private final String STOP_READ_COMMAND = "stop-read";

  @Override
  public Optional<Integer> processCommand(SocketChannel client, String command) throws IOException {
    switch (command) {
      case START_READ_COMMAND:
        return getStartReadCommand();
      case STOP_READ_COMMAND:
        return getStopReadCommand();
      default:
        unsupportedCommand(client);
        return Optional.empty();
    }
  }

  @Override
  public String getCommand(ByteBuffer buffer) {
    return StandardCharsets.UTF_8.decode(buffer)
        .toString()
        .trim()
        .toLowerCase();
  }

  private Optional<Integer> getStartReadCommand() {
    System.out.printf("Handle %s%n", START_READ_COMMAND);
    return Optional.of(SelectionKey.OP_READ);
  }

  private Optional<Integer> getStopReadCommand() {
    System.out.printf("Handle %s%n", STOP_READ_COMMAND);
    return Optional.of(0);
  }

  private void unsupportedCommand(SocketChannel client) throws IOException {
    final byte[] unknownCommand = String.format("Supported commands:%n%s%n%s%n", STOP_READ_COMMAND, START_READ_COMMAND)
        .getBytes(StandardCharsets.UTF_8);
    client.write(ByteBuffer.wrap(unknownCommand));
  }
}
