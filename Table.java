import java.sql.*;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class Table {
    public DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        if (rs == null) throw new SQLException("ResultSet is null.");
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        Vector<String> colNames = new Vector<>();
        for (int i = 1; i <= columnCount; i++)
            colNames.add(meta.getColumnLabel(i));

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> row = new Vector<>(columnCount);
            for (int i = 1; i <= columnCount; i++)
                row.add(rs.getObject(i) != null ? rs.getObject(i) : "");
            data.add(row);
        }

        return new DefaultTableModel(data, colNames) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
}
