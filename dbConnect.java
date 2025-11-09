import java.sql.*;

public class dbConnect {
    private static Connection mycon=null;

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        String db="testingdb", user = "root", pass = "Vaidik@1904";
        String url = "jdbc:mysql://localhost:3306/"+db;
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url,user,pass);
        return conn;
    }
    public static void main(String[] args) {
    try {
        dbConnect db = new dbConnect();
        Connection conn = db.getConnection();
        System.out.println("Connection successful: " + conn);
        conn.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
