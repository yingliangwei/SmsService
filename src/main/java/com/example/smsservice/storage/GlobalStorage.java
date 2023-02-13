package com.example.smsservice.storage;

import com.example.smsservice.entity.ContentEntity;
import com.example.smsservice.entity.UserEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

public class GlobalStorage {
    //储存登录连接的设备
    public static final ObservableList<UserEntity> userEntities = FXCollections.observableArrayList();
    //储存接收的短信
    public static final ObservableList<ContentEntity> contentEntities = FXCollections.observableArrayList();
    //储存发送的短信
    public static final ObservableList<ContentEntity> contentEntities1 = FXCollections.observableArrayList();
    //储存选中设备的ip
    public static final List<String> ips = new ArrayList<>();
    //储存要发送的号码
    public static final List<String> Numbers = new ArrayList<>();

    public static void addContentEntities(TableView<ContentEntity> contentEntityTableView, ContentEntity contentEntity) {
        contentEntities.add(contentEntity);
        contentEntityTableView.setItems(GlobalStorage.contentEntities);
    }

    //提供ip获取他是连接设备
    public static UserEntity getIpUser(String ip) {
        for (UserEntity userEntity : userEntities) {
            if (userEntity.getIp().equals(ip)) {
                return userEntity;
            }
        }
        return null;
    }


    public static void addNumbers(String[] strings) {
        for (String key : strings) {
            boolean is = false;
            for (String value : Numbers) {
                if (key.equals(value)) {
                    is = true;
                    break;
                }
            }
            //不存在，则储存
            if (!is) {
                Numbers.add(key);
            }
        }
    }

    //储存发送号码
    public static void add_send_contentEntities(TableView<ContentEntity> tableView, ContentEntity contentEntity) {
        contentEntities1.add(contentEntity);
        tableView.setItems(GlobalStorage.contentEntities1);
    }

    public static void add_User(TableView<UserEntity> tableView, UserEntity value) {
        for (UserEntity userEntity : userEntities) {
            if (userEntity.getPhone().equals(value.getPhone())) {
                userEntities.add(value);
                tableView.setItems(GlobalStorage.userEntities);
            }
        }
        userEntities.add(value);
        tableView.setItems(GlobalStorage.userEntities);
    }


    //删除登录
    public static void remove_user(TableView<UserEntity> tableView, UserEntity Entity) {
        for (int i = 0; i < userEntities.size(); i++) {
            UserEntity userEntity = userEntities.get(i);
            if (userEntity.getPhone().equals(Entity.getPhone())) {
                if (userEntity.getIp().equals(Entity.getIp())) {
                    userEntities.remove(i);
                }
            }
        }
        tableView.setItems(GlobalStorage.userEntities);
    }


    public static void remove_Phone(String Phone) {
        userEntities.removeIf(userEntity -> userEntity.getPhone().equals(Phone));
    }
}
