package com.example.smsservice.sqlite;


import com.example.smsservice.sqlite.utils.SqliteHelper;

import java.io.File;

public class SQLiteJDBC {
    public static SqliteHelper init() throws Exception {
        // db 文件存放路径地址
        String dbPath = System.getProperty("user.dir") + File.separator;
        // 1、创建sqlite连接
        String dbFilePath = dbPath + "new_file";
        // 需要判断文件是否存在，不存在则优先创建 .db 文件
        File dbFile = new File(dbFilePath);
        // 如果父路径不存在，则先创建父路径
        if (!dbFile.getParentFile().exists()) {
            boolean mk = dbFile.getParentFile().mkdirs();
        }
        // 如果文件不存在，则创建文件
        if (!dbFile.exists()) {
            boolean cj = dbFile.createNewFile();
        }
        // 建立连接
        return new SqliteHelper(dbFilePath);
    }
}
