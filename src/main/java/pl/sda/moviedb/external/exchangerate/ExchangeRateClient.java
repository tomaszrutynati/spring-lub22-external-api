package pl.sda.moviedb.external.exchangerate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import pl.sda.moviedb.config.NbpProperties;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ExchangeRateClient {

    private final RestTemplate restTemplate;
    private final NbpProperties nbpProperties;
    private final ObjectMapper objectMapper;

    public CurrencyRateResponse findCurrencyRate(String currency, LocalDate forDate) throws JsonProcessingException {
        String address = String.format("%s/exchangerates/rates/a/%s/%s/?format=json",
                nbpProperties.getUrl(), currency, forDate.toString());

        ResponseEntity<String> rateRsp =
                restTemplate.exchange(address, HttpMethod.GET, HttpEntity.EMPTY, String.class);

        if (rateRsp.getStatusCode().is2xxSuccessful()) {
            return objectMapper.readValue(rateRsp.getBody(), CurrencyRateResponse.class);
        }

        if (rateRsp.getStatusCodeValue() == 404) {
            return findCurrencyRate(currency, forDate.minusDays(1));
        }

        throw new IllegalStateException("It was impossible to fetch currency rate " + rateRsp.getStatusCodeValue());
    }
}
///exchangerates/rates/a/usd/2016-04-04