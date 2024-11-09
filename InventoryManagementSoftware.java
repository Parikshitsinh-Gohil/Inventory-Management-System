import java.sql.*;
import java.util.Scanner;
import dsa.CustomLinkedList;
import dsa.StringQueue;

public class InventoryManagementSoftware {
    private static CustomLinkedList operationHistory = new CustomLinkedList();
    private static StringQueue pendingTransactions = new StringQueue();
    private static Scanner sc = new Scanner(System.in);
    public static manageItems mi = new manageItems();
    public static transactionHandler th = new transactionHandler();
    public static generateReport gr = new generateReport();
    public static manageSupplier ms = new manageSupplier();

    public static class DatabaseConnection {
        public static Connection getConnection() throws SQLException {
            String url = "jdbc:mysql://localhost:3306/InventoryDB";
            String user = "root";
            String password = "";
            return DriverManager.getConnection(url, user, password);
        }
    }

    public static void main(String[] args) {
        boolean running = true;

        operationHistory = new CustomLinkedList();
        pendingTransactions = new StringQueue();

        while (running) {
            System.out.println("=+=+= Main Menu =+=+=");
            System.out.println("1. Manage Items");
            System.out.println("2. Process Transactions");
            System.out.println("3. Generate Reports");
            System.out.println("4. View Inventory Alerts");
            System.out.println("5. Manage Suppliers");
            System.out.println("6. Exit");
            System.out.print("Select an option: ");

            int choice = getValidInt();

            switch (choice) {
                case 1:
                    mi.manageItems();
                    break;
                case 2:
                    th.transactionHandler();
                    break;
                case 3:
                    gr.generateReports();
                    break;
                case 4:
                    viewInventoryAlert();
                    break;
                case 5:
                    ms.manageSuppliers();
                    break;
                case 6:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            operationHistory.addLast("User selected option " + choice);

            while (!pendingTransactions.isEmpty()) {
                String transaction = pendingTransactions.poll();
                System.out.println("Processing pending transaction: " + transaction);
            }
        }

        System.out.println("Operation History:");
        for (String operation : operationHistory) {
            System.out.println(operation);
        }

        sc.close();
    }
    private static void viewInventoryAlert() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT a.alert_id, a.item_id, a.alert_threshold, a.alert_date, i.quantity, i.name " +
                    "FROM inventoryalert a " +
                    "JOIN Items i ON a.item_id = i.item_id " +
                    "WHERE i.quantity <= a.alert_threshold";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("=+=+= Inventory Alerts =+=+=");
            boolean hasAlerts = false;
            while (rs.next()) {
                hasAlerts = true;
                System.out.println("Alert ID: " + rs.getInt("alert_id"));
                System.out.println("Item ID: " + rs.getString("item_id"));
                System.out.println("Item Name: " + rs.getString("name"));
                System.out.println("Current Quantity: " + rs.getInt("quantity"));
                System.out.println("Alert Threshold: " + rs.getInt("alert_threshold"));
                System.out.println("Alert Date: " + rs.getDate("alert_date"));
                System.out.println("-------------------------------");
            }

            if (!hasAlerts) {
                System.out.println("No inventory alert");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving inventory alerts.");
        }
    }

    public static class InvalidInput extends Exception {
        public InvalidInput(String message) {
            super(message);
        }
    }

    private static int getValidInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid number: ");
            }
        }
    }
}
