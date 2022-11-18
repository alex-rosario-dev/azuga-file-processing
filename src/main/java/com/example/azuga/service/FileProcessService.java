package com.example.azuga.service;

import com.example.azuga.model.FileData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@CommonsLog
public class FileProcessService {

    @Scheduled(cron = "0 */1 * ? * *")
    public void processFile()  {
        XmlMapper xmlMapper = new XmlMapper();
        ClassPathResource classPathResource = new ClassPathResource("rawFile.xml");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesBefore = now.minusMinutes(10);
        String jsonFileNameFormat = "JSON_Output";
        String txtFileNameFormat = "TEXT_Output";
        String jsonFileLocation = "reports/json/";
        String txtFileLocation = "reports/txt/";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        File file;
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        try {
            
            FileData fileData = xmlMapper.readValue(classPathResource.getFile(), FileData.class);
            log.info(String.format("Reading raw file: %s", fileData.toString()));
            fileData.setTimeStamp(now);
            
            String jsonData = mapper.writeValueAsString(fileData);
            log.info(String.format("JSON Data: %s", jsonData));
            
            String fileName = String.format("%s%s - %s", jsonFileLocation, jsonFileNameFormat, Instant.now());
            file = new File(fileName);
            FileUtils.writeStringToFile(file, jsonData, StandardCharsets.UTF_8);
            List<LocalDateTime> temperatures = new ArrayList<>();
            try (Stream<Path> paths = Files.walk(Paths.get(jsonFileLocation))) {
                paths
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            String jsonFileName = String.valueOf(path.getFileName());
                            String fileDateTime = jsonFileName.substring(jsonFileNameFormat.length()+3);
                            LocalDateTime fileLocalDateTime = LocalDateTime.parse(fileDateTime, formatter);
                            if(fileLocalDateTime.isAfter(tenMinutesBefore)) {
                                log.info(String.format("Processing File - %s", jsonFileName));
                                try {
                                    FileData jsonFileData = mapper.readValue(path.toFile(), FileData.class);
                                    if(jsonFileData.getTemperature() > 25) {
                                        temperatures.add(jsonFileData.getTimeStamp());
                                    }
                                } catch (IOException e) {
                                    log.error("Error in reading json file: ", e);
                                }
                            }
                        });
            }
            if(!temperatures.isEmpty()) {
                String txtFileName = String.format("%s%s - %s", txtFileLocation, txtFileNameFormat, Instant.now());
                file = new File(txtFileName);
                FileUtils.writeStringToFile(file, temperatures.toString(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("Error in file processing", e);
        }

    }
}
