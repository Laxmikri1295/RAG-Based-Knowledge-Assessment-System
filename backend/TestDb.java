import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDb {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://ep-dry-dust-an3jza8u-pooler.c-6.us-east-1.aws.neon.tech/neondb?sslmode=require";
        String user = "neondb_owner";
        String password = "npg_1te9IxPOCqag";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // Check columns
            System.out.println("--- Columns in 'documents' table ---");
            ResultSet rs = stmt.executeQuery("SELECT column_name, data_type, udt_name FROM information_schema.columns WHERE table_name = 'documents';");
            while (rs.next()) {
                System.out.println(rs.getString("column_name") + " : " + rs.getString("data_type") + " : " + rs.getString("udt_name"));
            }

            // Check rows count
            System.out.println("\n--- Row count in 'documents' ---");
            ResultSet rs2 = stmt.executeQuery("SELECT count(*) FROM documents;");
            if (rs2.next()) {
                System.out.println("Count: " + rs2.getInt(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
