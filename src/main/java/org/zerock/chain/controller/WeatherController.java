package org.zerock.chain.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@RestController
public class WeatherController {

    @CrossOrigin(origins = "*") // CORS 설정
    @GetMapping("/weather")
    public ResponseEntity<String> getWeather() {
        String serviceKey = "zy19AGtN3cyyLS50Oi4E9NFzFKytPbZZxbtIh1CBfsF8eBgL80IQi1qcGhFYLqLDxvOEXVpBw%2Fd8FnUgl6ywMA%3D%3D"; // 기상청 API 포털에서 발급받은 유효한 API 키
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=" + serviceKey + "&numOfRows=10&pageNo=1&base_date=20210628&base_time=0500&nx=55&ny=127&dataType=JSON";
        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.getForObject(apiUrl, String.class);
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }
}

