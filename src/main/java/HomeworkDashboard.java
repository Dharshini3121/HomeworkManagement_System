import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HomeworkDashboard extends JFrame {

    JComboBox<String> subjectBox, priorityBox;
    JTextField taskField, deadlineField;
    JTable table;
    DefaultTableModel model;
    JProgressBar progressBar;
    JLabel summaryLabel;

    public HomeworkDashboard() {

        setTitle("Homework Management App");
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 245, 255));
        mainPanel.setBorder(
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JLabel title = new JLabel(
            "HOMEWORK MANAGEMENT SYSTEM",
            SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(25, 65, 140));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 12));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(
            BorderFactory.createTitledBorder("Homework Details")
        );

        subjectBox = new JComboBox<>(new String[]{
            "OOPS", "DS", "DPCO", "DBMS",
            "Maths", "Design Thinking"
        });

        taskField = new JTextField();
        deadlineField = new JTextField("2026-10-28");

        priorityBox = new JComboBox<>(new String[]{
            "High", "Medium", "Low"
        });

        formPanel.add(new JLabel("Subject:"));
        formPanel.add(subjectBox);

        formPanel.add(new JLabel("Homework Name:"));
        formPanel.add(taskField);

        formPanel.add(new JLabel("Deadline (YYYY-MM-DD):"));
        formPanel.add(deadlineField);

        formPanel.add(new JLabel("Priority:"));
        formPanel.add(priorityBox);

        JButton addButton = createButton("ADD", new Color(30, 130, 76));
        JButton editButton = createButton("EDIT", new Color(45, 105, 190));
        JButton deleteButton = createButton("DELETE", new Color(200, 55, 55));
        JButton completeButton = createButton(
            "COMPLETE", new Color(120, 70, 170)
        );

        formPanel.add(addButton);
        formPanel.add(editButton);
        formPanel.add(deleteButton);
        formPanel.add(completeButton);

        mainPanel.add(formPanel, BorderLayout.WEST);

        model = new DefaultTableModel(
            new String[]{
                "ID", "Subject", "Homework",
                "Deadline", "Priority", "Status"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        table.getTableHeader().setBackground(
            new Color(35, 85, 160)
        );
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        mainPanel.add(
            new JScrollPane(table),
            BorderLayout.CENTER
        );
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        bottomPanel.setBackground(new Color(240, 245, 255));

        summaryLabel = new JLabel(
            "Total: 0 | Completed: 0 | Pending: 0",
            SwingConstants.CENTER
        );
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 15));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        bottomPanel.add(summaryLabel);
        bottomPanel.add(progressBar);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        addButton.addActionListener(e -> addHomework());
        editButton.addActionListener(e -> editHomework());
        deleteButton.addActionListener(e -> deleteHomework());
        completeButton.addActionListener(e -> completeHomework());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillSelectedTask();
            }
        });

        loadHomework();
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        return button;
    }

    private boolean validateForm() {

        if (taskField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Please enter Homework Name!"
            );
            return false;
        }

        try {
            Date.valueOf(deadlineField.getText().trim());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this, "Enter a valid date in YYYY-MM-DD format!"
            );
            return false;
        }

        return true;
    }

    private void addHomework() {

        if (!validateForm()) return;

        String sql = "INSERT INTO homework "
                + "(subject, task_name, deadline, priority, status) "
                + "VALUES (?, ?, ?, ?, 'Pending')";

        try (
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, subjectBox.getSelectedItem().toString());
            ps.setString(2, taskField.getText().trim());
            ps.setDate(
                3, Date.valueOf(deadlineField.getText().trim())
            );
            ps.setString(4, priorityBox.getSelectedItem().toString());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Homework Added Successfully!"
            );

            clearForm();
            loadHomework();

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void editHomework() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this, "Please select a homework to edit!"
            );
            return;
        }

        if (!validateForm()) return;

        int id = Integer.parseInt(
            model.getValueAt(
                table.convertRowIndexToModel(row), 0
            ).toString()
        );

        String sql = "UPDATE homework SET "
                + "subject=?, task_name=?, deadline=?, priority=? "
                + "WHERE id=?";

        try (
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setString(1, subjectBox.getSelectedItem().toString());
            ps.setString(2, taskField.getText().trim());
            ps.setDate(
                3, Date.valueOf(deadlineField.getText().trim())
            );
            ps.setString(4, priorityBox.getSelectedItem().toString());
            ps.setInt(5, id);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Homework Updated Successfully!"
            );

            clearForm();
            loadHomework();

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void deleteHomework() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this, "Please select a homework to delete!"
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this homework?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        int id = Integer.parseInt(
            model.getValueAt(
                table.convertRowIndexToModel(row), 0
            ).toString()
        );

        try (
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "DELETE FROM homework WHERE id=?"
            )
        ) {

            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Homework Deleted Successfully!"
            );

            clearForm();
            loadHomework();

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void completeHomework() {

        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(
                this, "Please select a homework!"
            );
            return;
        }

        int id = Integer.parseInt(
            model.getValueAt(
                table.convertRowIndexToModel(row), 0
            ).toString()
        );

        try (
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "UPDATE homework SET status='Completed' WHERE id=?"
            )
        ) {

            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Homework Marked as Completed!"
            );

            clearForm();
            loadHomework();

        } catch (SQLException e) {
            showError(e);
        }
    }
    private void loadHomework() {

        model.setRowCount(0);

        int total = 0;
        int completed = 0;

        String sql = "SELECT * FROM homework "
                + "ORDER BY deadline ASC, "
                + "FIELD(priority, 'High', 'Medium', 'Low')";

        try (
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                String status = rs.getString("status");

                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("subject"),
                    rs.getString("task_name"),
                    rs.getDate("deadline"),
                    rs.getString("priority"),
                    status
                });

                total++;

                if ("Completed".equalsIgnoreCase(status)) {
                    completed++;
                }
            }

            int pending = total - completed;

            summaryLabel.setText(
                "Total: " + total
                + " | Completed: " + completed
                + " | Pending: " + pending
            );

            int percentage = total == 0
                    ? 0
                    : (completed * 100) / total;

            progressBar.setValue(percentage);
            progressBar.setString(percentage + "% Completed");

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void fillSelectedTask() {

        int row = table.getSelectedRow();

        if (row == -1) return;

        int modelRow = table.convertRowIndexToModel(row);

        subjectBox.setSelectedItem(
            model.getValueAt(modelRow, 1).toString()
        );

        taskField.setText(
            model.getValueAt(modelRow, 2).toString()
        );

        deadlineField.setText(
            model.getValueAt(modelRow, 3).toString()
        );

        priorityBox.setSelectedItem(
            model.getValueAt(modelRow, 4).toString()
        );
    }

    private void clearForm() {
        taskField.setText("");
        deadlineField.setText(
            java.time.LocalDate.now().toString()
        );
        subjectBox.setSelectedIndex(0);
        priorityBox.setSelectedIndex(0);
        table.clearSelection();
    }

    private void showError(SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            "Database Error: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new HomeworkDashboard().setVisible(true);
        });
    }
}