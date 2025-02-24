package org.warm4ik.lab;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SucroseViscosityCalculator extends JFrame {
    private static final String VERSION = "1.0";
    private static final String LAST_MODIFIED = "2025-26-02";

    private static final double MIN_TEMPERATURE = 10.0;
    private static final double MAX_TEMPERATURE = 50.0;
    private static final double TEMPERATURE_STEP = 10.0;

    private static final double MIN_CONCENTRATION = 0.0;
    private static final double MAX_CONCENTRATION = 50.0;
    private static final double CONCENTRATION_STEP = 2.5;

    private static final List<ExperimentalData> EXPERIMENTAL_DATA = List.of(
            new ExperimentalData(40.0, 0.0, 0.65),
            new ExperimentalData(50.0, 50.0, 4.94),
            new ExperimentalData(30.0, 30.0, 2.50)
    );

    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JTextArea experimentalDataTextArea;


    private static final List<Double> TEMPERATURES = generateValues(MIN_TEMPERATURE, MAX_TEMPERATURE, TEMPERATURE_STEP);
    private static final List<Double> CONCENTRATIONS = generateValues(MIN_CONCENTRATION, MAX_CONCENTRATION, CONCENTRATION_STEP);

    public SucroseViscosityCalculator() {
        initUI();
        calculateAndDisplay();
    }

    private void initUI() {
        setTitle("Sucrose Viscosity Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1980, 1200);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(tableModel);
        experimentalDataTextArea = new JTextArea();
        experimentalDataTextArea.setEditable(false);

        JLabel versionLabel = new JLabel("Version: " + VERSION + ", Last Modified: " + LAST_MODIFIED);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createExperimentalDataPanel(), BorderLayout.SOUTH);
        mainPanel.add(versionLabel, BorderLayout.NORTH);

        add(mainPanel);
        setVisible(true);
    }

    private static List<Double> generateValues(double min, double max, double step) {
        List<Double> values = new ArrayList<>();
        BigDecimal current = BigDecimal.valueOf(min);
        BigDecimal end = BigDecimal.valueOf(max);
        BigDecimal stepBD = BigDecimal.valueOf(step);
        int scale = 10;

        while (current.compareTo(end) <= 0) {
            values.add(current.doubleValue());
            current = current.add(stepBD).setScale(scale, RoundingMode.HALF_UP);
        }
        return values;
    }

    private double calculateViscosity(double cb, double t) {
        double x = cb / (1900 - 18 * cb);
        double lgEta = 22.46 * x - 0.114 + ((30 - t) / (91 + t)) * (1.1 + 43.1 * Math.pow(x, 1.25));
        return Math.pow(10, lgEta);
    }

    private double calculatePercentageError(double experimental, double calculated) {
        return Math.abs((experimental - calculated) / experimental) * 100;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Calculated Viscosities in 'cP'"));
        panel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createExperimentalDataPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Experimental Data and Error"));
        panel.add(new JScrollPane(experimentalDataTextArea), BorderLayout.CENTER);
        return panel;
    }

    private void calculateAndDisplay() {
        tableModel.addColumn("↓T(°C)\\Conc(%)➡");
        CONCENTRATIONS.forEach(c -> tableModel.addColumn(String.format("%.1f%%", c)));

        DecimalFormat df = new DecimalFormat("#.##");
        TEMPERATURES.forEach(t -> {
            Object[] row = new Object[CONCENTRATIONS.size() + 1];
            row[0] = String.valueOf(t.intValue());

            for (int i = 0; i < CONCENTRATIONS.size(); i++) {
                double viscosity = calculateViscosity(CONCENTRATIONS.get(i), t);
                row[i + 1] = df.format(viscosity);
            }
            tableModel.addRow(row);
        });

        StringBuilder sb = new StringBuilder("Experimental Data:\n");
        EXPERIMENTAL_DATA.forEach(data -> {
            double calc = calculateViscosity(data.concentration, data.temperature);
            double error = calculatePercentageError(data.viscosity, calc);
            sb.append(String.format(
                    "t=%.0f°C, CB=%.1f%%, η_exp = %.2f cP, η_calc = %.2f cP, Error = %.2f%%\n",
                    data.temperature, data.concentration, data.viscosity, calc, error
            ));
        });
        experimentalDataTextArea.setText(sb.toString());
    }

    private record ExperimentalData(double temperature, double concentration, double viscosity) {
    }
}