import java.sql.*;
import java.util.Scanner;
public class manageSupplier {
    public static class DatabaseConnection {
        public static Connection getConnection() throws SQLException {
            String url = "jdbc:mysql://localhost:3306/InventoryDB";
            String user = "root";
            String password = "";
            return DriverManager.getConnection(url, user, password);
        }
    }
    public static void manageSuppliers() {
        try {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("=+=+= Manage Suppliers =+=+=");
                System.out.println("1. Add Supplier");
                System.out.println("2. Update Supplier");
                System.out.println("3. Delete Supplier");
                System.out.println("4. View Suppliers");
                System.out.println("5. Return to Main Menu");
                System.out.print("Select an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        addSupplier();
                        break;
                    case 2:
                        updateSupplier();
                        break;
                    case 3:
                        deleteSupplier();
                        break;
                    case 4:
                        viewSuppliers();
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred in managing suppliers: " + e.getMessage());
        }
    }

    private static void addSupplier() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter supplier ID: ");
        String supplierId = sc.nextLine();
        System.out.print("Enter supplier name: ");
        String supplierName = sc.nextLine();
        while (!isValidName(supplierName)) {
            System.out.print("Enter valid supplier Name: ");
            supplierName = sc.nextLine();
        }
        System.out.print("Enter supplier contact: ");
        Long contact = sc.nextLong();
        while (!isValidPhoneNumber(contact)) {
            System.out.print("Enter valid User's Phone: ");
            contact = sc.nextLong();
        }
        sc.nextLine();
        System.out.print("Enter supplier email: ");
        String supplierEmail = sc.nextLine();
        while (!checkEmailValidity(supplierEmail)) {
            System.out.print("Enter valid User's Email: ");
            supplierEmail = sc.nextLine();
        }
        System.out.print("Enter supplier address: ");
        String supplierAddress = sc.nextLine();

        String sql = "INSERT INTO Suppliers (supplier_id, supplier_name, phone, email, address) VALUES (?, ?, ?,?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplierId);
            pstmt.setString(2, supplierName);
            pstmt.setLong(3, contact);
            pstmt.setString(4, supplierEmail);
            pstmt.setString(5, supplierAddress);
            pstmt.executeUpdate();
            System.out.println("Supplier added successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error adding supplier.");
        }
    }

    private static void updateSupplier() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Supplier ID to update: ");
        String supplierId = scanner.nextLine();

        System.out.print("Enter new Supplier Name (or press Enter to keep unchanged): ");
        String supplierName = scanner.nextLine();
        System.out.print("Enter new Phone Number (or press Enter to keep unchanged): ");
        String phoneInput = scanner.nextLine();
        System.out.print("Enter new Email (or press Enter to keep unchanged): ");
        String email = scanner.nextLine();
        System.out.print("Enter new Address (or press Enter to keep unchanged): ");
        String address = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Suppliers SET supplier_name = COALESCE(?, supplier_name), phone = COALESCE(?, phone), email = COALESCE(?, email), address = COALESCE(?, address) WHERE supplier_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, supplierName.isEmpty() ? null : supplierName);
            pstmt.setObject(2, phoneInput.isEmpty() ? null : Long.parseLong(phoneInput), java.sql.Types.BIGINT);
            pstmt.setString(3, email.isEmpty() ? null : email);
            pstmt.setString(4, address.isEmpty() ? null : address);
            pstmt.setString(5, supplierId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Supplier updated successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating supplier.");
        }
    }

    private static void deleteSupplier() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Supplier ID to delete: ");
        String supplierId = scanner.nextLine();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM Suppliers WHERE supplier_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, supplierId);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Supplier deleted successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error deleting supplier.");
        }
    }

    private static void viewSuppliers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Suppliers";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("=+=+= Suppliers List =+=+=");
            while (rs.next()) {
                System.out.println("Supplier ID: " + rs.getString("supplier_id"));
                System.out.println("Supplier Name: " + rs.getString("supplier_name"));
                System.out.println("Phone: " + rs.getLong("phone"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Address: " + rs.getString("address"));
                System.out.println("-------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving suppliers.");
        }
    }

    public static boolean checkEmailValidity(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1 || email.indexOf('@', atIndex + 1) != -1) {
            return false;
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);

        if (localPart.isEmpty() || domainPart.isEmpty()) {
            return false;
        }

        if (domainPart.indexOf('.') == -1) {
            return false;
        }

        if (domainPart.startsWith(".") || domainPart.endsWith(".")) {
            return false;
        }

        if (email.length() > 254) {
            return false;
        }

        return true;
    }

    public static boolean isValidPhoneNumber(long phoneNumber) {
        return Long.toString(phoneNumber).length() == 10;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty();
    }

}
