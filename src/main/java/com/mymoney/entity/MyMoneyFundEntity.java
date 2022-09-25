package main.java.com.mymoney.entity;

import main.java.com.mymoney.enums.MyMoneyAsset;


public class MyMoneyFundEntity {
    private MyMoneyAsset asset;
    private Double amount;

    public MyMoneyFundEntity(MyMoneyAsset asset2, Double amount2) {
        asset = asset2;
        amount = amount2;
    }
    public MyMoneyAsset getAsset() {
        return asset;
    }
    public Double getAmount() {
        return amount;
    }
    public void setAmount(double amt) {
        amount = amt;
    }
    public void setAsset(MyMoneyAsset assetv) {
        asset = assetv;
    }
  }
