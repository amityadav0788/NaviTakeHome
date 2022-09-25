package main.java.com.mymoney.service;

import java.time.Month;
import java.util.List;
import java.util.zip.DataFormatException;

public interface MyMoneyFunctionInterface {
  int getAsset();

  void allocate(List<Double> allocations) throws DataFormatException;
  
  void sip(List<Double> sip) throws DataFormatException;
  
  void evaluateRates(List<Double> rate, Month month) throws IllegalStateException, DataFormatException;
  
  String balance(Month month);
  
  String doRebalance();
}
