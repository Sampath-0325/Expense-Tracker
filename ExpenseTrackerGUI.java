import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.RowFilter;
import java.text.NumberFormat;
import java.util.Locale;

public class ExpenseTrackerGUI extends JFrame {

    private JTextField amountField;
    private JComboBox<String> categoryBox;
    private JTextField dateField;
    private JTextField descriptionField;
    private JLabel totalLabel;
    private DefaultTableModel tableModel;
    private JTable expenseTable;
    private ExpenseManager manager;
    private JComboBox<String> filterCategoryBox;
    private JTextField filterDateField;
    private JLabel categoryTotalLabel;

    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // Blue
    private static final Color SECONDARY_COLOR = new Color(39, 174, 96); // Green
    private static final Color DELETE_COLOR = new Color(231, 76, 60); // Red
    private static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color TABLE_HEADER_COLOR = new Color(52, 152, 219);
    private static final Color GRADIENT_START = new Color(230, 240, 255);
    private static final Color GRADIENT_END = new Color(255, 240, 230);
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public ExpenseTrackerGUI() {
        setTitle("Expense Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Custom panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                
                // Create gradient
                GradientPaint gp = new GradientPaint(0, 0, GRADIENT_START, w, h, GRADIENT_END);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
                
                // Add subtle pattern
                g2d.setColor(new Color(255, 255, 255, 20));
                for (int i = 0; i < w; i += 40) {
                    for (int j = 0; j < h; j += 40) {
                        g2d.fillOval(i, j, 4, 4);
                    }
                }
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        setContentPane(mainPanel);

        // ==== TOP PANEL ====
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Amount field with validation
        amountField = new JTextField(10);
        amountField.setBackground(Color.WHITE);
        amountField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { validateAmount(); }
            public void removeUpdate(DocumentEvent e) { validateAmount(); }
            public void insertUpdate(DocumentEvent e) { validateAmount(); }
            private void validateAmount() {
                try {
                    if (!amountField.getText().isEmpty()) {
                        Double.parseDouble(amountField.getText());
                        amountField.setBackground(Color.WHITE);
                    }
                } catch (NumberFormatException e) {
                    amountField.setBackground(new Color(255, 200, 200));
                }
            }
        });

        // Category dropdown
        String[] categories = {"Food", "Travel", "Bills", "Shopping", "Others"};
        categoryBox = new JComboBox<>(categories);
        categoryBox.setBackground(Color.WHITE);
        
        // Date field with today's date
        dateField = new JTextField(10);
        dateField.setBackground(Color.WHITE);
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        
        descriptionField = new JTextField(20);
        descriptionField.setBackground(Color.WHITE);

        // Styled buttons
        JButton addButton = createStyledButton("Add Expense", SECONDARY_COLOR);
        JButton deleteButton = createStyledButton("Delete Selected", DELETE_COLOR);
        JButton exportButton = createStyledButton("Export to CSV", PRIMARY_COLOR);
        JButton applyFilterButton = createStyledButton("Apply Filter", PRIMARY_COLOR);

        // Add components to input panel with styled labels
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(createStyledLabel("Amount:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(amountField, gbc);
        gbc.gridx = 2;
        inputPanel.add(createStyledLabel("Category:"), gbc);
        gbc.gridx = 3;
        inputPanel.add(categoryBox, gbc);
        gbc.gridx = 4;
        inputPanel.add(createStyledLabel("Date:"), gbc);
        gbc.gridx = 5;
        inputPanel.add(dateField, gbc);
        gbc.gridx = 6;
        inputPanel.add(createStyledLabel("Description:"), gbc);
        gbc.gridx = 7;
        inputPanel.add(descriptionField, gbc);
        gbc.gridx = 8;
        inputPanel.add(addButton, gbc);
        gbc.gridx = 9;
        inputPanel.add(deleteButton, gbc);
        gbc.gridx = 10;
        inputPanel.add(exportButton, gbc);

        // ==== FILTER PANEL ====
        JPanel filterPanel = new JPanel();
        filterPanel.setOpaque(false);
        filterPanel.add(createStyledLabel("Filter by Category:"));
        filterCategoryBox = new JComboBox<>(categories);
        filterCategoryBox.setBackground(Color.WHITE);
        filterCategoryBox.insertItemAt("All", 0);
        filterCategoryBox.setSelectedIndex(0);
        filterPanel.add(filterCategoryBox);
        
        filterPanel.add(createStyledLabel("Filter by Date (YYYY-MM):"));
        filterDateField = new JTextField(10);
        filterDateField.setBackground(Color.WHITE);
        filterDateField.setText(LocalDate.now().toString().substring(0, 7));
        filterPanel.add(filterDateField);
        
        filterPanel.add(applyFilterButton);

        // ==== CENTER TABLE ====
        String[] columns = {"Amount", "Category", "Date", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        expenseTable = new JTable(tableModel);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.setAutoCreateRowSorter(true);
        expenseTable.setBackground(new Color(255, 255, 255, 200));
        expenseTable.setOpaque(false);
        expenseTable.setGridColor(new Color(200, 200, 200, 100));
        expenseTable.setSelectionBackground(new Color(52, 152, 219, 100));
        expenseTable.setSelectionForeground(Color.BLACK);
        
        // Style table header
        JTableHeader header = expenseTable.getTableHeader();
        header.setBackground(new Color(52, 152, 219, 200));
        header.setForeground(Color.WHITE);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        
        JScrollPane tableScrollPane = new JScrollPane(expenseTable);
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setOpaque(false);

        // ==== BOTTOM PANEL ====
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        bottomPanel.setOpaque(false);
        totalLabel = createStyledLabel("Total: " + CURRENCY_FORMAT.format(0.0));
        categoryTotalLabel = createStyledLabel("Category Total: " + CURRENCY_FORMAT.format(0.0));
        bottomPanel.add(totalLabel);
        bottomPanel.add(categoryTotalLabel);

        // ==== MAIN CONTENT PANEL ====
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);
        mainContentPanel.add(filterPanel, BorderLayout.NORTH);
        mainContentPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Add all panels to frame
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(mainContentPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // ==== LOGIC ====
        manager = new ExpenseManager();
        manager.loadExpenses("expenses.csv");

        for (Expense e : manager.getAllExpenses()) {
            tableModel.addRow(new Object[] {
                e.getAmount(), e.getCategory(), e.getDate(), e.getDescription()
            });
        }

        updateTotalLabels();

        // ==== EVENT HANDLERS ====
        addButton.addActionListener(e -> {
            try {
                if (amountField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter an amount.");
                    return;
                }
                double amount = Double.parseDouble(amountField.getText());
                String category = (String) categoryBox.getSelectedItem();
                String date = dateField.getText();
                String description = descriptionField.getText();

                Expense expense = new Expense(category, amount, date, description);
                manager.addExpense(expense);
                manager.saveExpenses("expenses.csv");

                tableModel.addRow(new Object[]{amount, category, date, description});
                updateTotalLabels();

                amountField.setText("");
                dateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                descriptionField.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid input.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = expenseTable.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = expenseTable.convertRowIndexToModel(selectedRow);
                tableModel.removeRow(modelRow);
                manager.deleteExpense(modelRow);
                manager.saveExpenses("expenses.csv");
                updateTotalLabels();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an expense to delete.");
            }
        });

        applyFilterButton.addActionListener(e -> {
            String category = (String) filterCategoryBox.getSelectedItem();
            String date = filterDateField.getText();
            filterTable(category, date);
        });

        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Expenses");
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getPath() + ".csv");
                }
                manager.exportToCSV(file.getPath());
                JOptionPane.showMessageDialog(this, "Expenses exported successfully!");
            }
        });

        // Add listeners for real-time filtering
        filterCategoryBox.addActionListener(e -> applyFilter());
        filterDateField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
        });
    }

    private void updateTotalLabels() {
        String currentMonth = LocalDate.now().toString().substring(0, 7);
        double total = manager.getTotalForMonth(currentMonth);
        totalLabel.setText("Total: " + CURRENCY_FORMAT.format(total));

        String selectedCategory = (String) filterCategoryBox.getSelectedItem();
        if (!"All".equals(selectedCategory)) {
            double categoryTotal = manager.getTotalForCategory(selectedCategory);
            categoryTotalLabel.setText(String.format("Category Total (%s): %s", 
                selectedCategory, CURRENCY_FORMAT.format(categoryTotal)));
        } else {
            categoryTotalLabel.setText("");
        }
    }

    private void filterTable(String category, String date) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        expenseTable.setRowSorter(sorter);

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        if (!"All".equals(category)) {
            filters.add(RowFilter.regexFilter(category, 1)); // Category is in column 1
        }

        if (!date.isEmpty()) {
            filters.add(RowFilter.regexFilter(date, 2)); // Date is in column 2
        }

        if (!filters.isEmpty()) {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        } else {
            sorter.setRowFilter(null);
        }
    }

    private void applyFilter() {
        String category = (String) filterCategoryBox.getSelectedItem();
        String date = filterDateField.getText();
        filterTable(category, date);
        updateTotalLabels();
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }
}
