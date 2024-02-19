package com.example.calculatorpoc;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    TextView inputText, outputText;

    private String input, output, newOutput;
    private static final String PREF_NAME = "CalculationHistory";
    private static final String KEY_HISTORY = "history";

    private final List<String> calculationHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.InputText);
        outputText = findViewById(R.id.OutputText);

    }

    public void onButtonClicked(View view) {
        Button button = (Button) view;
        String data = button.getText().toString();
        switch (data) {
            case "C":
                clearInput();
                break;

            case "⌫":
                handleBackspace();
                break;

            case "=":
                handleEquals();
                break;

            case "H":
                showHistory();
                break;

            default:
                handleArithmeticOperator(data);
        }
        updateInputText();
    }

    private void clearInput() {
        input = null;
        output = null;
        newOutput = null;
        outputText.setText("");
        updateInputText();
    }

    private void handleBackspace() {
        if (input != null && input.length() > 0) {
            input = input.substring(0, input.length() - 1);
        }
    }

    private void handleEquals() {
        if (input != null && !input.isEmpty()) {
            try {
                arithemeticOperations();
                String historyStep = input + "=" + removeDecimal(output);
                calculationHistory.add(historyStep);
                outputText.setText(removeDecimal(output));
                input = output;
                inputText.setText("");

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error in calculation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }


    private void handleArithmeticOperator(String operator) {
        if (isOperator(operator)) {
            if (input == null) {
                input = "";
            }
            if (!input.isEmpty() && isOperator(String.valueOf(input.charAt(input.length() - 1)))) {
                input = input.substring(0, input.length() - 1) + operator;
            } else {
                input += operator;
            }
        } else {
            appendToInput(operator);
        }
    }


    private void updateCalculationHistory(List<String> history) {
        saveCalculationHistory(history);
    }

    public void showHistory() {
        try {
            List<String> calculationHistory = getCalculationHistory();

            if (calculationHistory != null && !calculationHistory.isEmpty()) {
                int historyLimit = 10; // Set the desired limit
                int startIndex = Math.max(0, calculationHistory.size() - historyLimit);

                List<String> displayedHistory = new ArrayList<>(calculationHistory.subList(startIndex, calculationHistory.size()));
                Collections.reverse(displayedHistory);

                StringBuilder historyText = new StringBuilder();
                for (String step : displayedHistory) {
                    if (step != null) {
                        historyText.append(step).append("\n");
                    }
                }
                outputText.setText(historyText.toString());
            } else {
                Toast.makeText(getApplicationContext(), "History is empty", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("GetCalcuHisto",e.toString());
            Toast.makeText(getApplicationContext(), "Error showing history", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveCalculationHistory(List<String> history) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String historyJson = gson.toJson(history);

        editor.putString(KEY_HISTORY, historyJson);
        editor.apply();
    }


    public List<String> getCalculationHistory() {
        List<String> historyList = new ArrayList<>();

        try {
            SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String jsonHistory = preferences.getString(KEY_HISTORY, "");
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            Gson gson = new Gson();
            historyList = gson.fromJson(jsonHistory, listType);

            if (historyList == null) {
                return new ArrayList<>();
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return historyList;
    }

    private void appendToInput(String data) {
        if (input == null) {
            input = "";
        }
        if (input.isEmpty() && isOperator(data)) {
            return;
        }
        if (!input.isEmpty() && isOperator(String.valueOf(input.charAt(input.length() - 1))) && isOperator(data)) {
            Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isOperator(data)) {
            int lastOperatorIndex = findLastOperatorIndex(input);
            if (lastOperatorIndex != -1 && lastOperatorIndex < input.length() - 1) {
                arithemeticOperations();
            }
        }
        input += data;
        updateInputText();
    }



    private boolean isOperator(String data) {
        return data.equals("+") || data.equals("/") || data.equals("-") || data.equals("*") || data.equals(("%"));
    }


    private void updateInputText() {
        if (input != null) {
            String cleanedInput = removeDecimal(input);
            inputText.setText(cleanedInput);
        } else {
            inputText.setText("");
        }
    }

    private void arithemeticOperations() {
        if (input != null && !input.isEmpty()) {
            try {
                // Use regular expression to split the input based on operators and keep them
                String[] operators = input.split("\\d+");
                String[] numbers = input.split("[+\\-*/%]+");
                Log.d("Operator",operators[1]);
                Log.d("Numbers",numbers[0]);

                double result = Double.parseDouble(numbers[0]);

                // Perform the operations based on the order of operations
                for (int i = 1; i < numbers.length; i++) {
                    switch (operators[i]) {
                        case "+":
                            result += Double.parseDouble(numbers[i]);
                            break;
                        case "-":
                            result -= Double.parseDouble(numbers[i]);
                            break;
                        case "*":
                            result *= Double.parseDouble(numbers[i]);
                            break;
                        case "/":
                            result /= Double.parseDouble(numbers[i]);
                            break;
                        case "%":
                            result %= Double.parseDouble(numbers[i]);
                            break;
                    }
                }
                 String firstinput=input;
                input = removeDecimal(String.valueOf(result));
                output = removeDecimal(String.valueOf(result));
                outputText.setText(output);
                String fullExpression = firstinput + " = " + output;
                List<String> currentHistory = getCalculationHistory();
                currentHistory.add(fullExpression);
                updateCalculationHistory(currentHistory);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error in calculation", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private int findLastOperatorIndex(String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            char ch = str.charAt(i);
            if (isOperator(String.valueOf(ch))) {
                return i;
            }
        }
        return -1;
    }


    private String removeDecimal(String number) {
        if (number.endsWith(".0")) {
            number = number.substring(0, number.length() - 2);
        }
        return number;
    }
}


