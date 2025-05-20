public class Expense {
    private String category;
    private double amount;
    private String date;
    private String description;

    public Expense(String category, double amount, String date, String description){
        this.category=category;
        this.amount=amount;
        this.date=date;
        this.description=description;
    }

    public String getCategory(){
        return category;
    }
    public void setCategory(String category){
        this.category=category;
    }
    public double getAmount(){
        return amount;
    }
    public void setAmount(double amount){
        this.amount=amount;
    }
    public String getDate(){
        return date;
    }
    public void setDate(String date){
        this.date=date;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description=description;
    }

    public String toCSV(){
        return category+","+amount+","+date+","+description;
    }

    public static Expense fromCSV(String line){
        String[] parts=line.split(",");
        String category=parts[0];
        double amount=Double.parseDouble(parts[1]);
        String date=parts[2];
        String description=parts[3];
        return new Expense(category, amount, date, description);
    }

    
}