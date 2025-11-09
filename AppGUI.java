import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import com.formdev.flatlaf.FlatDarkLaf;
import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;

public class AppGUI extends JFrame implements ActionListener {
    private final JTextField studentIdField, firstNameField, lastNameField, majorField, phoneField, gpaField;
    private final JDateChooser dobChooser;
    private final JButton addButton, displayButton, sortButton, searchButton, modifyButton, deleteButton, exportButton;
    private JTextField searchBar;

    private Statement stmt;
    private Connection conn;

    private JTable table;
    private JScrollPane tableScrollPane;
    private TableRowSorter<TableModel> sorter;

    private final Color bgColor = new Color(30, 30, 30);
    private final Color panelColor = new Color(40, 40, 40);
    private final Color accent = new Color(0, 122, 204);
    private final Color textColor = new Color(230, 230, 230);

    public AppGUI() {
        FlatDarkLaf.setup();

        setTitle("Student Database Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(panelColor);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Student Database Manager", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 22));
        title.setForeground(accent);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        String[] labels = {"Student ID:", "First Name:", "Last Name:", "Major:", "Phone:", "GPA:", "DOB:"};
        JTextField[] fields = {
                studentIdField = new JTextField(15),
                firstNameField = new JTextField(15),
                lastNameField = new JTextField(15),
                majorField = new JTextField(15),
                phoneField = new JTextField(15),
                gpaField = new JTextField(15)
        };

        gbc.gridwidth = 1;
        int row = 1;
        for (int i = 0; i < labels.length - 1; i++) {
            gbc.gridx = 0; gbc.gridy = row;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(textColor);
            panel.add(label, gbc);

            gbc.gridx = 1;
            fields[i].setBackground(new Color(55, 55, 55));
            fields[i].setForeground(Color.WHITE);
            fields[i].setCaretColor(Color.WHITE);
            fields[i].setBorder(new LineBorder(new Color(80, 80, 80)));
            panel.add(fields[i], gbc);
            row++;
        }

        gbc.gridx = 0; gbc.gridy = row;
        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dobLabel.setForeground(textColor);
        panel.add(dobLabel, gbc);

        gbc.gridx = 1;
        dobChooser = new JDateChooser();
        dobChooser.setDateFormatString("yyyy-MM-dd");
        dobChooser.setPreferredSize(new Dimension(150, 28));
        panel.add(dobChooser, gbc);
        row++;

        addButton = makeButton("Add", new Color(76, 175, 80));
        displayButton = makeButton("Display", accent);
        sortButton = makeButton("Sort", new Color(255, 193, 7));
        searchButton = makeButton("Search", new Color(171, 71, 188));
        modifyButton = makeButton("Modify", new Color(244, 67, 54));
        deleteButton = makeButton("Delete", new Color(255, 87, 34));
        exportButton = makeButton("Export CSV", new Color(100, 181, 246));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(panelColor);
        buttonPanel.add(addButton);
        buttonPanel.add(displayButton);
        buttonPanel.add(sortButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(modifyButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(exportButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        gbc.gridy = ++row;
        JLabel searchLabel = new JLabel("Live Search:");
        searchLabel.setForeground(textColor);
        panel.add(searchLabel, gbc);

        gbc.gridx = 1;
        searchBar = new JTextField(15);
        searchBar.setBackground(new Color(55, 55, 55));
        searchBar.setForeground(Color.WHITE);
        searchBar.setCaretColor(Color.WHITE);
        searchBar.setBorder(new LineBorder(new Color(80, 80, 80)));
        panel.add(searchBar, gbc);
        row++;

        table = new JTable();
        styleTable(table);
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(500, 250));
        tableScrollPane.getViewport().setBackground(new Color(45, 45, 45));
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(tableScrollPane, gbc);

        add(panel, BorderLayout.CENTER);
        getContentPane().setBackground(bgColor);

        try {
            dbConnect db = new dbConnect();
            conn = db.getConnection();
            stmt = conn.createStatement();
            loadTableData(); // load immediately
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        searchBar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applySearchFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applySearchFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearchFilter(); }
        });

        setVisible(true);
    }

    private void applySearchFilter() {
        if (sorter != null) {
            String text = searchBar.getText();
            if (text.trim().length() == 0)
                sorter.setRowFilter(null);
            else
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private JButton makeButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new LineBorder(color.darker(), 1));
        b.setPreferredSize(new Dimension(100, 36));
        b.addActionListener(this);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { b.setBackground(color.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { b.setBackground(color); }
        });
        return b;
    }

    private void styleTable(JTable table) {
        table.setBackground(new Color(45, 45, 45));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(22);
        table.setGridColor(new Color(80, 80, 80));
        table.setSelectionBackground(new Color(60, 60, 60));
        table.setSelectionForeground(Color.WHITE);
        table.getTableHeader().setBackground(new Color(30, 30, 30));
        table.getTableHeader().setForeground(accent);
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
    }

    private void loadTableData() {
        try {
            Table tb = new Table();
            ResultSet rs = stmt.executeQuery("SELECT * FROM sdata");
            table.setModel(tb.buildTableModel(rs));
            sorter = new TableRowSorter<>(table.getModel());
            table.setRowSorter(sorter);
            styleTable(table);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void exportToCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export to CSV");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                String path = fileChooser.getSelectedFile().getAbsolutePath();
                if (!path.toLowerCase().endsWith(".csv")) path += ".csv";
                FileWriter fw = new FileWriter(path);

                ResultSet rs = stmt.executeQuery("SELECT * FROM sdata");
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                // Headers
                for (int i = 1; i <= colCount; i++) {
                    fw.write(meta.getColumnName(i) + (i < colCount ? "," : "\n"));
                }
                // Rows
                while (rs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        fw.write(rs.getString(i) + (i < colCount ? "," : "\n"));
                    }
                }
                fw.close();
                JOptionPane.showMessageDialog(this, "✅ Exported successfully to CSV!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Export failed!");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Table tb = new Table();

        if (e.getSource() == addButton) {
            try {
                double gpa = Double.parseDouble(gpaField.getText());
                if (gpa < 0.0 || gpa > 10.0) {
                    JOptionPane.showMessageDialog(this, "GPA must be between 0 and 10.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid GPA format.");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dob = (dobChooser.getDate() != null) ? sdf.format(dobChooser.getDate()) : "";

            String sql = "INSERT INTO sdata VALUES('" + studentIdField.getText() + "', '"
                    + firstNameField.getText() + "', '" + lastNameField.getText() + "', '"
                    + majorField.getText().toUpperCase() + "', '" + phoneField.getText() + "', '"
                    + gpaField.getText() + "', '" + dob + "')";
            try {
                stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "✅ Student added successfully!");
                loadTableData();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to add student.");
            }
        }

        if (e.getSource() == displayButton) loadTableData();

        if (e.getSource() == sortButton) {
            String[] options = {"First Name", "Last Name", "Major"};
            int choice = JOptionPane.showOptionDialog(this, "Sort by:", "Sort",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice == -1) return;
            String sql = switch (choice) {
                case 0 -> "SELECT * FROM sdata ORDER BY first_name";
                case 1 -> "SELECT * FROM sdata ORDER BY last_name";
                case 2 -> "SELECT * FROM sdata ORDER BY major";
                default -> "";
            };
            try {
                ResultSet rs = stmt.executeQuery(sql);
                table.setModel(tb.buildTableModel(rs));
                sorter = new TableRowSorter<>(table.getModel());
                table.setRowSorter(sorter);
                styleTable(table);
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        if (e.getSource() == searchButton) {
            String[] options = {"Student ID", "Last Name", "Major"};
            int choice = JOptionPane.showOptionDialog(this, "Search by:", "Search",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice == -1) return;
            String column = switch (choice) {
                case 0 -> "student_id";
                case 1 -> "last_name";
                case 2 -> "major";
                default -> "";
            };
            String term = JOptionPane.showInputDialog(this, "Enter search term:");
            if (term == null || term.isEmpty()) return;
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM sdata WHERE " + column + " LIKE '%" + term + "%'");
                table.setModel(tb.buildTableModel(rs));
                sorter = new TableRowSorter<>(table.getModel());
                table.setRowSorter(sorter);
                styleTable(table);
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        if (e.getSource() == modifyButton) {
            String studentId = JOptionPane.showInputDialog(this, "Enter student ID:");
            if (studentId == null || studentId.isEmpty()) return;
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM sdata WHERE student_id = '" + studentId + "'");
                if (rs.next()) {
                    String[] options = {"First Name", "Last Name", "Major", "Phone", "GPA", "Date of Birth"};
                    int choice = JOptionPane.showOptionDialog(this, "Select field to modify:", "Modify",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                    if (choice == -1) return;
                    String column = switch (choice) {
                        case 0 -> "first_name";
                        case 1 -> "last_name";
                        case 2 -> "major";
                        case 3 -> "phone";
                        case 4 -> "gpa";
                        case 5 -> "date_of_birth";
                        default -> "";
                    };
                    String newValue = JOptionPane.showInputDialog(this, "Enter new value:");
                    if (newValue == null || newValue.isEmpty()) return;
                    stmt.executeUpdate("UPDATE sdata SET " + column + "='" + newValue + "' WHERE student_id='" + studentId + "'");
                    JOptionPane.showMessageDialog(this, "✅ Student updated!");
                    loadTableData();
                } else JOptionPane.showMessageDialog(this, "❌ Student not found.");
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        if (e.getSource() == deleteButton) {
            String studentId = JOptionPane.showInputDialog(this, "Enter Student ID to delete:");
            if (studentId == null || studentId.isEmpty()) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete student with ID: " + studentId + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    int rows = stmt.executeUpdate("DELETE FROM sdata WHERE student_id = '" + studentId + "'");
                    if (rows > 0) { JOptionPane.showMessageDialog(this, "🗑️ Student deleted successfully!"); loadTableData(); }
                    else JOptionPane.showMessageDialog(this, "❌ Student not found.");
                } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error deleting student!"); }
            }
        }

        if (e.getSource() == exportButton) exportToCSV();
    }

    public static void main(String[] args) {
        new AppGUI();
    }
}
