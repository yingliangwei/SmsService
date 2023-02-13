package com.example.smsservice.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.smsservice.Interface.NetworkStatus;
import com.example.smsservice.entity.UserEntity;
import com.example.smsservice.util.KillServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WinSocket extends Thread {
    private final List<UserEntity> channels = new ArrayList<>();
    private Selector selector;
    /**
     * 值得注意的是 Buffer 及其子类都不是线程安全的。
     */
    private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);//设置缓冲区大小
    private final NetworkStatus networkStatus;
    private final int port;
    private ServerSocketChannel serverSocket;

    public WinSocket(NetworkStatus networkStatus, int port) {
        this.networkStatus = networkStatus;
        this.port = port;
    }

    public List<UserEntity> getChannels() {
        return channels;
    }

    public UserEntity getUserEntity(SocketChannel socketChannel) {
        for (UserEntity channel : channels) {
            if (channel.getSocketChannel() == socketChannel) {
                return channel;
            }
        }
        return null;
    }

    public boolean remove(SocketChannel channel) {
        for (int i = 0; i < channels.size(); i++) {
            UserEntity entity = channels.get(i);
            try {
                if (entity.getSocketChannel() == channel) {
                    channels.remove(i);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void run() {
        initServer();
    }

    void initServer() {
        KillServer.killPort(new String[]{String.valueOf(port)});
        try {
            serverSocket = ServerSocketChannel.open();
            //非阻塞方式
            serverSocket.configureBlocking(false);
            serverSocket.bind(new InetSocketAddress(port));
            //创建选择器
            selector = Selector.open();
            //注册监听事件
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            networkStatus.print(port + "服务器开启成功");
            initData();
        } catch (IOException e) {
            networkStatus.end(e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        if (serverSocket != null) {
            try {
                selector.close();
                serverSocket.close();
                networkStatus.Break(0);
                networkStatus.print("服务器关闭成功");
                KillServer.killPort(new String[]{String.valueOf(port)});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //初始化
    private void initData() throws IOException {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                selector.select();
                if (!selector.isOpen()) {
                    break;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = keys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    }
                    //去除本次keyIterator.next()的对象,但不会对下次遍历有影响
                    keyIterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    //读取
    void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        this.readBuffer.clear();
        int numRead;
        try {
            //获取客户点的read操作读入数据块数量
            numRead = socketChannel.read(readBuffer);
        } catch (Exception e) {
            key.cancel();
            networkStatus.print(port + "断开连接客户端：" + socketChannel.socket().getInetAddress().getHostAddress());
            UserEntity userEntity = getUserEntity(socketChannel);
            networkStatus.Break(userEntity);
            remove(socketChannel);
            socketChannel.close();
            networkStatus.Break(channels.size());
            return;
        }
        if (numRead > 0) {
            //接收客户端信息,下面3个获取客户端IP的方法，可自行选择
            String recMsg = new String(readBuffer.array(), 0, numRead);
            //System.out.println("收到客户端 " + socket.getLocalAddress() + "信息:" + recMsg);
            if (JSON.isValidObject(recMsg)) {
                networkStatus.handle(getUserEntity(socketChannel), JSONObject.parseObject(recMsg));
            }
        } else {
            networkStatus.print(port + "断开连接客户端:" + socketChannel.socket().getInetAddress().getHostAddress());
            UserEntity userEntity = getUserEntity(socketChannel);
            networkStatus.Break(userEntity);
            remove(socketChannel);
            socketChannel.close();
            networkStatus.Break(channels.size());
        }
    }

    //接收连接
    void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        channels.add(new UserEntity(clientChannel));
        networkStatus.connect(channels.size());
        networkStatus.print(port + "成功连接客户端：" + serverSocketChannel.getLocalAddress());
    }
}
