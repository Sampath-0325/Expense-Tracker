import java.util.*;
import java.io.*;

public class ExpenseManager {
    private List<Expense> expenses;

    public ExpenseManager() {
        expenses = new ArrayList<>();
    }

    public void loadExpenses(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                // If the file doesn't exist, don't throw an error — just return.
                return;
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                Expense e = Expense.fromCSV(line);
                expenses.add(e);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveExpenses(String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (Expense e : expenses) {
                bw.write(e.getCategory() + "," + e.getAmount() + "," + e.getDate() + "," + e.getDescription());
                bw.newLine();  // THIS IS CRUCIAL
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportToCSV(String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            // Write header
            bw.write("Category,Amount,Date,Description");
            bw.newLine();
            
            // Write data
            for (Expense e : expenses) {
                bw.write(e.getCategory() + "," + e.getAmount() + "," + e.getDate() + "," + e.getDescription());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    // Optional: useful helper method
    public void addExpenseAndSave(Expense expense, String fileName) {
        addExpense(expense);
        saveExpenses(fileName);
    }

    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
        }
    }

    public List<Expense> getAllExpenses() {
        return expenses;
    }

    public double getTotalForMonth(String month) {
        double total = 0;
        for (Expense e : expenses) {
            if (e.getDate().startsWith(month)) { // e.g., "2025-04"
                total += e.getAmount();
            }
        }
        return total;
    }

    public double getTotalForCategory(String category) {
        double total = 0;
        for (Expense e : expenses) {
            if (e.getCategory().equals(category)) {
                total += e.getAmount();
            }
        }
        return total;
    }

    public List<Expense> getExpensesByCategory(String category) {
        List<Expense> filtered = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getCategory().equals(category)) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    public List<Expense> getExpensesByDate(String date) {
        List<Expense> filtered = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.getDate().startsWith(date)) {
                filtered.add(e);
            }
        }
        return filtered;
    }
}
