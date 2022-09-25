package test.java.com.mymoney;

import main.java.com.mymoney.datamodel.MyMoneyDao;
import main.java.com.mymoney.service.MyMoneyFunctionImpl;
import static main.java.com.mymoney.enums.MyMoneyAsset.*;

import java.util.Arrays;
import java.util.List;
import java.util.zip.DataFormatException;

public class MyMoneyFunctionImplTest {
    private MyMoneyDao myMoneyDao;
    private MyMoneyFunctionImpl myMoneyFunction;

    public void init() {
        myMoneyDao.assetOrder.add(EQUITY);
        myMoneyDao.assetOrder.add(DEBT);
        myMoneyDao.assetOrder.add(GOLD);
        myMoneyFunction = new MyMoneyFunctionImpl(myMoneyDao);
    }

    void testAllocate() throws DataFormatException {
        try {
            myMoneyFunction.allocate(Arrays.asList(10d, 20d, 30d, 40d));
        } catch(DataFormatException e) {
            System.out.println("Successful in capturing incorrect values");
        }
      }

    void testSip() throws DataFormatException {
        List<Double> sipAmounts = Arrays.asList(10d, 20d, 30d);
        myMoneyFunction.sip(sipAmounts);
        if(sipAmounts.size() != myMoneyDao.initSip.getFunds().size()) {
            System.out.println("Incorrect size format");
        } else if(sipAmounts.stream().mapToDouble(Double::doubleValue).sum() != myMoneyDao.initSip.getInvestment()) {
            System.out.println("Incorrect SIP function output");
        }
    }
}
