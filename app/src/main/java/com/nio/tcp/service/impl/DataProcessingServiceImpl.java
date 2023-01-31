package com.nio.tcp.service.impl;

import com.nio.tcp.service.DataProcessingService;
import java.nio.ByteBuffer;

public class DataProcessingServiceImpl implements DataProcessingService {

  @Override
  public ByteBuffer convertInputToUppercase(ByteBuffer buffer) {
    for (int i = 0; i < buffer.limit(); i++) {
      buffer.put(i, (byte) Character.toUpperCase(buffer.get(i)));
    }
    return buffer;
  }
}
