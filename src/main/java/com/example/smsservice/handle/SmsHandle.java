package com.example.smsservice.handle;

import com.alibaba.fastjson2.JSONObject;
import com.example.smsservice.entity.ContentEntity;
import com.example.smsservice.entity.UserEntity;
import com.example.smsservice.storage.GlobalStorage;
import javafx.scene.control.TableView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SmsHandle {

    //处理接收到的信息
    public static void handle(TableView<UserEntity> tableView, TableView<ContentEntity> contentEntityTableView, UserEntity userEntity, JSONObject jsonObject) {
        String type = jsonObject.getString("type");
        if (type == null) {
            return;
        }
        if (type.equals("login")) {
            //记录设备手机号码
            String phone = jsonObject.getString("phone");
            GlobalStorage.remove_Phone(phone);
            userEntity.setPhone(phone);
            DateFormat currentTime = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");    //设置时间格式
            userEntity.setTime(currentTime.format(new Date()));
            GlobalStorage.add_User(tableView, userEntity);
        } else if (type.equals("sms")) {
            //接收短信
            JSONObject jsonObject1 = jsonObject.getJSONObject("context");
            String phone = jsonObject.getString("phone");//设备手机号码
            String senderNumber = jsonObject1.getString("senderNumber");
            String content = jsonObject1.getString("smsMessages");//内容
            ContentEntity contentEntity;
            contentEntity = new ContentEntity();
            contentEntity.setPhone(phone);
            contentEntity.setSend_phone(senderNumber);
            contentEntity.setContent(content);
            DateFormat currentTime = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");    //设置时间格式
            contentEntity.setTime(currentTime.format(new Date()));
            GlobalStorage.addContentEntities(contentEntityTableView, contentEntity);
        }
    }

}
