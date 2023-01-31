package com.nio.tcp.service;

import java.nio.ByteBuffer;

public interface DataProcessingService {

  ByteBuffer convertInputToUppercase(ByteBuffer buffer);
}
