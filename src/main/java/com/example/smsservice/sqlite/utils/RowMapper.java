package com.example.smsservice.sqlite.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
    T mapRow(ResultSet rs, int index) throws SQLException;
}
