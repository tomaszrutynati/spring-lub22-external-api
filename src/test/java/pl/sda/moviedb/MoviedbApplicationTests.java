package pl.sda.moviedb;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.sda.moviedb.external.exchangerate.CurrencyRateResponse;
import pl.sda.moviedb.external.exchangerate.ExchangeRateClient;

import java.time.LocalDate;

@SpringBootTest
class MoviedbApplicationTests {

    @Autowired
    private ExchangeRateClient exchangeRateClient;

    @Test
    void shouldFetchExchangeRate() throws JsonProcessingException {
        //given
        String currency = "eur";
        LocalDate forDate = LocalDate.now();
        //when
        CurrencyRateResponse rate = exchangeRateClient.findCurrencyRate(currency, forDate);
        //then
        Assertions.assertNotNull(rate);
    }

    @Test
    void contextLoads() {
    }

}
