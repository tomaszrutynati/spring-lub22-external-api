package pl.sda.moviedb.external.exchangerate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

    /*
    W API NBP NIE MA TAKIEJ METODY, TO TYLKO POKAZANIE JAK ZROBIC POST !!!!!!!
     */
    public void publishCurrencyRate(Double rate, String currency, LocalDate forDate) {
        String address = String.format("%s/exchangerates/rates/", nbpProperties.getUrl());

        HttpHeaders headers = new HttpHeaders();
        //Jesli api wymaga dodania naglowkow mozemy to zrobic w ten sposob!
        //Czesto jest tak ze musimy dodac dwa naglowki content-type(application/json) i accept(application/json)
        headers.add("api-key", "12132321132");

        HttpEntity<CurrencyRateRq> entity = new HttpEntity<>(new CurrencyRateRq(rate, currency, forDate), headers);

        ResponseEntity<Void> rsp = restTemplate.exchange(address, HttpMethod.POST, entity, Void.class);

        if (!rsp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Currency rate override failure");
        }
    }

    @AllArgsConstructor
    @Getter
    public static class CurrencyRateRq {
        private Double rate;
        private String currency;
        private LocalDate forDate;
    }
}