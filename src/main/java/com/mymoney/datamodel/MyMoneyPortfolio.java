package main.java.com.mymoney.datamodel;

import java.util.List;
import java.util.stream.Collectors;

import main.java.com.mymoney.entity.MyMoneyFundEntity;

public class MyMoneyPortfolio implements Cloneable {
    private final List<MyMoneyFundEntity> funds;
  
    public MyMoneyPortfolio(List<MyMoneyFundEntity> collect) {
        funds = collect;
    }

    @Override
    public MyMoneyPortfolio clone() {
      return new MyMoneyPortfolio(
          funds.stream().map(e -> new MyMoneyFundEntity(e.getAsset(), e.getAmount())).collect(Collectors.toList()));
    }
  
    @Override
    public String toString() {
      return funds.stream().map(entity -> Integer.toString((int) Math.floor((double) entity.getAmount()))).collect(Collectors.joining(" "));
    }
  
    /**
     * This method returns total investment across assets
     *
     * @return sumOfInvestment
     */
    public double getInvestment() {
      return funds.stream().mapToDouble(MyMoneyFundEntity::getAmount).sum();
    }

    /**
     * This method gets all funds across asset
     *
     * @return funds
     */
    public List<MyMoneyFundEntity> getFunds() {
        return funds;
    }
    
}
