package com.distelli.europa.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Collectors;
import com.distelli.europa.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SslSettings {
    public static ObjectMapper OM = new ObjectMapper();
    public static String SERVER_PRIVATE_KEY = "serverPrivateKey";
    public static String SERVER_CERTIFICATE = "serverCertificate";
    public static String AUTHORITY_PRIVATE_KEY = "authorityPrivateKey";
    public static String AUTHORITY_CERTIFICATE = "authorityCertificate";
    public static String DNS_NAME = "dnsName";

    protected String serverPrivateKey;
    protected String serverCertificate;
    protected String authorityPrivateKey;
    protected String authorityCertificate;
    protected String dnsName;

    public static SslSettings fromEuropaSettings(List<EuropaSetting> settings) {
        if ( settings.isEmpty() ) return null;
        return OM.convertValue(EuropaSetting.asMap(settings), SslSettings.class);
    }

    public List<EuropaSetting> toEuropaSettings() {
        Map<String, String> settings =  OM.convertValue(this, new TypeReference<Map<String, String>>(){});
        return settings.entrySet().stream()
            .map((entry) -> EuropaSetting.builder()
                 .domain(Constants.DOMAIN_ZERO)
                 .key(entry.getKey())
                 .value(entry.getValue())
                 .type(EuropaSettingType.SSL)
                 .build())
            .collect(Collectors.toList());
    }
}
