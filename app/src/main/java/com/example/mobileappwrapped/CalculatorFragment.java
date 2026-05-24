package com.example.mobileappwrapped;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobileappwrapped.databinding.FragmentCalculatorBinding;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class CalculatorFragment extends Fragment implements View.OnClickListener {

    private FragmentCalculatorBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCalculatorBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupNavigation(root);

        assignId(binding.buttonC);
        assignId(binding.buttonOpenBracket);
        assignId(binding.buttonCloseBracket);
        assignId(binding.buttonDivide);
        assignId(binding.buttonMultiply);
        assignId(binding.buttonPlus);
        assignId(binding.buttonMinus);
        assignId(binding.buttonEquals);
        assignId(binding.button0);
        assignId(binding.button1);
        assignId(binding.button2);
        assignId(binding.button3);
        assignId(binding.button4);
        assignId(binding.button5);
        assignId(binding.button6);
        assignId(binding.button7);
        assignId(binding.button8);
        assignId(binding.button9);
        assignId(binding.buttonAc);
        assignId(binding.buttonDot);

        return root;
    }

    private void assignId(Button btn) {
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();
        String dataToCalculate = binding.solutionTv.getText().toString();

        switch (buttonText) {
            case "AC":
                binding.solutionTv.setText("");
                binding.resultTv.setText("0");
                return;
            case "=":
                binding.solutionTv.setText(binding.resultTv.getText());
                return;
            case "C":
                if (!dataToCalculate.isEmpty()) {
                    dataToCalculate = dataToCalculate.substring(0, dataToCalculate.length() - 1);
                }
                break;
            default:
                dataToCalculate = dataToCalculate + buttonText;
                break;
        }
        binding.solutionTv.setText(dataToCalculate);

        String finalResult = getResult(dataToCalculate);

        if (!finalResult.equals("Err")) {
            binding.resultTv.setText(finalResult);
        }
    }

    private String getResult(String data) {
        Context context = null;
        try {
            context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scope = context.initStandardObjects();
            Object result = context.evaluateString(scope, data, "JavaScript", 1, null);

            String finalResult = result.toString();
            if (finalResult.endsWith(".0")) {
                finalResult = finalResult.substring(0, finalResult.length() - 2);
            }
            return finalResult;

        } catch (Exception e) {
            return "Err";
        } finally {
            if (context != null) {
                Context.exit();
            }
        }
    }
    private void setupNavigation(View root) {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(root).navigate(R.id.nav_home);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}