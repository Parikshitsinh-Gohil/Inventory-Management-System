import java.sql.*;
import java.util.Scanner;
import java.util.Random;


public class manageItems {
    public static class DatabaseConnection {
        public static Connection getConnection() throws SQLException {
            String url = "jdbc:mysql://localhost:3306/InventoryDB";
            String user = "root";
            String password = "";
            return DriverManager.getConnection(url, user, password);
        }
    }
    private static Scanner sc = new Scanner(System.in);
    public static void manageItems() {
        while (true) {
            System.out.println("=+=+= Manage Items =+=+=");
            System.out.println("1. Add Item");
            System.out.println("2. Update Item");
            System.out.println("3. Delete Item");
            System.out.println("4. View Items");
            System.out.println("5. Return to Main Menu");
            System.out.print("Select an option: ");

            int choice = getValidInt();

            switch (choice) {
                case 1:
                    addItem();
                    break;
                case 2:
                    updateItem();
                    break;
                case 3:
                    deleteItem();
                    break;
                case 4:
                    viewItems();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void addItem() {
        try {
            String itemId = itemIDGenerator();
            System.out.println("Item ID: " + itemId);
            System.out.print("Enter Item Name: ");
            String itemName = sc.nextLine();

            System.out.print("Enter Quantity: ");
            int quantity = getValidInt();

            System.out.print("Enter Price: ");
            double price = getValidDouble();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Items (item_id, name, quantity, price) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, itemId);
                pstmt.setString(2, itemName);
                pstmt.setInt(3, quantity);
                pstmt.setDouble(4, price);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Item added successfully!");
                }
            } catch (SQLException e) {
                System.out.println("Error adding item: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("An error occurred while adding item: " + e.getMessage());
        }
    }

    private static void updateItem() {
        try {
            System.out.print("Enter Item ID to update: ");
            String UIitemId = sc.nextLine();

            System.out.print("Enter new Item Name (or press Enter to keep unchanged): ");
            String UIitemName = sc.nextLine();

            System.out.print("Enter new Quantity (or press Enter to keep unchanged): ");
            String UIquantityInput = sc.nextLine();

            System.out.print("Enter new Price (or press Enter to keep unchanged): ");
            String UIpriceInput = sc.nextLine();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE Items SET name = COALESCE(?, name), quantity = COALESCE(?, quantity), price = COALESCE(?, price) WHERE item_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, UIitemName.isEmpty() ? null : UIitemName);
                pstmt.setObject(2, UIquantityInput.isEmpty() ? null : Integer.parseInt(UIquantityInput),
                        java.sql.Types.INTEGER);
                pstmt.setObject(3, UIpriceInput.isEmpty() ? null : Double.parseDouble(UIpriceInput), java.sql.Types.DOUBLE);
                pstmt.setString(4, UIitemId);

                int updateRow= pstmt.executeUpdate();
                if (updateRow > 0) {
                    System.out.println("Item updated successfully!");
                }
            } catch (SQLException e) {
                System.out.println("Error updating item: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format for quantity or price: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("An error occurred while updating item: " + e.getMessage());
        }
    }

    private static void deleteItem() {
        try {
            System.out.print("Enter Item ID to delete: ");
            String itemId = sc.nextLine();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM Items WHERE item_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, itemId);

                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Item deleted successfully!");
                }
            } catch (SQLException e) {
                System.out.println("Error deleting item: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("An error occurred while deleting item: " + e.getMessage());
        }
    }

    private static void viewItems() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Items";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("ItemID | Item Name | Quantity | Price");
            while (rs.next()) {
                System.out.println(rs.getString("item_id") + " | " + rs.getString("name") + " | " +
                        rs.getInt("quantity") + " | " + rs.getDouble("price"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching items: " + e.getMessage());
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

    private static double getValidDouble() {
        while (true) {
            try {
                return Double.parseDouble(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid double: ");
            }
        }
    }

    private static final String ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";

    public static String itemIDGenerator() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);

        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(ALPHABETS.length());
            sb.append(ALPHABETS.charAt(index));
        }
        for (int i = 0; i < 3; i++) {
            int index = random.nextInt(DIGITS.length());
            sb.append(DIGITS.charAt(index));
        }
        int lastDigitIndex = random.nextInt(ALPHABETS.length());
        sb.append(ALPHABETS.charAt(lastDigitIndex));
        return sb.toString();
    }
}
