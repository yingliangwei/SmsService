module com.example.smsservice {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.alibaba.fastjson2;
    requires java.sql;


    opens com.example.smsservice to javafx.fxml;
    exports com.example.smsservice;
    opens com.example.smsservice.entity to javafx.base;
}