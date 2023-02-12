package com.example.smsservice.service;

import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NetworkUtil {
    JSONObject jsonObject = new JSONObject();
    public int code = 0;
    public String message = "";
    public String type = "";

    public void put(String key, String value) {
        jsonObject.put(key, value);
    }

    public boolean write(SocketChannel socketChannel) {
        jsonObject.put("code", code);
        jsonObject.put("message", message);
        jsonObject.put("type", type);
        ByteBuffer writeBuffer = ByteBuffer.wrap(jsonObject.toString().getBytes());
        try {
            socketChannel.write(writeBuffer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public NetworkUtil() {
        jsonObject.put("code", code);
        jsonObject.put("message", message);
    }

    public NetworkUtil(int code) {
        this.code = code;

    }

    public NetworkUtil(String message) {
        this.message = message;
    }

    public NetworkUtil(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
