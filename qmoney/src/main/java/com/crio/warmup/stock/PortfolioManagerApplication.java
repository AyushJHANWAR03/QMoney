
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Task:
  // - Read the json file provided in the argument[0], The file is available in the classpath.
  // - Go through all of the trades in the given file,
  // - Prepare the list of all symbols a portfolio has.
  // - if "trades.json" has trades lik
  // [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  // Then you should return ["MSFT", "AAPL", "GOOGL"]
  // Hints:
  // 1. Go through two functions provided - #resolveFileFromResources() and #getObjectMapper
  // Check if they are of any help to you.
  // 2. Return the list of all symbols in the same order as provided in json.

  // Note:
  // 1. There can be few unused imports, you will need to fix them to make the build pass.
  // 2. You can use "./gradlew build" to check if your code builds successfully.
  public static List<String> mainReadQuotes(String[] args)
      throws IOException, URISyntaxException, RuntimeException {
    RestTemplate restTemplate = new RestTemplate();
    LocalDate LastDate = LocalDate.parse(args[1]);
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);

    List<TotalReturnsDto> stockdata = new ArrayList<>();
    for (PortfolioTrade trade : trades) {

      String urls = prepareUrl(trade, LastDate, "d6d387fc2ba8ed7fcb5417474d5dd04aa5313d6f");
      TiingoCandle[] tiingoCandles = restTemplate.getForObject(urls, TiingoCandle[].class);
      // if (tiingoCandles != null && tiingoCandles.length > 0) {
        double closing_price = tiingoCandles[tiingoCandles.length - 1].getClose();
        stockdata.add(new TotalReturnsDto(trade.getSymbol(), closing_price));
      // }
    }
    stockdata.sort(Comparator.comparing(TotalReturnsDto::getClosingPrice));

    List<String> sortedSymbols =
        stockdata.stream().map(TotalReturnsDto::getSymbol).collect(Collectors.toList());

    return sortedSymbols;

  }

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    List<PortfolioTrade> trades =
        Arrays.asList(getObjectMapper().readValue(file, PortfolioTrade[].class));
    List<String> ls = new ArrayList<>();
    for (PortfolioTrade trade : trades) {
      ls.add(trade.getSymbol());
    }
    getObjectMapper().writeValue(file, trades);
    return ls;

  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.



  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
        .toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Follow the instructions provided in the task documentation and fill up the correct values for
  // the variables provided. First value is provided for your reference.
  // A. Put a breakpoint on the first line inside mainReadFile() which says
  // return Collections.emptyList();
  // B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  // following the instructions to run the test.
  // Once you are able to run the test, perform following tasks and record the output as a
  // String in the function below.
  // Use this link to see how to evaluate expressions -
  // https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  // 1. evaluate the value of "args[0]" and set the value
  // to the variable named valueOfArgument0 (This is implemented for your reference.)
  // 2. In the same window, evaluate the value of expression below and set it
  // to resultOfResolveFilePathArgs0
  // expression ==> resolveFileFromResources(args[0])
  // 3. In the same window, evaluate the value of expression below and set it
  // to toStringOfObjectMapper.
  // You might see some garbage numbers in the output. Dont worry, its expected.
  // expression ==> getObjectMapper().toString()
  // 4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  // second place from top to variable functionNameFromTestFileInStackTrace
  // 5. In the same window, you will see the line number of the function in the stack trace window.
  // assign the same to lineNumberFromTestFileInStackTrace
  // Once you are done with above, just run the corresponding test and
  // make sure its working as expected. use below command to do the same.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "assessments/trades.json";
    String resultOfResolveFilePathArgs0 =
        "/home/crio-user/workspace/ayushjhanwar12-ME_QMONEY_V2/qmoney/bin/test/assessments/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@1573f9fc";
    String functionNameFromTestFileInStackTrace = "ModuleOneTest.mainReadFile()";
    String lineNumberFromTestFileInStackTrace = "19:1";


    return Arrays.asList(
        new String[] {valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
            functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace});
  }
  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  // TODO:
  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    PortfolioTrade[] trades = getObjectMapper().readValue(file, PortfolioTrade[].class);
    List<PortfolioTrade> tradesList = new ArrayList<>(Arrays.asList(trades));
    return tradesList;
  }


  // TODO:
  // Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String startdate = trade.getPurchaseDate().toString();
    String lastdate = endDate.toString();
    
    return "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?" + "startDate="
        + startdate + "&endDate=" + lastdate + "&token=" + token;
  }

  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    Candle candle = candles.get(0);
    return candle.getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    Candle candle = candles.get(candles.size() - 1);
    return candle.getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = prepareUrl(trade, endDate, token);
    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] tiingoCandles = restTemplate.getForObject(url, TiingoCandle[].class);
    return Arrays.asList(tiingoCandles);
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) 
      throws IOException, URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        LocalDate endDate = LocalDate.parse(args[1]);
        List<PortfolioTrade> trades = readTradesFromJson(args[0]);
        List<AnnualizedReturn> annualizedReturnsList = new ArrayList<>();

        for (PortfolioTrade trade : trades) {
          String url = prepareUrl(trade, endDate, getToken());
          TiingoCandle[] tiingoCandles = restTemplate.getForObject(url, TiingoCandle[].class);
          
          Double buyPrice = getOpeningPriceOnStartDate(Arrays.asList(tiingoCandles));
          Double sellPrice = getClosingPriceOnEndDate(Arrays.asList(tiingoCandles));
          AnnualizedReturn annualizedReturn = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
          annualizedReturnsList.add(annualizedReturn);
        }
        annualizedReturnsList.sort(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed());
        return annualizedReturnsList;

   
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  static String getToken() {
    return "d6d387fc2ba8ed7fcb5417474d5dd04aa5313d6f";
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      double totalReturn = (sellPrice - buyPrice)/buyPrice;
      LocalDate purchaseDate = trade.getPurchaseDate();
      double total_num_years = ChronoUnit.DAYS.between(purchaseDate, endDate)/365d;
      double annualized_returns = Math.pow((1+totalReturn), (1/total_num_years)) - 1;
      return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn);
  }


  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents, PortfolioTrade[].class);
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  private static String readFileAsString(String file) {
    return file.toString();
  }
  
  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

