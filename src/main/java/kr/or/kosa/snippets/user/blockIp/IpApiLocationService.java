package kr.or.kosa.snippets.user.blockIp;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
/**
 *  IP 주소를 기반으로 지리적 위치(GeoIP)를 조회하는 무료 및 유료 API
 * */
@Service
public class IpApiLocationService implements IpLocationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getCountry(String ip) {
        Map<String, Object> data = getData(ip);
        return data.getOrDefault("country", "Unknown").toString();
    }

    @Override
    public String getCity(String ip) {
        Map<String, Object> data = getData(ip);
        return data.getOrDefault("city", "Unknown").toString();
    }

    private Map<String, Object> getData(String ip) {
        try {
            String url = "http://ip-api.com/json/" + ip;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            return Map.of("country", "Unknown", "city", "Unknown");
        }
    }
}