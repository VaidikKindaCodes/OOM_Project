import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class AppGUI extends JFrame implements ActionListener {
    private final JTextField studentIdField, firstNameField, lastNameField, majorField, phoneField, gpaField, dobField;
    private final JButton addButton, displayButton, sortButton, searchButton, modifyButton;
    private Statement stmt;

    private final Color bgColor = new Color(30, 30, 30);
    private final Color panelColor = new Color(40, 40, 40);
    private final Color accent = new Color(0, 122, 204);
    private final Color textColor = new Color(230, 230, 230);

    public AppGUI() {
        setTitle("🎓 Student Database Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(580, 620);
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

        String[] labels = {"Student ID:", "First Name:", "Last Name:", "Major:", "Phone:", "GPA:", "Date of Birth (yyyy-mm-dd):"};
        JTextField[] fields = {
                studentIdField = new JTextField(15),
                firstNameField = new JTextField(15),
                lastNameField = new JTextField(15),
                majorField = new JTextField(15),
                phoneField = new JTextField(15),
                gpaField = new JTextField(15),
                dobField = new JTextField(15)
        };

        gbc.gridwidth = 1;
        int row = 1;
        for (int i = 0; i < labels.length; i++) {
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

        // Buttons
        addButton = makeButton("Add", new Color(76, 175, 80));
        displayButton = makeButton("Display", accent);
        sortButton = makeButton("Sort", new Color(255, 193, 7));
        searchButton = makeButton("Search", new Color(171, 71, 188));
        modifyButton = makeButton("Modify", new Color(244, 67, 54));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(panelColor);
        buttonPanel.add(addButton);
        buttonPanel.add(displayButton);
        buttonPanel.add(sortButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(modifyButton);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        add(panel, BorderLayout.CENTER);
        getContentPane().setBackground(bgColor);
        setVisible(true);
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

        // Hover effect
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(color);
            }
        });

        return b;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        dbConnect db = new dbConnect();
        Connection conn;
        try {
            conn = db.getConnection();
        } catch (SQLException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "❌ Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try { stmt = conn.createStatement(); }
        catch (SQLException ex) { ex.printStackTrace(); return; }

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

            String sql = "INSERT INTO sdata VALUES('" + studentIdField.getText() + "', '"
                    + firstNameField.getText() + "', '" + lastNameField.getText() + "', '"
                    + majorField.getText() + "', '" + phoneField.getText() + "', '"
                    + gpaField.getText() + "', '" + dobField.getText() + "')";
            try {
                stmt.executeUpdate(sql);
                JOptionPane.showMessageDialog(this, "✅ Student added successfully!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "❌ Failed to add student.");
            }
        }

        if (e.getSource() == displayButton) {
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM sdata");
                JTable table = createStyledTable(tb.buildTableModel(rs));
                JOptionPane.showMessageDialog(this, new JScrollPane(table), "All Students", JOptionPane.PLAIN_MESSAGE);
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        if (e.getSource() == sortButton) {
            String[] options = {"First Name", "Last Name", "Major"};
            int choice = JOptionPane.showOptionDialog(this, "Sort by:", "Sort", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice == -1) return;

            String sql = switch (choice) {
                case 0 -> "SELECT * FROM sdata ORDER BY first_name";
                case 1 -> "SELECT * FROM sdata ORDER BY last_name";
                case 2 -> "SELECT * FROM sdata ORDER BY major";
                default -> "";
            };
            try {
                ResultSet rs = stmt.executeQuery(sql);
                JTable table = createStyledTable(tb.buildTableModel(rs));
                JOptionPane.showMessageDialog(this, new JScrollPane(table), "Sorted Results", JOptionPane.PLAIN_MESSAGE);
            } catch (SQLException ex) { ex.printStackTrace(); }
        }

        if (e.getSource() == searchButton) {
            String[] options = {"Student ID", "Last Name", "Major"};
            int choice = JOptionPane.showOptionDialog(this, "Search by:", "Search", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (choice == -1) return;
            String column = switch (choice) {
                case 0 -> "student_id";
                case 1 -> "last_name";
                case 2 -> "major";
                default -> "";
            };
            String searchTerm = JOptionPane.showInputDialog(this, "Enter search term:");
            if (searchTerm == null || searchTerm.isEmpty()) return;

            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM sdata WHERE " + column + " LIKE '%" + searchTerm + "%'");
                JTable table = createStyledTable(tb.buildTableModel(rs));
                JOptionPane.showMessageDialog(this, new JScrollPane(table), "Search Results", JOptionPane.PLAIN_MESSAGE);
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
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Student not found.");
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private JTable createStyledTable(javax.swing.table.DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(new Color(45, 45, 45));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(22);
        table.setGridColor(new Color(80, 80, 80));
        table.getTableHeader().setBackground(new Color(30, 30, 30));
        table.getTableHeader().setForeground(accent);
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
        return table;
    }
}
