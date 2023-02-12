package com.example.smsservice;

import com.example.smsservice.service.NetState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL url = HelloApplication.class.getResource("hello-view.fxml");
        System.out.print(url);
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        Scene scene = new Scene(fxmlLoader.load(), 1100, 600);
        stage.setTitle("短信群发监控系统");
        stage.setScene(scene);
        stage.show();
        initView(fxmlLoader);
    }

    private void initView(FXMLLoader fxmlLoader) {
        InitTableColumn initTableColumn = new InitTableColumn(fxmlLoader);
        initTableColumn.init();
        initButton(fxmlLoader);
    }

    private void initButton(FXMLLoader fxmlLoader) {
        Label time = (Label) fxmlLoader.getNamespace().get("time");
        DateFormat currentTime = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");    //设置时间格式
        EventHandler<ActionEvent> eventHandler = e -> time.setText(currentTime.format(new Date()));
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(5000), eventHandler));                //一秒刷新一次
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();

        Label status = (Label) fxmlLoader.getNamespace().get("NetworkStatus");
        new Pint(code -> Platform.runLater(() -> status.setText("网络状态: " + code))).start();

        SplitPane splitPane1 = (SplitPane) fxmlLoader.getNamespace().get("splitPane1");
        SplitPane splitPane = (SplitPane) fxmlLoader.getNamespace().get("splitPane");

        lookupAll(splitPane);
        lookupAll(splitPane1);
    }

    //去除分割线条
    void lookupAll(SplitPane splitPane) {
        for (Node node : splitPane.lookupAll(".split-pane-divider")) {
            node.setVisible(false);
        }
    }

    private interface Pi {
        void message(String code);
    }

    private static class Pint extends Thread {
        private Pi pi;

        public Pint(Pi pi) {
            this.pi = pi;
        }

        @Override
        public void run() {
            do {
                pi.message(NetState.getTTL());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }


    static class ColorCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            Label label = new Label();
            if (item != null) {
                label.setText(item);
                setGraphic(label);
            }
        }

    }


    public static void main(String[] args) {
        launch();
    }
}