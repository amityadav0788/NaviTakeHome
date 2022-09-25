package main.java.com.mymoney.config;

import main.java.com.mymoney.datamodel.MyMoneyDao;
import static main.java.com.mymoney.enums.MyMoneyAsset.*;

public class MyMoneyConfig {
    public MyMoneyDao myMoneyDao() {
        MyMoneyDao dao = new MyMoneyDao();
        dao.assetOrder.add(EQUITY);
        dao.assetOrder.add(DEBT);
        dao.assetOrder.add(GOLD);
        return dao;
      }
}
