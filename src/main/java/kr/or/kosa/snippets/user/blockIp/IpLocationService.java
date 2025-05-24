package kr.or.kosa.snippets.user.blockIp;

public interface IpLocationService {
    String getCountry(String ip);
    String getCity(String ip);
}