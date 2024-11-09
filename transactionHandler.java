import java.sql.*;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Scanner;

public class transactionHandler {
    public static class DatabaseConnection {
        public static Connection getConnection() throws SQLException {
            String url = "jdbc:mysql://localhost:3306/InventoryDB";
            String user = "root";
            String password = "";
            return DriverManager.getConnection(url, user, password);
        }
    }
    public static void transactionHandler() {
        try {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("=+=+= Process Transactions =+=+=");
                System.out.println("1. Record Sale");
                System.out.println("2. Record Purchase");
                System.out.println("3. View Transactions");
                System.out.println("4. Return to Main Menu");
                System.out.print("Select an option: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        recordSale();
                        break;
                    case 2:
                        recordPurchase();
                        break;
                    case 3:
                        viewTransactions();
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred in processing transactions: " + e.getMessage());
        }
    }

    private static void recordSale() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Item ID: ");
        String itemId = scanner.nextLine();
        System.out.print("Enter Quantity Sold: ");
        int quantity = scanner.nextInt();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkItemSql = "SELECT quantity, price FROM Items WHERE item_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkItemSql);
            checkStmt.setString(1, itemId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                if (currentQuantity < quantity) {
                    System.out.println("Insufficient stock. Current stock is " + currentQuantity);
                    return;
                }

                String updateItemSql = "UPDATE Items SET quantity = quantity - ? WHERE item_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateItemSql);
                updateStmt.setInt(1, quantity);
                updateStmt.setString(2, itemId);
                updateStmt.executeUpdate();

                double totalAmount = quantity * price;

                Queue<Transaction> transactionQueue = new LinkedList<>();
                transactionQueue.add(new Transaction(itemId, quantity, totalAmount));

                while (!transactionQueue.isEmpty()) {
                    Transaction transaction = transactionQueue.poll();
                    String insertTransactionSql = "INSERT INTO Transactions (item_id, quantity, transaction_type, transaction_date, total_amount) VALUES (?, ?, 'Sale', CURDATE(), ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertTransactionSql);
                    insertStmt.setString(1, transaction.getItemId());
                    insertStmt.setInt(2, transaction.getQuantity());
                    insertStmt.setDouble(3, transaction.getTotalAmount());
                    insertStmt.executeUpdate();
                }

                System.out.println("Sale recorded successfully! Total Amount: " + totalAmount);
            } else {
                System.out.println("Item not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error recording sale.");
        }
    }

    private static void recordPurchase() {
        Scanner sc= new Scanner(System.in);

        System.out.print("Enter Item ID: ");
        String itemId = sc.nextLine();
        System.out.print("Enter Quantity Purchased: ");
        int quantity = sc.nextInt();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkItemSql = "SELECT price FROM Items WHERE item_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkItemSql);
            checkStmt.setString(1, itemId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                double price = rs.getDouble("price");

                String updateItemSql = "UPDATE Items SET quantity = quantity + ? WHERE item_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateItemSql);
                updateStmt.setInt(1, quantity);
                updateStmt.setString(2, itemId);
                updateStmt.executeUpdate();

                double totalAmount = quantity * price;

                Queue<Transaction> transactionQueue = new LinkedList<>();
                transactionQueue.add(new Transaction(itemId, quantity, totalAmount));

                while (!transactionQueue.isEmpty()) {
                    Transaction transaction = transactionQueue.poll();
                    String insertTransactionSql = "INSERT INTO Transactions (item_id, quantity, transaction_type, transaction_date, total_amount) VALUES (?, ?, 'Purchase', CURDATE(), ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertTransactionSql);
                    insertStmt.setString(1, transaction.getItemId());
                    insertStmt.setInt(2, transaction.getQuantity());
                    insertStmt.setDouble(3, transaction.getTotalAmount());
                    insertStmt.executeUpdate();
                }

                System.out.println("Purchase recorded successfully! Total Amount: " + totalAmount);
            } else {
                System.out.println("Item not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error recording purchase.");
        }
    }

    private static void viewTransactions() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Transactions";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("=+=+= Transactions List =+=+=");
            while (rs.next()) {
                System.out.println("Transaction ID: " + rs.getInt("transaction_id"));
                System.out.println("Item ID: " + rs.getString("item_id"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("Transaction Type: " + rs.getString("transaction_type"));
                System.out.println("Transaction Date: " + rs.getDate("transaction_date"));
                System.out.println("Total Amount: " + rs.getDouble("total_amount"));
                System.out.println("-------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving transactions.");
        }
    }

    private static class Transaction {
        private final String itemId;
        private final int quantity;
        private final double totalAmount;

        public Transaction(String itemId, int quantity, double totalAmount) {
            this.itemId = itemId;
            this.quantity = quantity;
            this.totalAmount = totalAmount;
        }

        public String getItemId() {
            return itemId;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotalAmount() {
            return totalAmount;
        }
    }
}
