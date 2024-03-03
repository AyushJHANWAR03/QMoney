
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.codec.multipart.Part;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {


  private RestTemplate restTemplate;
  public static final String TOKEN = "d6d387fc2ba8ed7fcb5417474d5dd04aa5313d6f";

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    // TODO Auto-generated method stub

   
    if(from.compareTo(to) >= 0){
      throw new RuntimeException();
    }
    String uri = buildUri(symbol, from, to);
    List<Candle> stockstartstoenddate = new ArrayList<>();
    try{
      String stocks = restTemplate.getForObject(uri, String.class);
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      TiingoCandle[] stockstartstoenddateArray = objectMapper.readValue(stocks, TiingoCandle[].class);
      stockstartstoenddate = Arrays.asList(stockstartstoenddateArray);
    } catch(NullPointerException e){
      throw new StockQuoteServiceException("Error occcured while requesting from tiingo api");
    }

    return stockstartstoenddate;
      }

  private String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = String.format("https://api.tiingo.com/tiingo/daily/%s/prices?"
            + "startDate=%s&endDate=%s&token=%s",symbol,startDate,endDate,TOKEN);

        return uriTemplate;
        
  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest


  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.

}
