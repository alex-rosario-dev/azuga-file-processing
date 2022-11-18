package com.example.azuga.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileData {
    private String id;
    private int temperature;
    private int humidity;
    private String location;
    private LocalDateTime timeStamp;
}
