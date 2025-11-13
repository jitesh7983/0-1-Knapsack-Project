import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Comparator;

public class EnhancedKnapsackGUI extends JFrame {
    private JTable inputTable, dpTable;
    private JTextField capacityField;
    private JTextArea outputArea;

    public EnhancedKnapsackGUI() {
        // Frame setup
        setTitle("0/1 Knapsack Solver");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Panel
        JLabel header = new JLabel("0/1 Knapsack Problem Solver", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(0, 102, 153)); // Dark blue
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(1000, 50));
        add(header, BorderLayout.NORTH);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputTable = createInputTable();
        inputTable.setBackground(new Color(170, 225, 180)); // Mint green background
        inputTable.setForeground(Color.BLACK);
        inputTable.setFont(new Font("Arial", Font.PLAIN, 16));
        inputTable.setRowHeight(174);
        customizeTableHeader(inputTable);

        JScrollPane tableScroll = new JScrollPane(inputTable);
        inputPanel.add(tableScroll, BorderLayout.CENTER);

        JPanel capacityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel capacityLabel = new JLabel("Knapsack Capacity (kg):");
        capacityLabel.setFont(new Font("Arial", Font.BOLD, 16));
        capacityField = new JTextField("8", 5); // Default capacity
        capacityField.setFont(new Font("Arial", Font.PLAIN, 16));
        capacityPanel.add(capacityLabel);
        capacityPanel.add(capacityField);
        inputPanel.add(capacityPanel, BorderLayout.SOUTH);

        add(inputPanel, BorderLayout.WEST);

        // Output Panel
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(240, 255, 243)); // Light blue background
        outputArea.setForeground(Color.DARK_GRAY);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 16));

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Results"));
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        // DP Solution Matrix Table
        JPanel dpPanel = new JPanel(new BorderLayout());
        dpPanel.setBorder(BorderFactory.createTitledBorder("DP Solution Matrix"));
        dpTable = new JTable(new DefaultTableModel());
        dpTable.setBackground(new Color(81, 111, 241)); // Light beige
        dpTable.setForeground(Color.WHITE);
        dpTable.setFont(new Font("Arial", Font.PLAIN, 16));
        dpTable.setRowHeight(60);
        JScrollPane dpScroll = new JScrollPane(dpTable);
        dpPanel.add(dpScroll, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputPanel, dpPanel);
        splitPane.setDividerLocation(360);
        add(splitPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton greedyButton = new JButton("Greedy Approach");
        JButton dpButton = new JButton("DP Approach");
        JButton resetButton = new JButton("Reset");

        greedyButton.setFont(new Font("Arial", Font.BOLD, 14));
        dpButton.setFont(new Font("Arial", Font.BOLD, 14));
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));

        greedyButton.setBackground(new Color(135, 206, 250)); // Light sky blue
        dpButton.setBackground(new Color(144, 238, 144)); // Light green
        resetButton.setBackground(new Color(255, 192, 201)); // Light pink

        buttonPanel.add(greedyButton);
        buttonPanel.add(dpButton);
        buttonPanel.add(resetButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        greedyButton.addActionListener(e -> calculateGreedy());
        dpButton.addActionListener(e -> calculateDP());
        resetButton.addActionListener(e -> resetAll());
    }

    // Create input table with editable fields
    private JTable createInputTable() {
        String[] columns = {"Item", "Value", "Weight"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Prevent editing the "Item" column
            }
        };

        for (int i = 0; i < 4; i++) {
            model.addRow(new Object[]{i + 1, 0, 0}); // Default rows
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        return table;
    }

    // Customize table header
    private void customizeTableHeader(JTable table) {
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(70, 130, 180)); // Steel blue
        table.getTableHeader().setForeground(Color.WHITE);
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
    }

    // Reset all fields and outputs
    private void resetAll() {
        outputArea.setText("");
        capacityField.setText("8");
        dpTable.setModel(new DefaultTableModel()); // Clear DP table
    }

    // Greedy Approach Calculation
    private void calculateGreedy() {
        int n = inputTable.getRowCount();
        int capacity;

        try {
            capacity = Integer.parseInt(capacityField.getText());
            if (capacity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive integer for capacity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Item[] items = new Item[n];
        try {
            for (int i = 0; i < n; i++) {
                int profit = Integer.parseInt(inputTable.getValueAt(i, 1).toString());
                int weight = Integer.parseInt(inputTable.getValueAt(i, 2).toString());
                if (profit <= 0 || weight <= 0) throw new NumberFormatException();
                items[i] = new Item(profit, weight, profit / (double) weight);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ensure all profit and weight values are positive integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Arrays.sort(items, Comparator.comparingDouble(Item::getRatio).reversed());

        int totalProfit = 0, totalWeight = 0;
        StringBuilder result = new StringBuilder("Greedy Approach Selected Items:\n");

        for (Item item : items) {
            if (totalWeight + item.weight <= capacity) {
                totalProfit += item.profit;
                totalWeight += item.weight;
                result.append("Item (Profit: ").append(item.profit).append(", Weight: ").append(item.weight).append(")\n");
            }
        }

        result.append("Total Profit: ").append(totalProfit).append("\n");
        result.append("Total Weight: ").append(totalWeight).append("\n");
        outputArea.append("Note: This may not be the optimal solution.\n\n");
        outputArea.append(result.toString());
    }

    // Dynamic Programming Approach Calculation
    private void calculateDP() {
        int n = inputTable.getRowCount();
        int capacity;

        try {
            capacity = Integer.parseInt(capacityField.getText());
            if (capacity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive integer for capacity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int[] profit = new int[n];
        int[] weight = new int[n];

        try {
            for (int i = 0; i < n; i++) {
                profit[i] = Integer.parseInt(inputTable.getValueAt(i, 1).toString());
                weight[i] = Integer.parseInt(inputTable.getValueAt(i, 2).toString());
                if (profit[i] <= 0 || weight[i] <= 0) throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ensure all profit and weight values are positive integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int[][] dp = new int[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            for (int w = 1; w <= capacity; w++) {
                if (weight[i - 1] <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], profit[i - 1] + dp[i - 1][w - weight[i - 1]]);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        String[] columnNames = new String[capacity + 4];
        columnNames[0] = "Profit";
        columnNames[1] = "Weight";
        columnNames[2] = "Item";
        for (int i = 0; i <= capacity; i++) {
            columnNames[i + 3] = String.valueOf(i);
        }

        DefaultTableModel dpModel = new DefaultTableModel(columnNames, n + 1);
        for (int i = 0; i <= n; i++) {
            dpModel.setValueAt(i == 0 ? 0 : profit[i - 1], i, 0);
            dpModel.setValueAt(i == 0 ? 0 : weight[i - 1], i, 1);
            dpModel.setValueAt(i == 0 ? "0" : String.valueOf(i), i, 2);

            for (int j = 0; j <= capacity; j++) {
                dpModel.setValueAt(dp[i][j], i, j + 3);
            }
        }

        dpTable.setModel(dpModel);
        customizeTableHeader(dpTable);

        int totalProfit = dp[n][capacity];
        int w = capacity;
        StringBuilder selectedItems = new StringBuilder("Selected Items:\n");

        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                selectedItems.append("Item ").append(i)
                        .append(" (Profit: ").append(profit[i - 1])
                        .append(", Weight: ").append(weight[i - 1]).append(")\n");
                w -= weight[i - 1];
            }
        }

        outputArea.append("Dynamic Programming Approach completed.\n");
        outputArea.append("Maximum Profit: " + totalProfit + "\n");
        outputArea.append(selectedItems.toString());
    }

    // Item class for Greedy approach
    static class Item {
        int profit, weight;
        double ratio;

        Item(int profit, int weight, double ratio) {
            this.profit = profit;
            this.weight = weight;
            this.ratio = ratio;
        }

        double getRatio() {
            return ratio;
        }
    }

    // Main method
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EnhancedKnapsackGUI().setVisible(true));
    }
}
