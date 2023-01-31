package com.nio.tcp.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;

public interface CommandService {

  Optional<Integer> processCommand(SocketChannel client, String command) throws IOException;

  String getCommand(ByteBuffer buffer);
}
