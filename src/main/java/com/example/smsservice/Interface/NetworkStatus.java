package com.example.smsservice.Interface;

import com.alibaba.fastjson2.JSONObject;
import com.example.smsservice.entity.UserEntity;

public interface NetworkStatus {
    void print(String text);

    void handle(UserEntity userEntity, JSONObject jsonObject);

    void Break(UserEntity userEntity);

    void end(String text);

    void connect(int size);

    void Break(int size);

}
