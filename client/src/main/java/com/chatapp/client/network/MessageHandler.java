package com.chatapp.client.network;

import com.chatapp.common.protocol.Packet;

public interface MessageHandler {
    void handleMessage(Packet packet);
}