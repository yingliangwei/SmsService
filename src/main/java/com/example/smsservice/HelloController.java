package com.example.smsservice;

import com.alibaba.fastjson2.JSONObject;
import com.example.smsservice.Interface.NetworkStatus;
import com.example.smsservice.entity.ContentEntity;
import com.example.smsservice.entity.UserEntity;
import com.example.smsservice.handle.SmsHandle;
import com.example.smsservice.service.WinSocket;
import com.example.smsservice.storage.GlobalStorage;
import com.example.smsservice.taskManager.Distribution;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.ArrayList;
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
            //GlobalStorage.remove_user(tableView, userEntity);
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

    //负责接收到数据处理
    void handle(UserEntity userEntity, JSONObject jsonObject) {
        SmsHandle.handle(tableView, tab_receive_content, userEntity, jsonObject);
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

}