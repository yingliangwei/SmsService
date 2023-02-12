package com.example.smsservice.test;


import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Test {
    private static final String host = "127.0.0.1";
    private static final int port = 808;

   /* public static void main(String[] args) {
        try {
            SocketChannel channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(host, port));
            //设置客户端请求为非阻塞方式
            channel.configureBlocking(false);
            //创建选择器
            Selector selector = Selector.open();
            //注册监听事件
            channel.register(selector, SelectionKey.OP_READ);

            new ProcessData(channel, selector).start();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "login");
            jsonObject.put("phone", "10068");
            ByteBuffer writeBuffer = ByteBuffer.wrap(jsonObject.toString().getBytes());
            channel.write(writeBuffer);

            Thread.sleep(5000);

            JSONObject sms = new JSONObject();
            sms.put("type", "sms");
            sms.put("phone", "10086");
            sms.put("content","短信内容");
            sms.put("send_phone", "110");
            ByteBuffer wrap = ByteBuffer.wrap(sms.toString().getBytes(StandardCharsets.UTF_8));
            channel.write(wrap);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
*/

    //处理服务端数据
    private static class ProcessData extends Thread {
        private final SocketChannel channel;
        private final Selector selector;


        public ProcessData(SocketChannel channel, Selector selector) {
            this.channel = channel;
            this.selector = selector;
        }

        public void run() {
            while (true) {
                try {
                    if (selector.select() > 0) {
                        Set<SelectionKey> keys = selector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = keys.iterator();
                        handler(keyIterator);
                    }
                } catch (Exception e) {
                    System.out.print("断开连接");
                    System.out.print("" + e);
                    e.fillInStackTrace();
                    break;
                }
            }
        }

        private void handler(Iterator<SelectionKey> keyIterator) throws IOException {
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                if (key.isConnectable()) {
                    success();
                    break;
                } else if (key.isReadable()) {
                    handleMessage(key);
                }
            }
        }

        //信息处理
        void handleMessage(SelectionKey key) throws IOException {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.clear();
            long byteRead = clientChannel.read(byteBuffer);
            if (byteRead == -1) {
                clientChannel.close();
            } else {
                byteBuffer.flip();
                String receiveMsg = new String(byteBuffer.array(), 0, byteBuffer.limit());
                System.out.println("接收来自服务器的消息：" + receiveMsg);
            }
        }

        //连接成功
        private void success() throws IOException {
            channel.finishConnect();
            channel.register(selector, SelectionKey.OP_READ);

        }
    }
}
