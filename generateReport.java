import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
public class generateReport {
    public static class DatabaseConnection {
        public static Connection getConnection() throws SQLException {
            String url = "jdbc:mysql://localhost:3306/InventoryDB";
            String user = "root";
            String password = "";
            return DriverManager.getConnection(url, user, password);
        }
    }
        public static void generateReports() {
        try {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("=+=+= Generate Reports =+=+=");
                System.out.println("1. Inventory Report");
                System.out.println("2. Sales Report");
                System.out.println("3. Purchase Report");
                System.out.println("4. Alerts Report");
                System.out.println("5. Return to Main Menu");
                System.out.print("Select a report type: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        generateInventoryReport();
                        break;
                    case 2:
                        generateSalesReport();
                        break;
                    case 3:
                        generatePurchaseReport();
                        break;
                    case 4:
                        generateAlertsReport();
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred in generating reports: " + e.getMessage());
        }
    }

    private static void generateInventoryReport() {
        String sql = "SELECT item_id, name, quantity, price FROM Items";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String timestamp = LocalDateTime.now().format(formatter);
            String fileName = timestamp + ".txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {

                writer.write("ItemID   | Item Name  | Qty   | Price");
                writer.newLine();

                while (rs.next()) {
                    String itemID = rs.getString("item_id");
                    String itemName = rs.getString("name");
                    int quantity = rs.getInt("quantity");
                    double price = rs.getDouble("price");

                    String line = String.format("%-8s | %-10s | %-5d | %.2f", itemID, itemName, quantity, price);
                    writer.write(line);
                    writer.newLine();
                }

                System.out.println("Inventory report generated and saved as " + fileName);
            } catch (IOException e) {
                System.out.println("Error writing to file: " + e.getMessage());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void generateSalesReport() {
        LocalDate startDate = validateDateInput("Enter the start date (yyyy-MM-dd): ");
        LocalDate endDate = validateDateInput("Enter the end date (yyyy-MM-dd): ");

        while (endDate.isBefore(startDate)) {
            System.out.println("End date cannot be before start date. Please enter a valid end date.");
            endDate = validateDateInput("Enter the end date (yyyy-MM-dd): ");
        }

        String sql = "SELECT * FROM Transactions WHERE transaction_type = 'Sale' AND transaction_date BETWEEN ? AND ?";
        double totalSales = 0.0;
        StringBuilder reportContent = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String itemId = rs.getString("item_id");
                    int quantity = rs.getInt("quantity");
                    double totalAmount = rs.getDouble("total_amount");
                    totalSales += totalAmount;
                    reportContent.append("ItemID: ").append(itemId)
                            .append(", Quantity: ").append(quantity)
                            .append(", Total Amount: $").append(totalAmount).append("\n");
                }
            }

            if (totalSales > 0) {
                reportContent.append("\nTotal Sales: $").append(totalSales);
                String fileName = String.format("Sales_%s_to_%s.txt", startDate, endDate);
                saveReportToFile(fileName, reportContent.toString());
                System.out.println("Sales report generated and saved as " + fileName);
            } else {
                System.out.println("No sales record found for the given period.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void generatePurchaseReport() {
        LocalDate startDate = validateDateInput("Enter the start date (yyyy-MM-dd): ");
        LocalDate endDate = validateDateInput("Enter the end date (yyyy-MM-dd): ");

        while (endDate.isBefore(startDate)) {
            System.out.println("End date cannot be before start date. Please enter a valid end date.");
            endDate = validateDateInput("Enter the end date (yyyy-MM-dd): ");
        }

        String sql = "SELECT * FROM Transactions WHERE transaction_type = 'Purchase' AND transaction_date BETWEEN ? AND ?";
        double totalPurchases = 0.0;
        StringBuilder reportContent = new StringBuilder();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(startDate));
            pstmt.setDate(2, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String itemId = rs.getString("item_id");
                    int quantity = rs.getInt("quantity");
                    double totalAmount = rs.getDouble("total_amount");
                    totalPurchases += totalAmount;
                    reportContent.append("ItemID: ").append(itemId)
                            .append(", Quantity: ").append(quantity)
                            .append(", Total Amount: $").append(totalAmount).append("\n");
                }
            }

            if (totalPurchases > 0) {
                reportContent.append("\nTotal Purchases: $").append(totalPurchases);
                String fileName = String.format("Purchase_%s_to_%s.txt", startDate, endDate);
                saveReportToFile(fileName, reportContent.toString());
                System.out.println("Purchase report generated and saved as " + fileName);
            } else {
                System.out.println("No purchase record found for the given period.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void generateAlertsReport() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT a.alert_id, a.item_id, a.alert_threshold, a.alert_date, i.quantity, i.name " +
                    "FROM inventoryalert a " +
                    "JOIN Items i ON a.item_id = i.item_id " +
                    "WHERE i.quantity <= a.alert_threshold";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("=+=+= Alerts Report =+=+=");
            while (rs.next()) {
                System.out.println("Alert ID: " + rs.getInt("alert_id"));
                System.out.println("Item ID: " + rs.getString("item_id"));
                System.out.println("Item Name: " + rs.getString("name"));
                System.out.println("Current Quantity: " + rs.getInt("quantity"));
                System.out.println("Alert Threshold: " + rs.getInt("alert_threshold"));
                System.out.println("Alert Date: " + rs.getDate("alert_date"));
                System.out.println("-------------------------------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static LocalDate validateDateInput(String prompt) {
        Scanner scanner = new Scanner(System.in);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = null;

        while (date == null) {
            System.out.print(prompt);
            String dateInput = scanner.nextLine();
            try {
                date = LocalDate.parse(dateInput, dateFormatter);
                if (date.isAfter(LocalDate.now())) {
                    System.out.println("Date cannot be in the future. Please enter a valid date.");
                    date = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please enter the date in yyyy-MM-dd format.");
            }
        }
        return date;
    }

    private static void saveReportToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
