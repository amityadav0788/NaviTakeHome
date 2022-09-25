package main.java.com.mymoney.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Month;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;

import main.java.com.mymoney.enums.MyMoneyCommands;

import java.lang.String;

public class MyMoneyHandler {
  private final MyMoneyFunctionInterface myMoneyFunction;

  public MyMoneyHandler(MyMoneyFunctionInterface myMoneyFunction) {
    this.myMoneyFunction = myMoneyFunction;
  }
    
    /** This method reads the input file and calls process commands
     * @param line
     * @return
     */
    public List<String> readFile(String filename) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
          List<String> outputs = lines.filter(l -> !l.isEmpty()).map(this::fireCommands).collect(Collectors.toList());
          // print the output
          outputs.stream().filter(Objects::nonNull).forEach(System.out::println);
          return outputs;
        } catch (IOException e) {
          throw new IOException("Not a calid file, maybe file is misplaced.");
        }
      }

    /** This method handles each line of input and fires commands
     * @param line
     * @return
     */
    public String fireCommands(String line) {
        String output = null;
        int supportedAsset = myMoneyFunction.getAsset();
        String[] commandAndInputs = line.split(" ");
        try {
          MyMoneyCommands inputCommand = MyMoneyCommands.valueOf(commandAndInputs[0]);
          switch (inputCommand) {
            case ALLOCATE:
              validateInput(commandAndInputs, supportedAsset);
              List<Double> allocations = getDoubles(1, supportedAsset, commandAndInputs);
              myMoneyFunction.allocate(allocations);
              break;
            case SIP:
              validateInput(commandAndInputs, supportedAsset);
              List<Double> sips = getDoubles(1, supportedAsset, commandAndInputs);
              myMoneyFunction.sip(sips);
              break;
            case CHANGE:
              validateInput(commandAndInputs, supportedAsset + 1);
              List<Double> rates =
                  Arrays.stream(commandAndInputs).skip(1).limit(supportedAsset).map(str -> Double.parseDouble(str.replace("%", "")))
                      .collect(Collectors.toList());
              Month month = Month.valueOf(commandAndInputs[supportedAsset + 1]);
              myMoneyFunction.evaluateRates(rates, month);
              break;
            case BALANCE:
              validateInput(commandAndInputs, 1);
              month = Month.valueOf(commandAndInputs[1]);
              output = myMoneyFunction.balance(month);
              break;
            case REBALANCE:
              output = myMoneyFunction.doRebalance();
              break;
            default:
              throw new DataFormatException("Invalid Command " + inputCommand + " supplied");
          }
        } catch (Exception e) {
          System.out.println(
              "Error Occurred while processing " + String.join(" ", commandAndInputs) + e.getMessage());
        }
        return output;
      }

      private List<Double> getDoubles(int skip, int limit, String[] commandAndInputs) {
        return Arrays.stream(commandAndInputs).skip(skip).limit(limit).map(Double::parseDouble).collect(Collectors.toList());
      }

      private void validateInput(String[] commandAndInputs, int size) {
        if (commandAndInputs.length != size + 1) {
          throw new InputMismatchException(
              "Please check the command " + String.join(" ", commandAndInputs));
        }
      }
}
