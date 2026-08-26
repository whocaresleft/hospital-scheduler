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

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import javax.swing.JCheckBox;

public class SwingDoctorView extends JPanel implements DoctorView {
    
    private static final long serialVersionUID = 1L;
    private JTextField idTextBox;
    private JTextField fnTextBox;
    private JTextField lnTextBox;
    private JTextField selectedIdTextBox;
    private JTextField selectedFnTextBox;
    private JTextField selectedLnTextBox;
    private JList<Doctor> doctorList;
    private DefaultListModel<Doctor> doctorListModel;
    private JLabel errorLabel;
    
    private DoctorPresenter presenter;
    
    public SwingDoctorView() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JLabel idLabel = new JLabel("Id");
        idLabel.setName("idLbl");
        GridBagConstraints gbc_idLabel = new GridBagConstraints();
        gbc_idLabel.insets = new Insets(0, 0, 5, 5);
        gbc_idLabel.anchor = GridBagConstraints.EAST;
        gbc_idLabel.gridx = 1;
        gbc_idLabel.gridy = 1;
        add(idLabel, gbc_idLabel);
        
        idTextBox = new JTextField();
        idTextBox.setName("idTextBox");
        GridBagConstraints gbc_idTextBox = new GridBagConstraints();
        gbc_idTextBox.gridwidth = 7;
        gbc_idTextBox.insets = new Insets(0, 0, 5, 5);
        gbc_idTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_idTextBox.gridx = 2;
        gbc_idTextBox.gridy = 1;
        add(idTextBox, gbc_idTextBox);
        idTextBox.setColumns(10);
        
        JLabel fnLabel = new JLabel("First Name");
        fnLabel.setName("firstNameLbl");
        GridBagConstraints gbc_fnLabel = new GridBagConstraints();
        gbc_fnLabel.anchor = GridBagConstraints.EAST;
        gbc_fnLabel.insets = new Insets(0, 0, 5, 5);
        gbc_fnLabel.gridx = 1;
        gbc_fnLabel.gridy = 2;
        add(fnLabel, gbc_fnLabel);
        
        fnTextBox = new JTextField();
        fnTextBox.setName("firstNameTextBox");
        GridBagConstraints gbc_fnTextBox = new GridBagConstraints();
        gbc_fnTextBox.gridwidth = 7;
        gbc_fnTextBox.insets = new Insets(0, 0, 5, 5);
        gbc_fnTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_fnTextBox.gridx = 2;
        gbc_fnTextBox.gridy = 2;
        add(fnTextBox, gbc_fnTextBox);
        fnTextBox.setColumns(10);
        
        JLabel lnLabel = new JLabel("Last Name");
        lnLabel.setName("lastNameLbl");
        GridBagConstraints gbc_lnLabel = new GridBagConstraints();
        gbc_lnLabel.anchor = GridBagConstraints.EAST;
        gbc_lnLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lnLabel.gridx = 1;
        gbc_lnLabel.gridy = 3;
        add(lnLabel, gbc_lnLabel);
        
        lnTextBox = new JTextField();
        lnTextBox.setName("lastNameTextBox");
        GridBagConstraints gbc_lnTextBox = new GridBagConstraints();
        gbc_lnTextBox.gridwidth = 7;
        gbc_lnTextBox.insets = new Insets(0, 0, 5, 5);
        gbc_lnTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_lnTextBox.gridx = 2;
        gbc_lnTextBox.gridy = 3;
        add(lnTextBox, gbc_lnTextBox);
        lnTextBox.setColumns(10);
        
        JButton addBtn = new JButton("Add");
        addBtn.setName("addBtn");
        addBtn.setEnabled(false);
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            }
        });
        
        KeyAdapter addBtnEnabler = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                addBtn.setEnabled(
                    !idTextBox.getText().isBlank()
                    && !fnTextBox.getText().isBlank()
                    && !lnTextBox.getText().isBlank());
            }
        };
        
        idTextBox.addKeyListener(addBtnEnabler);
        fnTextBox.addKeyListener(addBtnEnabler);
        lnTextBox.addKeyListener(addBtnEnabler);
        
        GridBagConstraints gbc_addBtn = new GridBagConstraints();
        gbc_addBtn.gridwidth = 4;
        gbc_addBtn.insets = new Insets(0, 0, 5, 5);
        gbc_addBtn.gridx = 2;
        gbc_addBtn.gridy = 4;
        add(addBtn, gbc_addBtn);
        
        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridheight = 3;
        gbc_scrollPane.gridwidth = 8;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 1;
        gbc_scrollPane.gridy = 5;
        add(scrollPane, gbc_scrollPane);
        
        doctorListModel = new DefaultListModel<>();
        doctorList = new JList<>(doctorListModel);
        doctorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorList.setName("doctorList");
        scrollPane.setViewportView(doctorList);
        
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.setName("deleteBtn");
        deleteBtn.setEnabled(false);
        GridBagConstraints gbc_deleteBtn = new GridBagConstraints();
        gbc_deleteBtn.gridwidth = 5;
        gbc_deleteBtn.insets = new Insets(0, 0, 5, 5);
        gbc_deleteBtn.gridx = 2;
        gbc_deleteBtn.gridy = 8;
        add(deleteBtn, gbc_deleteBtn);
        
        JLabel selectedDoctor = new JLabel("Selected Doctor");
        selectedDoctor.setName("selectedDoctor");
        GridBagConstraints gbc_selectedDoctor = new GridBagConstraints();
        gbc_selectedDoctor.gridwidth = 3;
        gbc_selectedDoctor.insets = new Insets(0, 0, 5, 5);
        gbc_selectedDoctor.gridx = 1;
        gbc_selectedDoctor.gridy = 9;
        add(selectedDoctor, gbc_selectedDoctor);
        
        JCheckBox editDoctorCheckbox = new JCheckBox("Edit");
        editDoctorCheckbox.setName("editDoctor");
        editDoctorCheckbox.setEnabled(false);
        GridBagConstraints gbc_editDoctorCheckbox = new GridBagConstraints();
        gbc_editDoctorCheckbox.gridwidth = 3;
        gbc_editDoctorCheckbox.insets = new Insets(0, 0, 5, 5);
        gbc_editDoctorCheckbox.gridx = 5;
        gbc_editDoctorCheckbox.gridy = 9;
        add(editDoctorCheckbox, gbc_editDoctorCheckbox);
        
        JLabel selectedIdLabel = new JLabel("Id");
        selectedIdLabel.setName("selectedIdLbl");
        GridBagConstraints gbc_selectedIdLabel = new GridBagConstraints();
        gbc_selectedIdLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedIdLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedIdLabel.gridx = 1;
        gbc_selectedIdLabel.gridy = 10;
        add(selectedIdLabel, gbc_selectedIdLabel);
        
        selectedIdTextBox = new JTextField();
        selectedIdTextBox.setEnabled(false);
        selectedIdTextBox.setName("selectedIdTextBox");
        GridBagConstraints gbc_selectedIdTextBox = new GridBagConstraints();
        gbc_selectedIdTextBox.gridwidth = 7;
        gbc_selectedIdTextBox.insets = new Insets(0, 0, 5, 5);
        gbc_selectedIdTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectedIdTextBox.gridx = 2;
        gbc_selectedIdTextBox.gridy = 10;
        add(selectedIdTextBox, gbc_selectedIdTextBox);
        selectedIdTextBox.setColumns(10);
        
        JLabel selectedFnLabel = new JLabel("First Name");
        selectedFnLabel.setName("selectedFirstNameLbl");
        GridBagConstraints gbc_selectedFnLabel = new GridBagConstraints();
        gbc_selectedFnLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedFnLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedFnLabel.gridx = 1;
        gbc_selectedFnLabel.gridy = 11;
        add(selectedFnLabel, gbc_selectedFnLabel);
        
        selectedFnTextBox = new JTextField();
        selectedFnTextBox.setEnabled(false);
        selectedFnTextBox.setName("selectedFirstNameTextBox");
        GridBagConstraints gbc_selectedFnTextBox = new GridBagConstraints();
        gbc_selectedFnTextBox.gridwidth = 7;
        gbc_selectedFnTextBox.insets = new Insets(0, 0, 5, 5);
        gbc_selectedFnTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectedFnTextBox.gridx = 2;
        gbc_selectedFnTextBox.gridy = 11;
        add(selectedFnTextBox, gbc_selectedFnTextBox);
        selectedFnTextBox.setColumns(10);
        
        JLabel selectedLnLabel = new JLabel("Last Name");
        selectedLnLabel.setName("selectedLastNameLbl");
        GridBagConstraints gbc_selectedLnLabel = new GridBagConstraints();
        gbc_selectedLnLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedLnLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedLnLabel.gridx = 1;
        gbc_selectedLnLabel.gridy = 12;
        add(selectedLnLabel, gbc_selectedLnLabel);
        gbc_lnTextBox.gridwidth = 6;
        gbc_lnTextBox.insets = new Insets(0, 0, 5, 5);
        gbc_lnTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_lnTextBox.gridx = 2;
        gbc_lnTextBox.gridy = 10;
        
        selectedLnTextBox = new JTextField();
        selectedLnTextBox.setEnabled(false);
        selectedLnTextBox.setName("selectedLastNameTextBox");
        GridBagConstraints gbc_selectedLnTextBox = new GridBagConstraints();
        gbc_selectedLnTextBox.gridwidth = 7;
        gbc_selectedLnTextBox.insets = new Insets(0, 0, 5, 5);
        gbc_selectedLnTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectedLnTextBox.gridx = 2;
        gbc_selectedLnTextBox.gridy = 12;
        add(selectedLnTextBox, gbc_selectedLnTextBox);
        selectedLnTextBox.setColumns(10);
        
        doctorList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                boolean isDoctorSelected = !doctorList.isSelectionEmpty();

                deleteBtn.setEnabled(isDoctorSelected);
                editDoctorCheckbox.setEnabled(isDoctorSelected);
                
                if (!isDoctorSelected) {
                    editDoctorCheckbox.setSelected(false);

                    selectedIdTextBox.setText("");
                    selectedFnTextBox.setText("");
                    selectedLnTextBox.setText("");
                } else {
                    
                    Doctor d = doctorList.getSelectedValue();
                    
                    selectedIdTextBox.setText(d.getId().getValue());
                    selectedFnTextBox.setText(d.getFirstName());
                    selectedLnTextBox.setText(d.getLastName());
                }
            }
        });
        
        editDoctorCheckbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                boolean isTicked = editDoctorCheckbox.isSelected();
                
                selectedFnTextBox.setEnabled(isTicked);
                selectedLnTextBox.setEnabled(isTicked);
            }
        });
        
        JButton updateBtn = new JButton("Update Selected");
        updateBtn.setName("updateBtn");
        updateBtn.setEnabled(false);
        GridBagConstraints gbc_updateBtn = new GridBagConstraints();
        gbc_updateBtn.insets = new Insets(0, 0, 5, 5);
        gbc_updateBtn.gridx = 5;
        gbc_updateBtn.gridy = 13;
        add(updateBtn, gbc_updateBtn);
        
        KeyAdapter updateBtnEnabler = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                Doctor selected = doctorList.getSelectedValue();
                
                boolean bothEqualToExisting = selectedFnTextBox.getText().equals(selected.getFirstName()) && selectedLnTextBox.getText().equals(selected.getLastName());
                boolean atLeastOneEmpty = selectedFnTextBox.getText().isBlank() || selectedLnTextBox.getText().isBlank();
                
                updateBtn.setEnabled(!(bothEqualToExisting || atLeastOneEmpty));
            }
        };
        selectedFnTextBox.addKeyListener(updateBtnEnabler);
        selectedLnTextBox.addKeyListener(updateBtnEnabler);
        
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(237, 51, 59));
        errorLabel.setName("errorLabel");
        GridBagConstraints gbc_errorLabel = new GridBagConstraints();
        gbc_errorLabel.gridwidth = 8;
        gbc_errorLabel.insets = new Insets(0, 0, 5, 5);
        gbc_errorLabel.gridx = 1;
        gbc_errorLabel.gridy = 17;
        add(errorLabel, gbc_errorLabel);
        
        addBtn.addActionListener(
            e -> {
                try {
                    presenter.addDoctor(Doctor.createDoctor(
                        Id.createId(idTextBox.getText()),
                        fnTextBox.getText(),
                        lnTextBox.getText()));
                } catch (IllegalArgumentException iae) {
                    idTextBox.setText("");
                    fnTextBox.setText("");
                    lnTextBox.setText("");
                    errorLabel.setText("Invalid id, must be [\\w]+");
                }
            });
        
        deleteBtn.addActionListener(
            e -> presenter.removeDoctor(
                doctorList.getSelectedValue()));
        
        updateBtn.addActionListener(
            e -> presenter.updateDoctor(
                doctorList.getSelectedValue(),
                Doctor.createDoctor(
                    Id.createId(selectedIdTextBox.getText()),
                    selectedFnTextBox.getText(),
                    selectedLnTextBox.getText()
                )));
    }
    
    DefaultListModel<Doctor> getDoctorListModel() { return doctorListModel; }
    
    @Override
    public void showAllDoctors(List<Doctor> doctors) {
        doctors.stream().forEach(doctorListModel::addElement);
    }

    @Override
    public void doctorAdded(Doctor doctor) {
        doctorListModel.addElement(doctor);
        clearErrorLabel();
    }

    @Override
    public void doctorRemoved(Doctor doctor) {
        doctorListModel.removeElement(doctor);
        clearErrorLabel();
    }

    @Override
    public void doctorUpdated(Doctor oldDoctor, Doctor newDoctor) {
        int index = doctorListModel.indexOf(oldDoctor);
        doctorListModel.setElementAt(newDoctor, index);
    }

    @Override
    public void showErrorDuplicateDoctor(Id duplicated) {
        errorLabel.setText("There already is a Doctor with id " + duplicated);
    }
    
    @Override
    public void showErrorDoctorNotFound(Id notFound) {
        errorLabel.setText("No existing doctor with id " + notFound);
    }
    
    private void clearErrorLabel() {
        errorLabel.setText(" ");
    }

    public void setPresenter(DoctorPresenter presenter) {
        this.presenter = presenter;
    }
}
