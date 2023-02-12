package com.example.smsservice;

import com.alibaba.fastjson2.JSONObject;
import com.example.smsservice.Interface.NetworkStatus;
import com.example.smsservice.entity.ContentEntity;
import com.example.smsservice.entity.UserEntity;
import com.example.smsservice.service.WinSocket;
import com.example.smsservice.storage.GlobalStorage;
import com.example.smsservice.taskManager.Distribution;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HelloController {
    //群发手机号码
    public TextArea SendMobileNumber;
    //每个设备发送速度
    public TextField DistributionSpeed;
    //端口
    public TextField port;
    //服务器日志
    public TextArea ServerLog;
    //连接设备数量
    public Label connections;
    //发送内容
    public TextArea SendContext;
    //时间
    public Label time;
    //网络状态
    public Label NetworkStatus;
    //开启服务器
    public Button Button_Server;

    public TableView<UserEntity> tableView;
    public TableView<ContentEntity> tab_receive_content;
    public TableView<ContentEntity> tab_send_content;
    public TextArea log;
    public TextField port1;
    public Button Button_Server1;
    public Button button_start;
    private WinSocket winSocket;

    //发送短信
    private final NetworkStatus networkStatus = new NetworkStatus() {
        @Override
        public void print(String text) {
            ServerLog_Print(text);
        }

        @Override
        public void handle(UserEntity userEntity, JSONObject jsonObject) {
            if (userEntity == null) {
                System.out.print("UserEntity null");
                return;
            }
            HelloController.this.handle(userEntity, jsonObject);
        }

        @Override
        public void Break(UserEntity userEntity) {
            GlobalStorage.remove_user(tableView, userEntity);
        }

        @Override
        public void end(String text) {
            ServerLog_Print(text);
        }

        @Override
        public void connect(int size) {
            setConnections(size);
        }

        @Override
        public void Break(int size) {
            setConnections(size);
        }
    };

    //收短信通知
    private final NetworkStatus networkStatus1 = new NetworkStatus() {
        @Override
        public void print(String text) {
            ServerLog_Print(text);
        }

        @Override
        public void handle(UserEntity userEntity, JSONObject jsonObject) {
            if (userEntity == null) {
                System.out.print("UserEntity null");
                return;
            }
            HelloController.this.handle1(userEntity, jsonObject);
        }

        @Override
        public void Break(UserEntity userEntity) {
            GlobalStorage.remove_user(tableView, userEntity);
        }

        @Override
        public void end(String text) {

        }

        @Override
        public void connect(int size) {
            setConnections(size);
        }

        @Override
        public void Break(int size) {
            setConnections(size);
        }
    };

    //负责接收短信
    private void handle1(UserEntity userEntity, JSONObject jsonObject) {
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
            userEntity.setType("接收");
            GlobalStorage.add_User(tableView, userEntity);
        } else if (type.equals("sms")) {
            System.out.print(jsonObject + "\n");
            JSONObject jsonObject1 = jsonObject.getJSONObject("context");
            //设备上传的短信
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
            GlobalStorage.contentEntities.add(contentEntity);
            tab_receive_content.setItems(GlobalStorage.contentEntities);
        }
    }

    private WinSocket winSocket1;

    //负责发送
    void handle(UserEntity userEntity, JSONObject jsonObject) {
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
            userEntity.setType("发送");
            GlobalStorage.add_User(tableView, userEntity);
        }
    }

    /**
     * @param phone      设备手机号码
     * @param send_phone 目的号码
     * @param context    内容
     */
    void sendMessage(String phone, String send_phone, String context) {
        ContentEntity contentEntity = new ContentEntity();
        contentEntity.setPhone(phone);
        contentEntity.setSend_phone(send_phone);
        contentEntity.setContent(context);
        DateFormat currentTime = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");    //设置时间格式
        contentEntity.setTime(currentTime.format(new Date()));
        GlobalStorage.contentEntities1.add(contentEntity);
        tab_send_content.setItems(GlobalStorage.contentEntities1);
    }

    void setConnections(int size) {
        Platform.runLater(() -> HelloController.this.connections.setText(String.format("可分配设备数量：%s", size)));
    }

    private Distribution distribution;

    //开始群发
    public void StartDistribution() {
        String[] name = SendMobileNumber.getText().split("\\|");
        if (name.length == 0) {
            log.appendText("可分配号码为空\n");
            return;
        }
        if (DistributionSpeed.getText().length() == 0) {
            log.appendText("内容为空\n");
            return;
        }
        if (GlobalStorage.ips.size() == 0) {
            log.appendText("可分配设备为空\n");
            return;
        }
        //储存号码
        GlobalStorage.addNumbers(name);
        String mills = DistributionSpeed.getText();
        long millis = 0;
        if (mills.length() != 0) {
            millis = Long.parseLong(DistributionSpeed.getText());
        }
        if (distribution != null && distribution.isInterrupted()) {
            //运行中,退出循环
            distribution.setIsStart(true);
            distribution = null;
            button_start.setText("关闭分配群发");
        }
        button_start.setText("开始分配群发");
        distribution = new Distribution(tab_send_content, SendContext.getText(), millis);
        distribution.start();
    }

    //开启服务器
    public void StartServer() {
        if (this.port.getText().equals("")) {
            ServerLog_Print("端口不能为空！");
            return;
        }
        if (winSocket == null) {
            int port = Integer.parseInt(this.port.getText());
            winSocket = new WinSocket(networkStatus, port);
        }
        if (this.Button_Server.getText().equals("开启发送短信服务器")) {
            winSocket.start();
            this.Button_Server.setText("关闭发送短信服务器");
        } else {
            winSocket.close();
            winSocket = null;
            this.Button_Server.setText("开启发送短信服务器");
        }
    }


    void ServerLog_Print(String text) {
        Platform.runLater(() -> HelloController.this.ServerLog.appendText(text + "\n"));
    }

    //直接发送短信
    public void button_send(ActionEvent actionEvent) {
        String[] name = SendMobileNumber.getText().split("\\|");
        if (name.length == 0) {
            log.appendText("可分配号码为空\n");
            return;
        }
        if (SendContext.getText().length() == 0) {
            log.appendText("内容为空\n");
            return;
        }
        if (GlobalStorage.ips.size() == 0) {
            log.appendText("可分配设备为空\n");
            return;
        }
        //储存号码
        GlobalStorage.addNumbers(name);
        String mills = DistributionSpeed.getText();
        long millis = 0;
        if (mills.length() != 0) {
            millis = Long.parseLong(DistributionSpeed.getText());
        }
        for (int i = 0; i < GlobalStorage.Numbers.size(); i++) {
            List<String> text = new ArrayList<>();
            String numbers = GlobalStorage.Numbers.get(i);
            text.add(numbers);
            UserEntity userEntity = GlobalStorage.getIpUser(GlobalStorage.ips.get(i));
            if (userEntity == null) {
                return;
            }
            Distribution.Send send = new Distribution.Send(tab_send_content, userEntity, SendContext.getText(), text, millis);
            send.start();
        }
        //发送完毕，删除
        GlobalStorage.Numbers.clear();
    }


    //开始收短信的服务器
    public void StartServer1(ActionEvent actionEvent) {
        if (this.port.getText().equals("")) {
            ServerLog_Print("端口不能为空！");
            return;
        }
        if (winSocket1 == null) {
            int port = Integer.parseInt(this.port1.getText());
            winSocket1 = new WinSocket(networkStatus1, port);
        }
        if (this.Button_Server1.getText().equals("开启收短信服务器")) {
            winSocket1.start();
            this.Button_Server1.setText("关闭收短信服务器");
        } else {
            winSocket1.close();
            winSocket1 = null;
            this.Button_Server1.setText("开启收短信服务器");
        }
    }
}