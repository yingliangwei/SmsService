package com.example.smsservice;

import com.example.smsservice.entity.ContentEntity;
import com.example.smsservice.entity.UserEntity;
import com.example.smsservice.storage.GlobalStorage;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;


public class InitTableColumn {
    //储存选中
    private final FXMLLoader fxmlLoader;

    public InitTableColumn(FXMLLoader loader) {
        this.fxmlLoader = loader;
    }

    public void init() {
        TableColumn<UserEntity, String> checkboxCol_ip = findViewById("tab_ip");
        PropertyValueFactory<UserEntity, String> propertyValueFactory = new PropertyValueFactory<>("ip");
        checkboxCol_ip.setCellValueFactory(propertyValueFactory);
        checkboxCol_ip.setCellFactory(new Callback<>() {
            @Override
            public TableCell<UserEntity, String> call(TableColumn<UserEntity, String> userEntityStringTableColumn) {
                return new TextFieldTableCell<>() {
                    @Override
                    public void updateItem(String s, boolean b) {
                        super.updateItem(s, b);
                        if (s == null || b) {
                            return;
                        }
                        CheckBox checkBox = new CheckBox();
                        if (GlobalStorage.ips.contains(s)) {
                            checkBox.setSelected(true);
                        }
                        HBox hbox = new HBox();
                        hbox.setAlignment(Pos.CENTER);
                        hbox.getChildren().add(checkBox);
                        // 增加监听
                        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue) {
                                GlobalStorage.ips.add(s);
                            } else {
                                GlobalStorage.ips.remove(s);
                            }
                        });
                        this.setGraphic(hbox);
                    }
                };
            }
        });

        //判断类型
        TableColumn<UserEntity, String> checkbox_type = findViewById("tab_type");
        PropertyValueFactory<UserEntity, String> type = new PropertyValueFactory<>("type");
        checkbox_type.setCellValueFactory(type);
        checkbox_type.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<UserEntity, String> checkboxCol_phone = findViewById("tab_phone");
        PropertyValueFactory<UserEntity, String> phone = new PropertyValueFactory<>("phone");
        checkboxCol_phone.setCellValueFactory(phone);
        checkboxCol_phone.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<UserEntity, String> checkboxCol_time = findViewById("tab_time");
        PropertyValueFactory<UserEntity, String> factory = new PropertyValueFactory<>("time");
        checkboxCol_time.setCellValueFactory(factory);
        checkboxCol_time.setCellFactory(TextFieldTableCell.forTableColumn());

        //接收的号码储存
        TableView<ContentEntity> tab_receive_content = findViewById("tab_receive_content");
        tab_receive_content.setEditable(true);

        initTableColumn("tab_receive_content_phone", "phone");
        initTableColumn("tab_receive_content_send_phone", "send_phone");
        initTableColumn("tab_receive_content_content", "content");
        initTableColumn("tab_receive_content_time", "time");

        //发送号码
        TableView<ContentEntity> tab_send_content = findViewById("tab_send_content");
        tab_send_content.setEditable(true);

        initTableColumn("tab_send_content_phone", "phone");
        initTableColumn("tab_send_content_send_phone", "send_phone");
        initTableColumn("tab_send_content_content", "content");
        initTableColumn("tab_send_content_time", "time");
    }

    void initTableColumn(String id, String value) {
        TableColumn<ContentEntity, String> tab_receive_content_send_phone = findViewById(id);
        PropertyValueFactory<ContentEntity, String> contentEntityStringPropertyValueFactory1 = new PropertyValueFactory<>(value);
        tab_receive_content_send_phone.setCellValueFactory(contentEntityStringPropertyValueFactory1);
        tab_receive_content_send_phone.setCellFactory(TextFieldTableCell.forTableColumn());
    }

    <T> T findViewById(String value) {
        return (T) fxmlLoader.getNamespace().get(value);
    }
}
