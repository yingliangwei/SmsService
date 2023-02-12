package com.example.smsservice.taskManager;

import com.alibaba.fastjson2.JSONObject;
import com.example.smsservice.entity.ContentEntity;
import com.example.smsservice.entity.UserEntity;
import com.example.smsservice.storage.GlobalStorage;
import com.example.smsservice.util.ListUtils;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//号码处理和分配
public class Distribution extends Thread {
    private boolean isStart;
    //内容
    private final String context;
    //暂停
    private final long millis;
    private final List<List<String>> partition;
    //发送显示的列表
    private final TableView<ContentEntity> tableView;
    private Send send;

    public void setIsStart(boolean isStart) {
        this.isStart = isStart;
        if (send != null) {
            send.setStart(isStart);
        }
    }

    public Distribution(TableView<ContentEntity> tableView, String context, long millis) {
        this.context = context;
        this.tableView = tableView;
        this.millis = millis;
        this.partition = ListUtils.partition(GlobalStorage.Numbers, GlobalStorage.Numbers.size() / GlobalStorage.ips.size());
    }

    @Override
    public void run() {
        if (GlobalStorage.Numbers.size() == 1) {
            for (int i = 0; i < GlobalStorage.Numbers.size(); i++) {
                List<String> text = new ArrayList<>();
                String numbers = GlobalStorage.Numbers.get(i);
                text.add(numbers);
                UserEntity userEntity = GlobalStorage.getIpUser(GlobalStorage.ips.get(i));
                if (userEntity == null) {
                    return;
                }
                send = new Send(tableView, userEntity, context, text, millis);
                send.start();
            }
        } else {
            //分配成多少组
            for (int i = 0; i < partition.size(); i++) {
                if (isStart) {
                    break;
                }
                List<String> strings = partition.get(i);
                UserEntity userEntity = GlobalStorage.getIpUser(GlobalStorage.ips.get(i));
                if (userEntity == null) {
                    return;
                }
                send = new Send(tableView, userEntity, context, strings, millis);
                send.start();
            }
        }
        //发送完毕，删除
        GlobalStorage.Numbers.clear();
    }

    public static class Send extends Thread {
        //号码集合
        private final List<String> strings;
        //涨停速度
        private final long millis;
        //连接设备
        private final UserEntity userEntity;
        //内容
        private final String content;
        private final TableView<ContentEntity> tableView;
        //停止服务
        private boolean isStart;

        public void setStart(boolean start) {
            isStart = start;
        }

        public Send(TableView<ContentEntity> tableView, UserEntity userEntity, String content, List<String> strings, long millis) {
            this.strings = strings;
            this.userEntity = userEntity;
            this.tableView = tableView;
            this.millis = millis;
            this.content = content;
        }

        @Override
        public void run() {
            try {
                for (String value : strings) {
                    if (isStart) {
                        break;
                    }
                    write(userEntity.getSocketChannel(), content, value);
                    try {
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void write(SocketChannel channel, String context, String send_phone) {
            ContentEntity contentEntity = new ContentEntity();
            contentEntity.setPhone(userEntity.getPhone());
            contentEntity.setSend_phone(send_phone);
            contentEntity.setContent(content);
            DateFormat currentTime = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");    //设置时间格式
            contentEntity.setTime(currentTime.format(new Date()));
            GlobalStorage.add_send_contentEntities(tableView, contentEntity);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "sms");
            jsonObject.put("send_phone", send_phone);
            jsonObject.put("content", context);
            //这里要记录数据库，然后提取id,好获取客户端发送状态
            jsonObject.put("id", "1");
            ByteBuffer writeBuffer = ByteBuffer.wrap(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
            try {
                channel.write(writeBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
