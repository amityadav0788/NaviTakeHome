package main.java.com.mymoney;

import java.util.InputMismatchException;
import main.java.com.mymoney.datamodel.MyMoneyDao;
import main.java.com.mymoney.service.MyMoneyHandler;
import main.java.com.mymoney.service.MyMoneyFunctionInterface;
import main.java.com.mymoney.service.MyMoneyFunctionImpl;
import static main.java.com.mymoney.enums.MyMoneyAsset.*;
import java.lang.String;

public class App {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new InputMismatchException(
                "Please specify input file");
          }
          String input = args[0];
          System.out.println(input);
          MyMoneyDao myMoneyDao = new MyMoneyDao();
          myMoneyDao.assetOrder.add(EQUITY);
          myMoneyDao.assetOrder.add(DEBT);
          myMoneyDao.assetOrder.add(GOLD);
          MyMoneyFunctionInterface myMoneyService = new MyMoneyFunctionImpl(myMoneyDao);
          MyMoneyHandler myMoneyHandler = new MyMoneyHandler(myMoneyService);
          myMoneyHandler.readFile(input);

    }
}
