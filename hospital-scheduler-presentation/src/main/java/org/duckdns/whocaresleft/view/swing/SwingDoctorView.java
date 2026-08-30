package org.duckdns.whocaresleft.view.swing;

import java.util.List;

import javax.swing.JPanel;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Doctor;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.duckdns.whocaresleft.view.DoctorView;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JCheckBox;
import javax.swing.JSeparator;
import java.awt.Color;
import javax.swing.JScrollPane;

public class SwingDoctorView extends JPanel implements DoctorView {
    
    private static final long serialVersionUID = 1L;
    
    private JTextField idTextBox;
    private JTextField firstNameTextBox;
    private JTextField lastNameTextBox;
    private JTextField selectedIdTextBox;
    private JTextField selectedFirstNameTextBox;
    private JTextField selectedLastNameTextBox;
    private JList<Doctor> doctorList;
    private DefaultListModel<Doctor> doctorListModel;
    private JLabel infoLabel;
    private JLabel errorLabel;
    
    private transient DoctorPresenter presenter;
    
    public SwingDoctorView() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
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
        
        JLabel firstNameLabel = new JLabel("First Name");
        firstNameLabel.setName("firstNameLabel");
        GridBagConstraints gbc_firstNameLabel = new GridBagConstraints();
        gbc_firstNameLabel.anchor = GridBagConstraints.EAST;
        gbc_firstNameLabel.insets = new Insets(0, 0, 5, 5);
        gbc_firstNameLabel.gridx = 1;
        gbc_firstNameLabel.gridy = 2;
        add(firstNameLabel, gbc_firstNameLabel);
        
        firstNameTextBox = new JTextField();
        firstNameTextBox.setName("firstNameTextBox");
        GridBagConstraints gbc_firstNameTextBox = new GridBagConstraints();
        gbc_firstNameTextBox.gridwidth = 3;
        gbc_firstNameTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_firstNameTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_firstNameTextBox.gridx = 2;
        gbc_firstNameTextBox.gridy = 2;
        add(firstNameTextBox, gbc_firstNameTextBox);
        firstNameTextBox.setColumns(10);
        
        JLabel lastNameLabel = new JLabel("Last Name");
        lastNameLabel.setName("lastNameLabel");
        GridBagConstraints gbc_lastNameLabel = new GridBagConstraints();
        gbc_lastNameLabel.anchor = GridBagConstraints.EAST;
        gbc_lastNameLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lastNameLabel.gridx = 1;
        gbc_lastNameLabel.gridy = 3;
        add(lastNameLabel, gbc_lastNameLabel);
        
        lastNameTextBox = new JTextField();
        lastNameTextBox.setName("lastNameTextBox");
        GridBagConstraints gbc_lastNameTextBox = new GridBagConstraints();
        gbc_lastNameTextBox.gridwidth = 3;
        gbc_lastNameTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_lastNameTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_lastNameTextBox.gridx = 2;
        gbc_lastNameTextBox.gridy = 3;
        add(lastNameTextBox, gbc_lastNameTextBox);
        lastNameTextBox.setColumns(10);
        
        JButton addButton = new JButton("Add");
        addButton.setName("addButton");
        addButton.setEnabled(false);
        GridBagConstraints gbc_addButton = new GridBagConstraints();
        gbc_addButton.insets = new Insets(0, 0, 5, 0);
        gbc_addButton.gridwidth = 4;
        gbc_addButton.gridx = 1;
        gbc_addButton.gridy = 4;
        add(addButton, gbc_addButton);
        
        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 4;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 5;
        add(scrollPane, gbc_scrollPane);
        
        doctorListModel = new DefaultListModel<>();
        doctorList = new JList<>(doctorListModel);
        scrollPane.setViewportView(doctorList);
        doctorList.setName("doctorList");
        
        JSeparator separator = new JSeparator();
        GridBagConstraints gbc_separator = new GridBagConstraints();
        gbc_separator.gridwidth = 4;
        gbc_separator.insets = new Insets(0, 0, 5, 0);
        gbc_separator.gridx = 1;
        gbc_separator.gridy = 6;
        add(separator, gbc_separator);
        
        JLabel selectedDoctorLabel = new JLabel("Selected Doctor");
        selectedDoctorLabel.setName("selectedDoctorLabel");
        GridBagConstraints gbc_selectedDoctorLabel = new GridBagConstraints();
        gbc_selectedDoctorLabel.gridwidth = 2;
        gbc_selectedDoctorLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedDoctorLabel.gridx = 1;
        gbc_selectedDoctorLabel.gridy = 7;
        add(selectedDoctorLabel, gbc_selectedDoctorLabel);
        
        JCheckBox editDoctor = new JCheckBox("Edit");
        editDoctor.setName("editDoctor");
        editDoctor.setEnabled(false);
        GridBagConstraints gbc_editDoctor = new GridBagConstraints();
        gbc_editDoctor.gridwidth = 2;
        gbc_editDoctor.insets = new Insets(0, 0, 5, 0);
        gbc_editDoctor.gridx = 3;
        gbc_editDoctor.gridy = 7;
        add(editDoctor, gbc_editDoctor);
        
        JLabel selectedIdLabel = new JLabel("Id");
        selectedIdLabel.setName("selectedIdLabel");
        GridBagConstraints gbc_selectedIdLabel = new GridBagConstraints();
        gbc_selectedIdLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedIdLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedIdLabel.gridx = 1;
        gbc_selectedIdLabel.gridy = 8;
        add(selectedIdLabel, gbc_selectedIdLabel);
        
        selectedIdTextBox = new JTextField();
        selectedIdTextBox.setEditable(false);
        selectedIdTextBox.setName("selectedIdTextBox");
        GridBagConstraints gbc_selectedIdTextBox = new GridBagConstraints();
        gbc_selectedIdTextBox.gridwidth = 3;
        gbc_selectedIdTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_selectedIdTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectedIdTextBox.gridx = 2;
        gbc_selectedIdTextBox.gridy = 8;
        add(selectedIdTextBox, gbc_selectedIdTextBox);
        selectedIdTextBox.setColumns(10);
        
        JLabel selectedFirstNameLabel = new JLabel("First Name");
        selectedFirstNameLabel.setName("selectedFirstNameLabel");
        GridBagConstraints gbc_selectedFirstNameLabel = new GridBagConstraints();
        gbc_selectedFirstNameLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedFirstNameLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedFirstNameLabel.gridx = 1;
        gbc_selectedFirstNameLabel.gridy = 9;
        add(selectedFirstNameLabel, gbc_selectedFirstNameLabel);
        
        selectedFirstNameTextBox = new JTextField();
        selectedFirstNameTextBox.setEditable(false);
        selectedFirstNameTextBox.setName("selectedFirstNameTextBox");
        GridBagConstraints gbc_selectedFirstNameTextBox = new GridBagConstraints();
        gbc_selectedFirstNameTextBox.gridwidth = 3;
        gbc_selectedFirstNameTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_selectedFirstNameTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectedFirstNameTextBox.gridx = 2;
        gbc_selectedFirstNameTextBox.gridy = 9;
        add(selectedFirstNameTextBox, gbc_selectedFirstNameTextBox);
        selectedFirstNameTextBox.setColumns(10);
        
        JLabel selectedLastNameLabel = new JLabel("Last Name");
        selectedLastNameLabel.setName("selectedLastNameLabel");
        GridBagConstraints gbc_selectedLastNameLabel = new GridBagConstraints();
        gbc_selectedLastNameLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedLastNameLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedLastNameLabel.gridx = 1;
        gbc_selectedLastNameLabel.gridy = 10;
        add(selectedLastNameLabel, gbc_selectedLastNameLabel);
        
        selectedLastNameTextBox = new JTextField();
        selectedLastNameTextBox.setEditable(false);
        selectedLastNameTextBox.setName("selectedLastNameTextBox");
        GridBagConstraints gbc_selectedLastNameTextBox = new GridBagConstraints();
        gbc_selectedLastNameTextBox.gridwidth = 3;
        gbc_selectedLastNameTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_selectedLastNameTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectedLastNameTextBox.gridx = 2;
        gbc_selectedLastNameTextBox.gridy = 10;
        add(selectedLastNameTextBox, gbc_selectedLastNameTextBox);
        selectedLastNameTextBox.setColumns(10);
        
        JButton deleteButton = new JButton("Delete selected");
        deleteButton.setEnabled(false);
        deleteButton.setName("deleteButton");
        GridBagConstraints gbc_deleteButton = new GridBagConstraints();
        gbc_deleteButton.gridwidth = 2;
        gbc_deleteButton.insets = new Insets(0, 0, 5, 5);
        gbc_deleteButton.gridx = 1;
        gbc_deleteButton.gridy = 11;
        add(deleteButton, gbc_deleteButton);
        
        JButton updateButton = new JButton("Update selected");
        updateButton.setEnabled(false);
        updateButton.setName("updateButton");
        GridBagConstraints gbc_updateButton = new GridBagConstraints();
        gbc_updateButton.gridwidth = 2;
        gbc_updateButton.insets = new Insets(0, 0, 5, 0);
        gbc_updateButton.gridx = 3;
        gbc_updateButton.gridy = 11;
        add(updateButton, gbc_updateButton);
        
        infoLabel = new JLabel(" ");
        infoLabel.setName("infoLabel");
        GridBagConstraints gbc_infoLabel = new GridBagConstraints();
        gbc_infoLabel.gridwidth = 4;
        gbc_infoLabel.insets = new Insets(0, 0, 5, 0);
        gbc_infoLabel.gridx = 1;
        gbc_infoLabel.gridy = 12;
        add(infoLabel, gbc_infoLabel);
        
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(237, 51, 59));
        errorLabel.setName("errorLabel");
        GridBagConstraints gbc_errorLabel = new GridBagConstraints();
        gbc_errorLabel.gridwidth = 4;
        gbc_errorLabel.insets = new Insets(0, 0, 5, 0);
        gbc_errorLabel.gridx = 1;
        gbc_errorLabel.gridy = 13;
        add(errorLabel, gbc_errorLabel);
        
        KeyAdapter addButtonEnabler = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                addButton.setEnabled(
                    !idTextBox.getText().isBlank() &&
                    !firstNameTextBox.getText().isBlank() &&
                    !lastNameTextBox.getText().isBlank());
            }
        };
        
        idTextBox.addKeyListener(addButtonEnabler);
        firstNameTextBox.addKeyListener(addButtonEnabler);
        lastNameTextBox.addKeyListener(addButtonEnabler);
        
        addButton.addActionListener(e -> {
            idTextBox.setText("");
            firstNameTextBox.setText("");
            lastNameTextBox.setText("");
            addButton.setEnabled(false);
            showInfoMessage("Adding Doctor...");
        });
        
        doctorList.addListSelectionListener(e -> {
            boolean isDoctorSelected = !doctorList.isSelectionEmpty();
        
            if (isDoctorSelected) {
                deleteButton.setEnabled(true);
                editDoctor.setEnabled(true);
                editDoctor.setSelected(false);
                
                Doctor d = doctorList.getSelectedValue();
                
                selectedIdTextBox.setText(d.getId().getValue());
                selectedFirstNameTextBox.setText(d.getFirstName());
                selectedLastNameTextBox.setText(d.getLastName());
            } else {
                deleteButton.setEnabled(false);
                editDoctor.setEnabled(false);
            }
        });
        
        deleteButton.addActionListener(e -> {
            doctorList.clearSelection();
            
            selectedIdTextBox.setText("");
            selectedFirstNameTextBox.setText("");
            selectedLastNameTextBox.setText("");
            
            editDoctor.setEnabled(false);
            showInfoMessage("Deleting Doctor...");
        });
        
        editDoctor.addChangeListener(e -> {
            boolean ticked = editDoctor.isSelected();
            deleteButton.setEnabled(!ticked);
            
            selectedFirstNameTextBox.setEditable(ticked);
            selectedLastNameTextBox.setEditable(ticked);
            
            if (!ticked) updateButton.setEnabled(false);
        });
        
        KeyAdapter updateButtonEnabler = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                Doctor d = doctorList.getSelectedValue();
                
                String fnText = selectedFirstNameTextBox.getText();
                String lnText = selectedLastNameTextBox.getText();
                
                updateButton.setEnabled(
                    !fnText.isBlank() && !lnText.isBlank() &&
                    !(fnText.equals(d.getFirstName()) && lnText.equals(d.getLastName())));
            }
        };
        
        selectedFirstNameTextBox.addKeyListener(updateButtonEnabler);
        selectedLastNameTextBox.addKeyListener(updateButtonEnabler);
        
        updateButton.addActionListener(e -> {
            doctorList.clearSelection();
            
            editDoctor.setEnabled(false);
            showInfoMessage("Updating Doctor...");
        });
    }
    
    public void setPresenter(DoctorPresenter presenter) { this.presenter = presenter; }
    
    DefaultListModel<Doctor> getDoctorListModel() { return doctorListModel; }
    
    private void showInfoMessage(String message) { infoLabel.setText(message); }
    
    private void clearErrorLabel() { errorLabel.setText(" "); }
    
    @Override
    public void showAllDoctors(List<Doctor> doctors) {
        doctors.forEach(doctorListModel::addElement);
    }
    
    @Override
    public void doctorAdded(Doctor doctor) {
        doctorListModel.addElement(doctor);
        infoLabel.setText("Doctor added!");
        clearErrorLabel();
    }
    
    @Override
    public void doctorRemoved(Doctor doctor) {
        doctorListModel.removeElement(doctor);
        infoLabel.setText("Doctor removed!");
        clearErrorLabel();
    }
    
    @Override
    public void doctorUpdated(Doctor oldDoctor, Doctor newDoctor) {
        int oldDoctorIndex = doctorListModel.indexOf(oldDoctor);
        doctorListModel.setElementAt(newDoctor, oldDoctorIndex);
        infoLabel.setText("Doctor updated!");
        clearErrorLabel();
    }
    
    @Override
    public void showErrorDuplicateDoctor(Id duplicated) {
        errorLabel.setText("A Doctor with id " + duplicated.getValue() + " already exists");
    }
    
    @Override
    public void showErrorDoctorNotFound(Id notFound) {
        errorLabel.setText("No Doctor with id " + notFound.getValue() + " was found");
    }
    
}