package com.example.em.service;

import com.example.em.domain.EMDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final RestTemplate restTemplate;
    private final EMService emService;

    public List<EMDto.HospitalLog> getLogs(String url) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                    String datetimeString = json.getAsString();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return LocalDateTime.parse(datetimeString, formatter);
                })
                .create();

        EMDto.Log[] emLogsArray = gson.fromJson(response.getBody(), EMDto.Log[].class);
        List<EMDto.Log> emLogsList = Arrays.asList(emLogsArray);

        return emService.transformLog(emLogsList);
    }

    public List<EMDto.LogChart> getChartLogs(List<EMDto.HospitalLog> logs) {
        return logs.stream()
                .map(hospitalLog -> {
                    EMDto.LogChart logChart = new EMDto.LogChart();
                    logChart.setDatetime(hospitalLog.getDatetime());
                    logChart.setEm_class(hospitalLog.getEm_class());
                    return logChart;
                })
                .collect(Collectors.toList());
    }

}
