package org.duckdns.whocaresleft.view.swing;

import java.util.List;

import javax.swing.JPanel;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Department;
import org.duckdns.whocaresleft.presenter.DepartmentPresenter;
import org.duckdns.whocaresleft.view.DepartmentView;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.JList;

public class SwingDepartmentView extends JPanel implements DepartmentView {
    
    private static final long serialVersionUID = 1L;
    
    private JTextField idTextBox;
    private JTextField nameTextBox;
    private JTextField selectedIdTextBox;
    private JTextField selectedNameTextBox;
    private JList<Department> departmentList;
    private DefaultListModel<Department> departmentListModel;
    private JLabel infoLabel;
    private JLabel errorLabel;
    
    private transient DepartmentPresenter presenter;
    
    public SwingDepartmentView() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JLabel idLabel = new JLabel("Id");
        idLabel.setName("idLabel");
        GridBagConstraints gbc_idLabel = new GridBagConstraints();
        gbc_idLabel.insets = new Insets(0, 0, 5, 5);
        gbc_idLabel.anchor = GridBagConstraints.EAST;
        gbc_idLabel.gridx = 1;
        gbc_idLabel.gridy = 1;
        add(idLabel, gbc_idLabel);
        
        idTextBox = new JTextField();
        idTextBox.setName("idTextBox");
        GridBagConstraints gbc_idTextBox = new GridBagConstraints();
        gbc_idTextBox.gridwidth = 3;
        gbc_idTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_idTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_idTextBox.gridx = 2;
        gbc_idTextBox.gridy = 1;
        add(idTextBox, gbc_idTextBox);
        idTextBox.setColumns(10);
        
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setName("nameLabel");
        GridBagConstraints gbc_nameLabel = new GridBagConstraints();
        gbc_nameLabel.anchor = GridBagConstraints.EAST;
        gbc_nameLabel.insets = new Insets(0, 0, 5, 5);
        gbc_nameLabel.gridx = 1;
        gbc_nameLabel.gridy = 2;
        add(nameLabel, gbc_nameLabel);
        
        nameTextBox = new JTextField();
        nameTextBox.setName("nameTextBox");
        GridBagConstraints gbc_nameTextBox = new GridBagConstraints();
        gbc_nameTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_nameTextBox.gridwidth = 3;
        gbc_nameTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_nameTextBox.gridx = 2;
        gbc_nameTextBox.gridy = 2;
        add(nameTextBox, gbc_nameTextBox);
        nameTextBox.setColumns(10);
        
        JButton addButton = new JButton("Add");
        addButton.setName("addButton");
        addButton.setEnabled(false);
        GridBagConstraints gbc_addButton = new GridBagConstraints();
        gbc_addButton.insets = new Insets(0, 0, 5, 0);
        gbc_addButton.gridwidth = 4;
        gbc_addButton.gridx = 1;
        gbc_addButton.gridy = 3;
        add(addButton, gbc_addButton);
        
        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridheight = 2;
        gbc_scrollPane.gridwidth = 4;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 4;
        add(scrollPane, gbc_scrollPane);
        
        departmentListModel = new DefaultListModel<>();
        departmentList = new JList<>(departmentListModel);
        departmentList.setName("departmentList");
        scrollPane.setViewportView(departmentList);
        
        JLabel selectedDepartmentLabel = new JLabel("Selected Department");
        selectedDepartmentLabel.setName("selectedDepartmentLabel");
        GridBagConstraints gbc_selectedDepartmentLabel = new GridBagConstraints();
        gbc_selectedDepartmentLabel.gridwidth = 2;
        gbc_selectedDepartmentLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedDepartmentLabel.gridx = 1;
        gbc_selectedDepartmentLabel.gridy = 6;
        add(selectedDepartmentLabel, gbc_selectedDepartmentLabel);
        
        JCheckBox editDepartment = new JCheckBox("Edit");
        editDepartment.setName("editDepartment");
        editDepartment.setEnabled(false);
        GridBagConstraints gbc_editDepartment = new GridBagConstraints();
        gbc_editDepartment.insets = new Insets(0, 0, 5, 0);
        gbc_editDepartment.gridwidth = 3;
        gbc_editDepartment.gridx = 3;
        gbc_editDepartment.gridy = 6;
        add(editDepartment, gbc_editDepartment);
        
        JLabel selectedIdLabel = new JLabel("Id");
        selectedIdLabel.setName("selectedIdLabel");
        GridBagConstraints gbc_selectedIdLabel = new GridBagConstraints();
        gbc_selectedIdLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedIdLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedIdLabel.gridx = 1;
        gbc_selectedIdLabel.gridy = 7;
        add(selectedIdLabel, gbc_selectedIdLabel);
        
        selectedIdTextBox = new JTextField();
        selectedIdTextBox.setName("selectedIdTextBox");
        selectedIdTextBox.setEditable(false);
        GridBagConstraints gbc_selectedidTextBox = new GridBagConstraints();
        gbc_selectedidTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_selectedidTextBox.gridwidth = 3;
        gbc_selectedidTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectedidTextBox.gridx = 2;
        gbc_selectedidTextBox.gridy = 7;
        add(selectedIdTextBox, gbc_selectedidTextBox);
        selectedIdTextBox.setColumns(10);
        
        JLabel selectedNameLabel = new JLabel("Name");
        selectedNameLabel.setName("selectedNameLabel");
        GridBagConstraints gbc_selectedNameLabel = new GridBagConstraints();
        gbc_selectedNameLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedNameLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedNameLabel.gridx = 1;
        gbc_selectedNameLabel.gridy = 8;
        add(selectedNameLabel, gbc_selectedNameLabel);
        
        selectedNameTextBox = new JTextField();
        selectedNameTextBox.setName("selectedNameTextBox");
        selectedNameTextBox.setEditable(false);
        GridBagConstraints gbc_selectednameTextBox = new GridBagConstraints();
        gbc_selectednameTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_selectednameTextBox.gridwidth = 3;
        gbc_selectednameTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectednameTextBox.gridx = 2;
        gbc_selectednameTextBox.gridy = 8;
        add(selectedNameTextBox, gbc_selectednameTextBox);
        selectedNameTextBox.setColumns(10);
        
        JButton deleteButton = new JButton("Delete selected");
        deleteButton.setName("deleteButton");
        deleteButton.setEnabled(false);
        GridBagConstraints gbc_deleteButton = new GridBagConstraints();
        gbc_deleteButton.gridwidth = 2;
        gbc_deleteButton.insets = new Insets(0, 0, 5, 5);
        gbc_deleteButton.gridx = 1;
        gbc_deleteButton.gridy = 9;
        add(deleteButton, gbc_deleteButton);
        
        JButton updateButton = new JButton("Update selected");
        updateButton.setName("updateButton");
        updateButton.setEnabled(false);
        GridBagConstraints gbc_updateButton = new GridBagConstraints();
        gbc_updateButton.insets = new Insets(0, 0, 5, 0);
        gbc_updateButton.gridwidth = 2;
        gbc_updateButton.gridx = 3;
        gbc_updateButton.gridy = 9;
        add(updateButton, gbc_updateButton);
        
        infoLabel = new JLabel(" ");
        infoLabel.setName("infoLabel");
        GridBagConstraints gbc_infoLabel = new GridBagConstraints();
        gbc_infoLabel.insets = new Insets(0, 0, 5, 0);
        gbc_infoLabel.gridwidth = 4;
        gbc_infoLabel.gridx = 1;
        gbc_infoLabel.gridy = 10;
        add(infoLabel, gbc_infoLabel);
        
        errorLabel = new JLabel(" ");
        errorLabel.setName("errorLabel");
        errorLabel.setForeground(new Color(237, 51, 59));
        GridBagConstraints gbc_errorLabel = new GridBagConstraints();
        gbc_errorLabel.gridwidth = 4;
        gbc_errorLabel.gridx = 1;
        gbc_errorLabel.gridy = 11;
        add(errorLabel, gbc_errorLabel);
        
        KeyAdapter addButtonEnabler = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                addButton.setEnabled(
                    !idTextBox.getText().isBlank() &&
                    !nameTextBox.getText().isBlank());
            }
        };
        idTextBox.addKeyListener(addButtonEnabler);
        nameTextBox.addKeyListener(addButtonEnabler);
        
        addButton.addActionListener(e -> {
            try {
                Department department = Department.createDepartment(
                    Id.createId(idTextBox.getText()),
                    nameTextBox.getText());
                
                new Thread(() -> presenter.addDepartment(department)).start();
                
                idTextBox.setText("");
                nameTextBox.setText("");
                addButton.setEnabled(false);
                showInfoMessage("Adding Department...");
            } catch (IllegalArgumentException iae) {
                showErrorMessage("Id contains invalid value: Letters, digits, and underscores only");
            }
        });
        
        departmentList.addListSelectionListener(e -> {
            boolean isDepartmentSelected = !departmentList.isSelectionEmpty();
            
            deleteButton.setEnabled(isDepartmentSelected);
            editDepartment.setEnabled(isDepartmentSelected);
            
            if (isDepartmentSelected) {
                editDepartment.setSelected(false);
                
                Department d = departmentList.getSelectedValue();
                
                selectedIdTextBox.setText(d.getId().getValue());
                selectedNameTextBox.setText(d.getName());
            }
        });
        
        editDepartment.addActionListener(e -> {
            boolean isTicked = editDepartment.isSelected();
            deleteButton.setEnabled(!isTicked);
            
            selectedNameTextBox.setEditable(isTicked);
            
            updateButton.setEnabled(isTicked && isUpdatePossible());
        });
        
        KeyAdapter updateButtonEnabler = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateButton.setEnabled(isUpdatePossible());
            }
        };
        selectedNameTextBox.addKeyListener(updateButtonEnabler);
        
        deleteButton.addActionListener(e -> {
            Department d = departmentList.getSelectedValue();
            
            new Thread(() -> presenter.removeDepartment(d)).start();
            
            departmentList.clearSelection();
            
            selectedIdTextBox.setText("");
            selectedNameTextBox.setText("");
            
            editDepartment.setEnabled(false);
            showInfoMessage("Deleting Department...");
        });
        
        updateButton.addActionListener(e -> {
            Department current = departmentList.getSelectedValue();
            Department updated = Department.createDepartment(
                current.getId(), selectedNameTextBox.getText());
            
            new Thread(() -> presenter.updateDepartment(current, updated)).start();
            
            editDepartment.setSelected(false);
            showInfoMessage("Updating Department...");
        });
    }
    
    public void setPresenter(DepartmentPresenter presenter) { this.presenter = presenter; }
    
    public DefaultListModel<Department> getDepartmentListModel() { return departmentListModel; }
    
    private boolean isUpdatePossible() {
        Department d = departmentList.getSelectedValue();
        String written = selectedNameTextBox.getText();
        
        return !(written.isBlank() || written.equals(d.getName()));
    }
    
    private void showInfoMessage(String message) { infoLabel.setText(message); }
    private void clearInfoLabel() { showInfoMessage(" "); }
    
    private void showErrorMessage(String message) { errorLabel.setText(message); }
    private void clearErrorLabel() { showErrorMessage(" "); }
    
    private void addToList(Department toAdd) { departmentListModel.addElement(toAdd); }
    private void removeFromList(Department toRemove) { departmentListModel.removeElement(toRemove); }
    
    @Override
    public void showAllDepartments(List<Department> departments) {
        SwingUtilities.invokeLater(() ->
            departments.forEach(this::addToList));
    }
    
    @Override
    public void departmentAdded(Department department) {
        SwingUtilities.invokeLater(() -> {
            addToList(department);
            showInfoMessage("Department added!");
            clearErrorLabel();
        });
    }
    
    @Override
    public void departmentRemoved(Department department) {
        SwingUtilities.invokeLater(() -> {
            removeFromList(department);
            showInfoMessage("Department removed!");
            clearErrorLabel();
        });
    }
    
    @Override
    public void departmentUpdated(Department oldDepartment, Department newDepartment) {
        SwingUtilities.invokeLater(() -> {
            int oldDepartmentIndex = departmentListModel.indexOf(oldDepartment);
            departmentListModel.setElementAt(newDepartment, oldDepartmentIndex);
            showInfoMessage("Department updated!");
            clearErrorLabel();
        });
    }
    
    @Override
    public void showErrorDuplicateDepartment(Department found) {
        SwingUtilities.invokeLater(() -> {
            clearInfoLabel();
            showErrorMessage("A Department with id " + found.getId() + " already exists");
        });
    }
    
    @Override
    public void showErrorDepartmentNotFound(Department notFound) {
        SwingUtilities.invokeLater(() -> {
            clearInfoLabel();
            showErrorMessage("No Department with id " + notFound.getId() + " was found");
        });
    }
}
