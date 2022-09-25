package main.java.com.mymoney.datamodel;

import java.time.Month;
import java.util.*;

import main.java.com.mymoney.enums.MyMoneyAsset;

public class MyMoneyDao {
  private TreeMap<Month, MyMoneyPortfolio> monthBal = new TreeMap<>();
  private TreeMap<Month, Map<MyMoneyAsset, Double>> monthChangeR = new TreeMap<>();
  public SortedMap<Month, MyMoneyPortfolio> monthBalance = Collections.synchronizedSortedMap(monthBal);  
  public SortedMap<Month, Map<MyMoneyAsset, Double>> monthChangeRate = Collections.synchronizedSortedMap(monthChangeR);
  public MyMoneyPortfolio initAllocate;
  public MyMoneyPortfolio initSip;
  public Map<MyMoneyAsset, Double> initWeights = new HashMap<>();
  public Set<MyMoneyAsset> assetOrder = new LinkedHashSet<>();
}
