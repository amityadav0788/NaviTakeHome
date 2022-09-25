package main.java.com.mymoney.service;

import java.time.Month;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.DataFormatException;

import main.java.com.mymoney.datamodel.MyMoneyDao;
import main.java.com.mymoney.datamodel.MyMoneyPortfolio;
import main.java.com.mymoney.util.MyMoneyHelper;
import main.java.com.mymoney.entity.MyMoneyFundEntity;
import main.java.com.mymoney.enums.MyMoneyAsset;

public class MyMoneyFunctionImpl implements MyMoneyFunctionInterface {
    private static final String CANNOT_REBALANCE = "CANNOT_REBALANCE";
    private final MyMoneyDao myMoneyDao;
  
    public MyMoneyFunctionImpl(MyMoneyDao dao) {
      this.myMoneyDao = dao;
    }
  
    /**
     * This method initializes the allocation and creates portfolio with weights. Weights are stored on set
     * These weights are then used while rebalancing
     *
     * @param allocations
     * @throws DataFormatException
     */
    @Override
    public void allocate(List<Double> allocation) throws DataFormatException {
      if (Objects.nonNull(myMoneyDao.initAllocate)) {
        throw new IllegalStateException("The funds are already Allocated Once");
      }
      myMoneyDao.initAllocate = createMyMoneyFunds(allocation);
      myMoneyDao.initWeights = calculateDesiredWeight();
    }

    /**
     * This method starts monthly sip from February for all assets.
     *
     * @param sips
     * @throws DataFormatException
     */
    @Override
    public void sip(List<Double> sips) throws DataFormatException {
      if (Objects.nonNull(myMoneyDao.initSip)) {
        throw new IllegalStateException("The SIP is already registered once");
      }
      myMoneyDao.initSip = createMyMoneyFunds(sips);
    }
  
    /**
     * Allocates the ate for each month
     *
     * @param rates
     * @param month
     */
    @Override
    public void evaluateRates(List<Double> rates, Month month)
        throws IllegalStateException, DataFormatException {
      if (Objects.nonNull(myMoneyDao.monthChangeRate.getOrDefault(month, null))) {
        throw new IllegalStateException(
            "The rate of " + month.name() + " is already registered");
      }
      if (Objects.isNull(rates) || Objects.isNull(month)) {
        throw new InputMismatchException("Parameters are not correct");
      }
      if (rates.size() != myMoneyDao.assetOrder.size()) {
        throw new DataFormatException("The input is not formatted");
      }
      Map<MyMoneyAsset, Double> change =
          MyMoneyHelper.zip(myMoneyDao.assetOrder.stream(), rates.stream(), Map::entry)
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      myMoneyDao.monthChangeRate.put(month, change);
    }

    /**
     * This method calculates the balance upto the given month
     *
     * @param month
     * @return
     */
    @Override
    public String balance(Month month) {
      updateBalance();
      MyMoneyPortfolio fund =
          Optional.ofNullable(myMoneyDao.monthBalance.get(month)).orElseThrow(() -> new IllegalStateException("No balance data for   "+ month.name()));
      return fund.toString();
    }

    /**
     * This method calculate the balance after rebalance command
     *
     * @return balance
     */
    @Override
    public String doRebalance() {
      updateBalance();
      Month lastUpdatedMonth = myMoneyDao.monthBalance.lastKey();
      Month lastRebalancedMonth = getLastReBalancedMonth(lastUpdatedMonth);
      MyMoneyPortfolio balance = myMoneyDao.monthBalance.getOrDefault(lastRebalancedMonth, null);
      return Objects.nonNull(balance) ? balance.toString() : CANNOT_REBALANCE;
    }

    @Override
    public int getAsset() {
      return myMoneyDao.assetOrder.size();
    }

    private MyMoneyPortfolio createMyMoneyFunds(List<Double> allocation) throws DataFormatException {
      validateInputs(myMoneyDao.assetOrder, allocation);
      List<MyMoneyFundEntity> fundEntityList =
      MyMoneyHelper.zip(myMoneyDao.assetOrder.stream(), allocation.stream(), MyMoneyFundEntity::new)
              .collect(Collectors.toList());
      return new MyMoneyPortfolio(fundEntityList);
    }
    
    private Map<MyMoneyAsset, Double> calculateDesiredWeight() {
      if (Objects.isNull(myMoneyDao.initAllocate)) {
        throw new IllegalStateException("The funds are not yet Allocated");
      }
      return myMoneyDao.initAllocate.getFunds().stream()
          .collect(
              Collectors.toMap(
                  MyMoneyFundEntity::getAsset,
                  e -> e.getAmount() * 100 / myMoneyDao.initAllocate.getInvestment()));
    }
  
    private void validateInputs(Set<MyMoneyAsset> assetOrderForIO, List<Double> allocations)
        throws DataFormatException {
      if (Objects.isNull(allocations) || allocations.size() != assetOrderForIO.size()) {
        throw new DataFormatException("The input is not in the desired format");
      }
    }
  
    private void updateBalance() {
      Month lastCalculatedMonth = (!myMoneyDao.monthBalance.isEmpty()) ? myMoneyDao.monthBalance.lastKey() : null;
      Month lastChangeRateMonth = myMoneyDao.monthChangeRate.lastKey();
      Map<MyMoneyAsset, Double> lastKnownChange = myMoneyDao.monthChangeRate.get(lastChangeRateMonth);
      if (Objects.isNull(lastKnownChange)) {
        throw new IllegalStateException("Rate is not present");
      }
      if (lastCalculatedMonth == null) {
        MyMoneyPortfolio myMoneyFund =
            calculateBalance(
                myMoneyDao.initAllocate,
                null,
                myMoneyDao.monthChangeRate.get(Month.JANUARY));
        myMoneyDao.monthBalance.put(Month.JANUARY, myMoneyFund);
        lastCalculatedMonth = myMoneyDao.monthBalance.lastKey();
      }
      if (lastCalculatedMonth != lastChangeRateMonth) {
        Month startMonth = lastCalculatedMonth;
        Month endMonth = lastChangeRateMonth;
        for (int index = startMonth.getValue(); index < endMonth.getValue(); index++) {
          Month lastUpdatedMonth = Month.of(index);
          Month currentCalculationMonth = Month.of(index + 1);
          MyMoneyPortfolio carryOverBalance =
              myMoneyDao.monthBalance.get(lastUpdatedMonth).clone();
          Map<MyMoneyAsset, Double> changeRate =
              myMoneyDao.monthChangeRate.get(currentCalculationMonth);
              MyMoneyPortfolio availableBalance =
              calculateBalance(carryOverBalance, myMoneyDao.initSip, changeRate);
          if (shouldReBalance(currentCalculationMonth)) {
            availableBalance = doReBalance(availableBalance);
          }
          myMoneyDao.monthBalance.putIfAbsent(currentCalculationMonth, availableBalance);
        }
      }
    }
  
    private MyMoneyPortfolio calculateBalance(
        MyMoneyPortfolio carryOverBalance,
        MyMoneyPortfolio monthlySip,
        Map<MyMoneyAsset, Double> changeRate) {
      MyMoneyPortfolio balAfterSip = applySipInvestment(carryOverBalance, monthlySip);
      return applyMarketChange(balAfterSip, changeRate);
    }
  
    private MyMoneyPortfolio applyMarketChange(
        MyMoneyPortfolio carryOverBalance, Map<MyMoneyAsset, Double> changeRate) {
      List<MyMoneyFundEntity> funds = carryOverBalance.getFunds();
      funds.forEach(
          entity -> {
            double rate = changeRate.get(entity.getAsset());
            double updatedAmount = entity.getAmount() * (1 + rate / 100);
            entity.setAmount(Math.floor(updatedAmount));
          });
      return carryOverBalance;
    }
  
    private MyMoneyPortfolio applySipInvestment(
        MyMoneyPortfolio carryOverBalance, MyMoneyPortfolio initialSip) {
      List<MyMoneyFundEntity> funds = carryOverBalance.getFunds();
      if (Objects.nonNull(initialSip)) {
        IntStream.range(0, funds.size())
            .forEach(
                index -> {
                  MyMoneyFundEntity fundEntity = funds.get(index);
                  double sipAmount = initialSip.getFunds().get(index).getAmount();
                  fundEntity.setAmount(Math.floor(fundEntity.getAmount() + sipAmount));
                });
      }
      return carryOverBalance;
    }
  
    private Month getLastReBalancedMonth(Month month) {
      return month == Month.DECEMBER ? month : Month.JUNE;
    }
  
    private boolean shouldReBalance(Month month) {
      // Assumption#3: The re-balancing happens on 6 and 12 months.
      return month.equals(Month.JUNE) || month.equals(Month.DECEMBER);
    }
  
    private MyMoneyPortfolio doReBalance(MyMoneyPortfolio currentFunds) {
      List<MyMoneyFundEntity> funds = currentFunds.getFunds();
      double totalInvestment = currentFunds.getInvestment();
      funds.forEach(
          entity -> {
            double desiredWeight = myMoneyDao.initWeights.get(entity.getAsset());
            entity.setAmount(Math.floor(totalInvestment * desiredWeight / 100));
          });
      return currentFunds;
    }

}
  
