package org.warm4ik.lab;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CalculatorFrame extends JFrame {

    private JTextField minTemperatureField;
    private JTextField maxTemperatureField;
    private JTextField temperatureStepField;

    private JTextField minConcentrationField;
    private JTextField maxConcentrationField;
    private JTextField concentrationStepField;


    /** таблица для отображения рассчитанных значений вязкости */
    private DefaultTableModel resultTableModel;
    private JTable resultTable;


    /** таблица для ввода экспериментальных данных */
    private DefaultTableModel expDataTableModel;
    private JTable expDataTable;

    /** текстовое поле для отображения сравнения экспериментальных и рассчитанных данных */
    private JTextArea experimentalComparisonTextArea;


    private final ViscosityCalculator calculator = new ViscosityCalculator();

    public CalculatorFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Sucrose Viscosity Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JPanel temperaturePanel = new JPanel(new GridLayout(2, 3, 5, 5));
        temperaturePanel.setBorder(BorderFactory.createTitledBorder("Параметры температуры (°C)"));
        temperaturePanel.add(new JLabel("Мин. температура:"));
        temperaturePanel.add(new JLabel("Макс. температура:"));
        temperaturePanel.add(new JLabel("Шаг температуры:"));
        minTemperatureField = new JTextField("10.0");
        maxTemperatureField = new JTextField("50.0");
        temperatureStepField = new JTextField("10.0");
        temperaturePanel.add(minTemperatureField);
        temperaturePanel.add(maxTemperatureField);
        temperaturePanel.add(temperatureStepField);

        JPanel concentrationPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        concentrationPanel.setBorder(BorderFactory.createTitledBorder("Параметры концентрации (%)"));
        concentrationPanel.add(new JLabel("Мин. концентрация:"));
        concentrationPanel.add(new JLabel("Макс. концентрация:"));
        concentrationPanel.add(new JLabel("Шаг концентрации:"));
        minConcentrationField = new JTextField("0.0");
        maxConcentrationField = new JTextField("50.0");
        concentrationStepField = new JTextField("2.5");
        concentrationPanel.add(minConcentrationField);
        concentrationPanel.add(maxConcentrationField);
        concentrationPanel.add(concentrationStepField);

        JButton calculateButton = new JButton("Рассчитать");
        calculateButton.addActionListener(e -> calculateAndDisplay());

        JButton addExpDataButton = new JButton("Добавить экспериментальные данные");
        addExpDataButton.addActionListener(e -> {
            expDataTableModel.addRow(new Object[]{"", "", ""});
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addExpDataButton);
        buttonPanel.add(calculateButton);

        /** объединение панели параметров */
        JPanel parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(temperaturePanel, BorderLayout.NORTH);
        parametersPanel.add(concentrationPanel, BorderLayout.CENTER);
        parametersPanel.add(buttonPanel, BorderLayout.SOUTH);

        resultTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(resultTableModel);
        resultTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane resultTableScrollPane = new JScrollPane(resultTable);
        resultTableScrollPane.setBorder(BorderFactory.createTitledBorder("Рассчитанные вязкости (cP)"));

        expDataTableModel = new DefaultTableModel(new Object[]{"Температура(°C)", "Концентрация(%)", "Вязкость(cP)"}, 0);
        expDataTable = new JTable(expDataTableModel);
        expDataTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane expDataScrollPane = new JScrollPane(expDataTable);
        expDataScrollPane.setBorder(BorderFactory.createTitledBorder("Экспериментальные данные"));

        experimentalComparisonTextArea = new JTextArea();
        experimentalComparisonTextArea.setEditable(false);
        JScrollPane expComparisonScrollPane = new JScrollPane(experimentalComparisonTextArea);
        expComparisonScrollPane.setBorder(BorderFactory.createTitledBorder("Сравнение с экспериментальными данными"));

        JPanel lowerPanel = new JPanel(new GridLayout(1, 2));
        lowerPanel.add(expDataScrollPane);
        lowerPanel.add(expComparisonScrollPane);

        /** компановка основных панелей */
        setLayout(new BorderLayout());
        add(parametersPanel, BorderLayout.NORTH);
        add(resultTableScrollPane, BorderLayout.CENTER);
        add(lowerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    /** метод, который считывает введённые параметры, проводит расчёты и отображает результаты. */
    private void calculateAndDisplay() {
        try {
            double minTemp = Double.parseDouble(minTemperatureField.getText());
            double maxTemp = Double.parseDouble(maxTemperatureField.getText());
            double tempStep = Double.parseDouble(temperatureStepField.getText());
            double minConc = Double.parseDouble(minConcentrationField.getText());
            double maxConc = Double.parseDouble(maxConcentrationField.getText());
            double concStep = Double.parseDouble(concentrationStepField.getText());

            List<Double> temperatures = calculator.generateValues(minTemp, maxTemp, tempStep);
            List<Double> concentrations = calculator.generateValues(minConc, maxConc, concStep);

            resultTableModel.setRowCount(0);
            resultTableModel.setColumnCount(0);
            resultTableModel.addColumn("T(°C)\\C(%)");
            for (Double c : concentrations) {
                resultTableModel.addColumn(String.format("%.1f%%", c));
            }

            for (Double t : temperatures) {
                Object[] row = new Object[concentrations.size() + 1];
                row[0] = String.format("%.1f", t);
                for (int i = 0; i < concentrations.size(); i++) {
                    double viscosity = calculator.calculateViscosity(concentrations.get(i), t);
                    row[i + 1] = calculator.formatValue(viscosity);
                }
                resultTableModel.addRow(row);
            }

            StringBuilder comparison = new StringBuilder("Сравнение экспериментальных данных:\n");
            int rowCount = expDataTableModel.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                String tempStr = expDataTableModel.getValueAt(i, 0).toString().trim();
                String concStr = expDataTableModel.getValueAt(i, 1).toString().trim();
                String viscStr = expDataTableModel.getValueAt(i, 2).toString().trim();
                if (tempStr.isEmpty() || concStr.isEmpty() || viscStr.isEmpty()) {
                    continue;
                }
                double expTemp = Double.parseDouble(tempStr);
                double expConc = Double.parseDouble(concStr);
                double expVisc = Double.parseDouble(viscStr);
                double calcVisc = calculator.calculateViscosity(expConc, expTemp);
                double error = calculator.calculatePercentageError(expVisc, calcVisc);
                comparison.append(String.format(
                        "t=%.1f°C, C=%.1f%%, η_exp=%.2f cP, η_calc=%.2f cP, Ошибка=%.2f%%\n",
                        expTemp, expConc, expVisc, calcVisc, error
                ));
            }
            experimentalComparisonTextArea.setText(comparison.toString());

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите корректные " +
                    "числовые значения во всех полях ввода!", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
        }
    }
}
