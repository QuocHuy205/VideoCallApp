package com.chatapp.server.core;

import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistry {
    private static ClientRegistry instance;
    private final ConcurrentHashMap<Long, ClientHandler> clients;

    private ClientRegistry() {
        this.clients = new ConcurrentHashMap<>();
    }

    public static ClientRegistry getInstance() {
        if (instance == null) {
            synchronized (ClientRegistry.class) {
                if (instance == null) {
                    instance = new ClientRegistry();
                }
            }
        }
        return instance;
    }

    public void addClient(Long userId, ClientHandler handler) {
        clients.put(userId, handler);
    }

    public void removeClient(Long userId) {
        clients.remove(userId);
    }

    public ClientHandler getClient(Long userId) {
        return clients.get(userId);
    }

    public boolean isOnline(Long userId) {
        return clients.containsKey(userId);
    }

    public int getOnlineCount() {
        return clients.size();
    }
}