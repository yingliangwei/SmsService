package com.example.smsservice.entity;

import javafx.scene.control.TableColumn;

import java.nio.channels.SocketChannel;

public class UserEntity {
    //手机号码
    private String phone;
    //连接的地址
    private SocketChannel socketChannel;
    private String ip;
    private String time;


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public UserEntity(String phone, SocketChannel channel, String time) {
        this.time = time;
        this.setSocketChannel(channel);
    }

    public UserEntity(SocketChannel channel) {
        this.setSocketChannel(channel);
        setIp(channel.socket().getInetAddress().getHostAddress());
    }


    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


}
