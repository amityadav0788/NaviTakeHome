package test.java.com.mymoney;

import main.java.com.mymoney.datamodel.MyMoneyDao;
import main.java.com.mymoney.service.*;
import static main.java.com.mymoney.enums.MyMoneyAsset.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MyMoneyHandlerTest {
    private MyMoneyDao myMoneyDao;
    private MyMoneyFunctionInterface myMoneyFunction;
    private MyMoneyHandler myMoneyHandler;
  
    public void init() {
      myMoneyDao = new MyMoneyDao();
      myMoneyDao.assetOrder.add(EQUITY);
      myMoneyDao.assetOrder.add(DEBT);
      myMoneyDao.assetOrder.add(GOLD);
      myMoneyFunction = new MyMoneyFunctionImpl(myMoneyDao);
      myMoneyHandler = new MyMoneyHandler(myMoneyFunction);
    }
  
    
    void testInvalidFile() throws IOException {
      myMoneyHandler.readFile("");
      System.out.println("Target class should have thrown error but didn't");
    }
  
    
    void testValidFile() throws IOException {
      String inputFile =
          Objects.requireNonNull(this.getClass().getClassLoader().getResource("testInputFile"))
              .getFile();
      String outputFile =
          Objects.requireNonNull(this.getClass().getClassLoader().getResource("testOutputFile"))
              .getFile();
      List<String> output = myMoneyHandler.readFile(inputFile);
      try (Stream<String> lines = Files.lines(Paths.get(outputFile))) {
        String expectedResult = lines.map(String::trim).collect(Collectors.joining(";"));
        String result =
            output.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.joining(";"));
        if(result.equals(expectedResult)) {
          System.out.println("Results match");
        } else System.out.println("Results don't match");
      }
    }
  }
  
