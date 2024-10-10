package ru.nightmirror.wlbytime.models;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Builder
public class DatabaseSettings {
    File localStorageDir;
    String type;
    String address;
    String databaseName;
    boolean userUserAndPassword;
    String user;
    String password;
    List<String> params;
}
