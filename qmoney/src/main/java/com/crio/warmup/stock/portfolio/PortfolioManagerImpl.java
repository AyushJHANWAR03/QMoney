package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {
  public RestTemplate restTemplate;


  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

public StockQuotesService stockQuotesService;  
public PortfolioManagerImpl(StockQuotesService stockQuotesService) {
  this.stockQuotesService = stockQuotesService;
}

  //CHECKSTYLE:OFF
  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
   
        return stockQuotesService.getStockQuote(symbol, from, to);
  }

  protected String buildUri(String symbol, LocalDate StartDate, LocalDate endDate) {
      String token = "d6d387fc2ba8ed7fcb5417474d5dd04aa5313d6f";
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";

      return uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol)
            .replace("$STARTDATE", StartDate.toString())
            .replace("$ENDDATE", endDate.toString());      
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws StockQuoteServiceException {
        AnnualizedReturn annualizedReturn;
        List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
        for (int i = 0; i < portfolioTrades.size(); i++) {
          annualizedReturn = getAnnualizedReturn(portfolioTrades.get(i),endDate);
          annualizedReturns.add(annualizedReturn);
        }
        Collections.sort(annualizedReturns,getComparator());
        return annualizedReturns;
  }


  public  AnnualizedReturn getAnnualizedReturn(PortfolioTrade trade, LocalDate endDate) throws StockQuoteServiceException {
    AnnualizedReturn annualizedReturn;
    LocalDate startDate = trade.getPurchaseDate();
    String symbol = trade.getSymbol();

    try{
      List<Candle> stockStarttoEndDate;
      stockStarttoEndDate = getStockQuote(symbol, startDate, endDate);
      Candle stockStartDate = stockStarttoEndDate.get(0);
      Candle stockLatest = stockStarttoEndDate.get(stockStarttoEndDate.size() - 1);
      Double buyPrice = stockStartDate.getOpen();
      Double sellPrice = stockLatest.getClose();
      Double total_returns = (sellPrice - buyPrice) / buyPrice;

      Double num_years = (double) ChronoUnit.DAYS.between(startDate, endDate)/365d;
      Double annualizedReturns = Math.pow((1+total_returns), (1/num_years)) - 1;

      annualizedReturn = new AnnualizedReturn(symbol, annualizedReturns, total_returns);
    } catch (JsonProcessingException e) {
      annualizedReturn = new AnnualizedReturn(symbol, Double.NaN, Double.NaN);
    }
    return annualizedReturn;
  }
  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturnParallel(
      List<PortfolioTrade> portfolioTrades, LocalDate endDate, int numThreads)
      throws InterruptedException, StockQuoteServiceException {
        List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
  List<Future<AnnualizedReturn>> futureReturnsList = new ArrayList<Future<AnnualizedReturn>>();
  final ExecutorService pool = Executors.newFixedThreadPool(numThreads);

  for (int i = 0; i < portfolioTrades.size(); i++) {
    PortfolioTrade trade = portfolioTrades.get(i);
    Callable<AnnualizedReturn> callableTask = () -> {
      return getAnnualizedReturn(trade, endDate);
    };
    Future<AnnualizedReturn> futureReturns = pool.submit(callableTask);
    futureReturnsList.add(futureReturns);
  }

  for (int i = 0; i < portfolioTrades.size(); i++) {
    Future<AnnualizedReturn> futureReturns = futureReturnsList.get(i);
    try {
      AnnualizedReturn returns = futureReturns.get();
      annualizedReturns.add(returns);
    } catch (ExecutionException e) {
      throw new StockQuoteServiceException("Error when calling the API", e);

    }
  }
  Collections.sort(annualizedReturns, Comparator.comparingDouble(AnnualizedReturn::getAnnualizedReturn).reversed());

  return annualizedReturns;
}
    // TODO Auto-generated method stub

}
