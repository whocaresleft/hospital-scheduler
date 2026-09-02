package org.duckdns.whocaresleft.view.swing;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;

import org.duckdns.whocaresleft.core.Id;
import org.duckdns.whocaresleft.model.Shift;
import org.duckdns.whocaresleft.presenter.ShiftPresenter;
import org.duckdns.whocaresleft.view.ShiftView;

import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.github.lgooddatepicker.components.TimePickerSettings.TimeIncrement;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

public class SwingShiftView extends JPanel implements ShiftView {
    
    private static final long serialVersionUID = 1L;
    
    private JTextField doctorIdTextBox;
    private JTextField departmentIdTextBox;
    private DatePicker datePicker;
    private TimePicker startTimePicker;
    private TimePicker endTimePicker;
    private JButton addButton;
    private DefaultListModel<Shift> shiftListModel;
    private JList<Shift> shiftList;
    private JCheckBox editShift;
    private JTextField selectedDoctorIdTextBox;
    private JTextField selectedDepartmentIdTextBox;
    private DatePicker selectedDatePicker;
    private TimePicker selectedStartTimePicker;
    private TimePicker selectedEndTimePicker;
    private JButton deleteButton;
    private JButton updateButton;
    private JLabel infoLabel;
    private JLabel errorLabel;
    
    private transient ShiftPresenter presenter;
    
    public SwingShiftView() {
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 1.0};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        JLabel addShiftLabel = new JLabel("Shift creation");
        addShiftLabel.setName("shiftCreation");
        GridBagConstraints gbc_addShiftLabel = new GridBagConstraints();
        gbc_addShiftLabel.gridwidth = 2;
        gbc_addShiftLabel.insets = new Insets(0, 0, 5, 0);
        gbc_addShiftLabel.gridx = 0;
        gbc_addShiftLabel.gridy = 0;
        add(addShiftLabel, gbc_addShiftLabel);
        
        JLabel doctorIdLabel = new JLabel("Doctor Id");
        doctorIdLabel.setName("doctorIdLabel");
        GridBagConstraints gbc_doctorIdLabel = new GridBagConstraints();
        gbc_doctorIdLabel.insets = new Insets(0, 0, 5, 5);
        gbc_doctorIdLabel.anchor = GridBagConstraints.EAST;
        gbc_doctorIdLabel.gridx = 0;
        gbc_doctorIdLabel.gridy = 1;
        add(doctorIdLabel, gbc_doctorIdLabel);
        
        doctorIdTextBox = new JTextField();
        doctorIdTextBox.setName("doctorIdTextBox");
        doctorIdTextBox.setEditable(false);
        GridBagConstraints gbc_doctorIdTextBox = new GridBagConstraints();
        gbc_doctorIdTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_doctorIdTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_doctorIdTextBox.gridx = 1;
        gbc_doctorIdTextBox.gridy = 1;
        add(doctorIdTextBox, gbc_doctorIdTextBox);
        doctorIdTextBox.setColumns(10);
        
        JLabel departmentIdLabel = new JLabel("Department Id");
        departmentIdLabel.setName("departmentIdLabel");
        GridBagConstraints gbc_departmentIdLabel = new GridBagConstraints();
        gbc_departmentIdLabel.anchor = GridBagConstraints.EAST;
        gbc_departmentIdLabel.insets = new Insets(0, 0, 5, 5);
        gbc_departmentIdLabel.gridx = 0;
        gbc_departmentIdLabel.gridy = 2;
        add(departmentIdLabel, gbc_departmentIdLabel);
        
        departmentIdTextBox = new JTextField();
        departmentIdTextBox.setName("departmentIdTextBox");
        departmentIdTextBox.setEditable(false);
        GridBagConstraints gbc_departmentIdTextBox = new GridBagConstraints();
        gbc_departmentIdTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_departmentIdTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_departmentIdTextBox.gridx = 1;
        gbc_departmentIdTextBox.gridy = 2;
        add(departmentIdTextBox, gbc_departmentIdTextBox);
        departmentIdTextBox.setColumns(10);
        
        JLabel dateLabel = new JLabel("Date");
        dateLabel.setName("dateLabel");
        GridBagConstraints gbc_dateLabel = new GridBagConstraints();
        gbc_dateLabel.anchor = GridBagConstraints.EAST;
        gbc_dateLabel.insets = new Insets(0, 0, 5, 5);
        gbc_dateLabel.gridx = 0;
        gbc_dateLabel.gridy = 3;
        add(dateLabel, gbc_dateLabel);
        
        JLabel startTimeLabel = new JLabel("Start Time");
        startTimeLabel.setName("startTimeLabel");
        GridBagConstraints gbc_startTimeLabel = new GridBagConstraints();
        gbc_startTimeLabel.anchor = GridBagConstraints.EAST;
        gbc_startTimeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_startTimeLabel.gridx = 0;
        gbc_startTimeLabel.gridy = 4;
        add(startTimeLabel, gbc_startTimeLabel);
        
        JLabel endTimeLabel = new JLabel("End Time");
        endTimeLabel.setName("endTimeLabel");
        GridBagConstraints gbc_endTimeLabel = new GridBagConstraints();
        gbc_endTimeLabel.anchor = GridBagConstraints.EAST;
        gbc_endTimeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_endTimeLabel.gridx = 0;
        gbc_endTimeLabel.gridy = 5;
        add(endTimeLabel, gbc_endTimeLabel);
        
        datePicker = new DatePicker(customDateFormat());
        datePicker.setEnabled(false);
        datePicker.getComponentDateTextField().setName("dateTextBox");
        datePicker.getComponentToggleCalendarButton().setName("dateButton");
        GridBagConstraints gbc_datePicker = new GridBagConstraints();
        gbc_datePicker.insets = new Insets(0, 0, 5, 0);
        gbc_datePicker.fill = GridBagConstraints.BOTH;
        gbc_datePicker.gridx = 1;
        gbc_datePicker.gridy = 3;
        add(datePicker, gbc_datePicker);
        
        startTimePicker = new TimePicker(customTimeFormat());
        startTimePicker.setEnabled(false);
        startTimePicker.getComponentTimeTextField().setName("startTimeTextBox");
        startTimePicker.getComponentToggleTimeMenuButton().setName("startTimeButton");
        GridBagConstraints gbc_startTimePicker = new GridBagConstraints();
        gbc_startTimePicker.insets = new Insets(0, 0, 5, 0);
        gbc_startTimePicker.fill = GridBagConstraints.BOTH;
        gbc_startTimePicker.gridx = 1;
        gbc_startTimePicker.gridy = 4;
        add(startTimePicker, gbc_startTimePicker);
        
        endTimePicker = new TimePicker(customTimeFormat());
        endTimePicker.setEnabled(false);
        endTimePicker.getComponentTimeTextField().setName("endTimeTextBox");
        endTimePicker.getComponentToggleTimeMenuButton().setName("endTimeButton");
        GridBagConstraints gbc_endTimePicker = new GridBagConstraints();
        gbc_endTimePicker.insets = new Insets(0, 0, 5, 0);
        gbc_endTimePicker.fill = GridBagConstraints.BOTH;
        gbc_endTimePicker.gridx = 1;
        gbc_endTimePicker.gridy = 5;
        add(endTimePicker, gbc_endTimePicker);
        
        addButton = new JButton("Add");
        addButton.setName("addButton");
        addButton.setEnabled(false);
        GridBagConstraints gbc_addButton = new GridBagConstraints();
        gbc_addButton.gridwidth = 2;
        gbc_addButton.insets = new Insets(0, 0, 5, 0);
        gbc_addButton.gridx = 0;
        gbc_addButton.gridy = 6;
        add(addButton, gbc_addButton);
        
        JScrollPane scrollPane = new JScrollPane();
        GridBagConstraints gbc_scrollPane = new GridBagConstraints();
        gbc_scrollPane.gridwidth = 2;
        gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_scrollPane.fill = GridBagConstraints.BOTH;
        gbc_scrollPane.gridx = 0;
        gbc_scrollPane.gridy = 7;
        add(scrollPane, gbc_scrollPane);
        
        shiftListModel = new DefaultListModel<>();
        shiftList = new JList<>(shiftListModel);
        shiftList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        shiftList.setEnabled(false);
        scrollPane.setViewportView(shiftList);
        shiftList.setName("shiftList");
        
        JLabel selectedShiftLabel = new JLabel("Selected Shift");
        selectedShiftLabel.setName("selectedShiftLabel");
        GridBagConstraints gbc_selectedShiftLabel = new GridBagConstraints();
        gbc_selectedShiftLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedShiftLabel.gridx = 0;
        gbc_selectedShiftLabel.gridy = 8;
        add(selectedShiftLabel, gbc_selectedShiftLabel);
        
        editShift = new JCheckBox("Edit");
        editShift.setName("editShift");
        editShift.setEnabled(false);
        GridBagConstraints gbc_editShift = new GridBagConstraints();
        gbc_editShift.insets = new Insets(0, 0, 5, 0);
        gbc_editShift.gridx = 1;
        gbc_editShift.gridy = 8;
        add(editShift, gbc_editShift);
        
        JLabel selectedDoctorIdLabel = new JLabel("Doctor Id");
        selectedDoctorIdLabel.setName("selectedDoctorIdLabel");
        GridBagConstraints gbc_selectedDoctorIdLabel = new GridBagConstraints();
        gbc_selectedDoctorIdLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedDoctorIdLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedDoctorIdLabel.gridx = 0;
        gbc_selectedDoctorIdLabel.gridy = 9;
        add(selectedDoctorIdLabel, gbc_selectedDoctorIdLabel);
        
        selectedDoctorIdTextBox = new JTextField();
        selectedDoctorIdTextBox.setName("selectedDoctorIdTextBox");
        selectedDoctorIdTextBox.setEditable(false);
        GridBagConstraints gbc_selectedDoctorIdTextBox = new GridBagConstraints();
        gbc_selectedDoctorIdTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_selectedDoctorIdTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectedDoctorIdTextBox.gridx = 1;
        gbc_selectedDoctorIdTextBox.gridy = 9;
        add(selectedDoctorIdTextBox, gbc_selectedDoctorIdTextBox);
        selectedDoctorIdTextBox.setColumns(10);
        
        JLabel selectedDepartmentIdLabel = new JLabel("Department Id");
        selectedDepartmentIdLabel.setName("selectedDepartmentIdLabel");
        GridBagConstraints gbc_selectedDepartmentIdLabel = new GridBagConstraints();
        gbc_selectedDepartmentIdLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedDepartmentIdLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedDepartmentIdLabel.gridx = 0;
        gbc_selectedDepartmentIdLabel.gridy = 10;
        add(selectedDepartmentIdLabel, gbc_selectedDepartmentIdLabel);
        
        selectedDepartmentIdTextBox = new JTextField();
        selectedDepartmentIdTextBox.setName("selectedDepartmentIdTextBox");
        selectedDepartmentIdTextBox.setEditable(false);
        GridBagConstraints gbc_selectedDepartmentIdTextBox = new GridBagConstraints();
        gbc_selectedDepartmentIdTextBox.insets = new Insets(0, 0, 5, 0);
        gbc_selectedDepartmentIdTextBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_selectedDepartmentIdTextBox.gridx = 1;
        gbc_selectedDepartmentIdTextBox.gridy = 10;
        add(selectedDepartmentIdTextBox, gbc_selectedDepartmentIdTextBox);
        selectedDepartmentIdTextBox.setColumns(10);
        
        JLabel selectedDateLabel = new JLabel("Date");
        selectedDateLabel.setName("selectedDateLabel");
        GridBagConstraints gbc_selectedDateLabel = new GridBagConstraints();
        gbc_selectedDateLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedDateLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedDateLabel.gridx = 0;
        gbc_selectedDateLabel.gridy = 11;
        add(selectedDateLabel, gbc_selectedDateLabel);
        
        selectedDatePicker = new DatePicker(customDateFormat());
        selectedDatePicker.setEnabled(false);
        selectedDatePicker.getComponentDateTextField().setName("selectedDateTextBox");
        selectedDatePicker.getComponentToggleCalendarButton().setName("selectedDateButton");
        GridBagConstraints gbc_selectedDatePicker = new GridBagConstraints();
        gbc_selectedDatePicker.insets = new Insets(0, 0, 5, 0);
        gbc_selectedDatePicker.fill = GridBagConstraints.BOTH;
        gbc_selectedDatePicker.gridx = 1;
        gbc_selectedDatePicker.gridy = 11;
        add(selectedDatePicker, gbc_selectedDatePicker);
        
        JLabel selectedStartTimeLabel = new JLabel("Start Time");
        selectedStartTimeLabel.setName("selectedStartTimeLabel");
        GridBagConstraints gbc_selectedStartTimeLabel = new GridBagConstraints();
        gbc_selectedStartTimeLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedStartTimeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedStartTimeLabel.gridx = 0;
        gbc_selectedStartTimeLabel.gridy = 12;
        add(selectedStartTimeLabel, gbc_selectedStartTimeLabel);
        
        JLabel selectedEndTimeLabel = new JLabel("End Time");
        selectedEndTimeLabel.setName("selectedEndTimeLabel");
        GridBagConstraints gbc_selectedEndTimeLabel = new GridBagConstraints();
        gbc_selectedEndTimeLabel.anchor = GridBagConstraints.EAST;
        gbc_selectedEndTimeLabel.insets = new Insets(0, 0, 5, 5);
        gbc_selectedEndTimeLabel.gridx = 0;
        gbc_selectedEndTimeLabel.gridy = 13;
        add(selectedEndTimeLabel, gbc_selectedEndTimeLabel);
        
        selectedStartTimePicker = new TimePicker(customTimeFormat());
        selectedStartTimePicker.setEnabled(false);
        selectedStartTimePicker.getComponentTimeTextField().setName("selectedStartTimeTextBox");
        selectedStartTimePicker.getComponentToggleTimeMenuButton().setName("selectedStartTimeButton");
        GridBagConstraints gbc_selectedStartTimePicker = new GridBagConstraints();
        gbc_selectedStartTimePicker.insets = new Insets(0, 0, 5, 0);
        gbc_selectedStartTimePicker.fill = GridBagConstraints.BOTH;
        gbc_selectedStartTimePicker.gridx = 1;
        gbc_selectedStartTimePicker.gridy = 12;
        add(selectedStartTimePicker, gbc_selectedStartTimePicker);
        
        selectedEndTimePicker = new TimePicker(customTimeFormat());
        selectedEndTimePicker.setEnabled(false);
        selectedEndTimePicker.getComponentTimeTextField().setName("selectedEndTimeTextBox");
        selectedEndTimePicker.getComponentToggleTimeMenuButton().setName("selectedEndTimeButton");
        GridBagConstraints gbc_selectedEndTimePicker = new GridBagConstraints();
        gbc_selectedEndTimePicker.insets = new Insets(0, 0, 5, 0);
        gbc_selectedEndTimePicker.fill = GridBagConstraints.BOTH;
        gbc_selectedEndTimePicker.gridx = 1;
        gbc_selectedEndTimePicker.gridy = 13;
        add(selectedEndTimePicker, gbc_selectedEndTimePicker);
        
        deleteButton = new JButton("Delete selected");
        deleteButton.setName("deleteButton");
        deleteButton.setEnabled(false);
        GridBagConstraints gbc_deleteButton = new GridBagConstraints();
        gbc_deleteButton.insets = new Insets(0, 0, 5, 5);
        gbc_deleteButton.gridx = 0;
        gbc_deleteButton.gridy = 14;
        add(deleteButton, gbc_deleteButton);
        
        updateButton = new JButton("Update selected");
        updateButton.setName("updateButton");
        updateButton.setEnabled(false);
        GridBagConstraints gbc_updateButton = new GridBagConstraints();
        gbc_updateButton.insets = new Insets(0, 0, 5, 0);
        gbc_updateButton.gridx = 1;
        gbc_updateButton.gridy = 14;
        add(updateButton, gbc_updateButton);
        
        infoLabel = new JLabel(" ");
        infoLabel.setName("infoLabel");
        GridBagConstraints gbc_infoLabel = new GridBagConstraints();
        gbc_infoLabel.gridwidth = 2;
        gbc_infoLabel.insets = new Insets(0, 0, 5, 0);
        gbc_infoLabel.gridx = 0;
        gbc_infoLabel.gridy = 15;
        add(infoLabel, gbc_infoLabel);
        
        errorLabel = new JLabel(" ");
        errorLabel.setName("errorLabel");
        errorLabel.setForeground(new Color(237, 51, 59));
        GridBagConstraints gbc_errorLabel = new GridBagConstraints();
        gbc_errorLabel.gridwidth = 2;
        gbc_errorLabel.gridx = 0;
        gbc_errorLabel.gridy = 16;
        add(errorLabel, gbc_errorLabel);
        
        KeyAdapter manualWritingAddButtonEnabler = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { addButton.setEnabled(isAddPossible()); }
        };
        doctorIdTextBox.addKeyListener(manualWritingAddButtonEnabler);
        departmentIdTextBox.addKeyListener(manualWritingAddButtonEnabler);
        datePicker.addDateChangeListener(e -> addButton.setEnabled(isAddPossible()));
        startTimePicker.addTimeChangeListener(e -> addButton.setEnabled(isAddPossible()));
        endTimePicker.addTimeChangeListener(e -> addButton.setEnabled(isAddPossible()));
        
        addButton.addActionListener(e -> add());
        
        shiftList.addListSelectionListener(e -> {
            boolean isShiftSelected = !shiftList.isSelectionEmpty();
            
            deleteButton.setEnabled(isShiftSelected);
            editShift.setEnabled(isShiftSelected);
            
            if (isShiftSelected) {
                editShift.setSelected(false);
                
                Shift s = shiftList.getSelectedValue();
                
                selectedDoctorIdTextBox.setText(s.getDoctorId().getValue());
                selectedDepartmentIdTextBox.setText(s.getDepartmentId().getValue());
                selectedDatePicker.setDate(s.getDate());
                selectedStartTimePicker.setTime(s.getStartTime());
                selectedEndTimePicker.setTime(s.getEndTime());
            }
        });
        
        editShift.addActionListener(e -> {
            boolean isTicked = editShift.isSelected();
            deleteButton.setEnabled(!isTicked);
            
            selectedDoctorIdTextBox.setEditable(isTicked);
            selectedDepartmentIdTextBox.setEditable(isTicked);
            selectedDatePicker.setEnabled(isTicked);
            selectedStartTimePicker.setEnabled(isTicked);
            selectedEndTimePicker.setEnabled(isTicked);
            
            updateButton.setEnabled(isTicked && isUpdatePossible());
        });
        
        KeyAdapter manualWritingUpdateButtonEnabler = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { updateButton.setEnabled(isUpdatePossible()); }
        };
        selectedDoctorIdTextBox.addKeyListener(manualWritingUpdateButtonEnabler);
        selectedDepartmentIdTextBox.addKeyListener(manualWritingUpdateButtonEnabler);
        selectedDatePicker.addDateChangeListener(e -> updateButton.setEnabled(isUpdatePossible()));
        selectedStartTimePicker.addTimeChangeListener(e -> updateButton.setEnabled(isUpdatePossible()));
        selectedEndTimePicker.addTimeChangeListener(e -> updateButton.setEnabled(isUpdatePossible()));
        
        deleteButton.addActionListener(e -> delete());
        
        updateButton.addActionListener(e -> update());
    }
    
    public void setPresenter(ShiftPresenter presenter) { this.presenter = presenter; }
    
    public DefaultListModel<Shift> getShiftListModel() { return shiftListModel; }
    
    private void add() {
        Shift shift = createShift(
            doctorIdTextBox.getText(),
            departmentIdTextBox.getText(),
            datePicker.getDate(),
            startTimePicker.getTime(),
            endTimePicker.getTime());
        
        if (shift != null) {
            disableUI();
            new Thread(() -> presenter.addShift(shift)).start();
            
            doctorIdTextBox.setText("");
            departmentIdTextBox.setText("");
            datePicker.setDate(null);
            startTimePicker.setTime(null);
            endTimePicker.setTime(null);
            addButton.setEnabled(false);
            showInfoMessage("Adding Shift...");
        }
    }
    
    private void delete() {
        Shift shift = shiftList.getSelectedValue();
        
        disableUI();
        new Thread(() -> presenter.removeShift(shift)).start();
        
        shiftList.clearSelection();
        
        selectedDoctorIdTextBox.setText("");
        selectedDepartmentIdTextBox.setText("");
        selectedDatePicker.setDate(null);
        selectedStartTimePicker.setTime(null);
        selectedEndTimePicker.setTime(null);
        showInfoMessage("Deleting Shift...");
    }
    
    private void update() {
        Shift current = shiftList.getSelectedValue();
        Shift updated = createShift(
            selectedDoctorIdTextBox.getText(),
            selectedDepartmentIdTextBox.getText(),
            selectedDatePicker.getDate(),
            selectedStartTimePicker.getTime(),
            selectedEndTimePicker.getTime());
        
        if (updated != null) {
            editShift.setSelected(false);
            disableUI();
            new Thread(() -> presenter.updateShift(current, updated)).start();
            showInfoMessage("Updating Shift...");
        }
    }
    
    private Shift createShift(String doctorIdValue, String departmentIdValue, LocalDate date, LocalTime starTime, LocalTime endTime) {
        Id doctorId;
        Id departmentId;
        Shift shift = null;
        
        try {
            doctorId = Id.createId(doctorIdValue);
        } catch (IllegalArgumentException iae) {
            showErrorMessage("Doctor Id contains invalid value: Letters, digits, and underscores only");
            return null;
        }
        
        try {
            departmentId = Id.createId(departmentIdValue);
        } catch (IllegalArgumentException iae) {
            showErrorMessage("Department Id contains invalid value: Letters, digits, and underscores only");
            return null;
        }
        
        try {
            shift = Shift.createShift(doctorId, departmentId, date, starTime, endTime);
        } catch (IllegalArgumentException iae) {
            showErrorMessage(iae.getMessage());
        }
        
        return shift;
    }
    
    private boolean isAddPossible() {
        boolean isDoctorIdPresent = !doctorIdTextBox.getText().isBlank();
        boolean isDepartmentIdPresent = !departmentIdTextBox.getText().isBlank();
        boolean isDatePresent = datePicker.getDate() != null;
        boolean isStartTimePresent = startTimePicker.getTime() != null;
        boolean isEndTimePresent = endTimePicker.getTime() != null;
        
        return
            isDoctorIdPresent &&
            isDepartmentIdPresent &&
            isDatePresent &&
            isStartTimePresent &&
            isEndTimePresent;
    }
    
    private boolean isUpdatePossible() {
        Shift current = shiftList.getSelectedValue();
        
        String selectedDoctorId = selectedDoctorIdTextBox.getText();
        String selectedDepartmentId = selectedDepartmentIdTextBox.getText();
        LocalDate selectedDate = selectedDatePicker.getDate();
        LocalTime selectedStartTime = selectedStartTimePicker.getTime();
        LocalTime selectedEndTime = selectedEndTimePicker.getTime();
        
        return !selectedDoctorId.isBlank() &&
                !selectedDepartmentId.isBlank() &&
                selectedDate != null &&
                selectedStartTime != null &&
                selectedEndTime != null &&
                !(
                     selectedDoctorId.equals(current.getDoctorId().getValue()) &&
                     selectedDepartmentId.equals(current.getDepartmentId().getValue()) &&
                     selectedDate.equals(current.getDate()) &&
                     selectedStartTime.equals(current.getStartTime()) &&
                     selectedEndTime.equals(current.getEndTime())
                );
    }
    
    void showInfoMessage(String message) {
        infoLabel.setText(message);
        clearErrorLabel();
    }
    private void clearInfoLabel() { infoLabel.setText(" ");}
    
    void showErrorMessage(String message) {
        clearInfoLabel();
        errorLabel.setText(message);
    }
    private void clearErrorLabel() { errorLabel.setText(" "); }
    
    private void addToList(Shift toAdd) { shiftListModel.addElement(toAdd); }
    private void removeFromList(Shift toRemove) { shiftListModel.removeElement(toRemove); }
    
    void disableUI() {
        doctorIdTextBox.setEditable(false);
        departmentIdTextBox.setEditable(false);
        datePicker.setEnabled(false);
        startTimePicker.setEnabled(false);
        endTimePicker.setEnabled(false);
        addButton.setEnabled(false);
        shiftList.setEnabled(false);
        editShift.setEnabled(false);
        selectedDoctorIdTextBox.setEditable(false);
        selectedDepartmentIdTextBox.setEditable(false);
        selectedDatePicker.setEnabled(false);
        selectedStartTimePicker.setEnabled(false);
        selectedEndTimePicker.setEnabled(false);
        deleteButton.setEnabled(false);
        updateButton.setEnabled(false);
    }
    
    void enableUI() {
        doctorIdTextBox.setEditable(true);
        departmentIdTextBox.setEditable(true);
        datePicker.setEnabled(true);
        startTimePicker.setEnabled(true);
        endTimePicker.setEnabled(true);
        addButton.setEnabled(false);
        shiftList.clearSelection();
        shiftList.setEnabled(true);
        editShift.setEnabled(false);
        selectedDoctorIdTextBox.setEditable(false);
        selectedDepartmentIdTextBox.setEditable(false);
        selectedDatePicker.setEnabled(false);
        selectedStartTimePicker.setEnabled(false);
        selectedEndTimePicker.setEnabled(false);
        deleteButton.setEnabled(false);
        updateButton.setEnabled(false);
    }
    
    private void restoreUpdateUI() {
        doctorIdTextBox.setEditable(true);
        departmentIdTextBox.setEditable(true);
        datePicker.setEnabled(true);
        startTimePicker.setEnabled(true);
        endTimePicker.setEnabled(true);
        addButton.setEnabled(false);
        shiftList.setEnabled(true);
        editShift.setEnabled(true);
        editShift.setSelected(false);
        selectedDoctorIdTextBox.setEditable(false);
        selectedDepartmentIdTextBox.setEditable(false);
        selectedDatePicker.setEnabled(false);
        selectedStartTimePicker.setEnabled(false);
        selectedEndTimePicker.setEnabled(false);
        deleteButton.setEnabled(true);
        updateButton.setEnabled(false);
    }
    
    private static DatePickerSettings customDateFormat() {
        DatePickerSettings dateSettings = new DatePickerSettings(Locale.ENGLISH);
        dateSettings.setFormatForDatesCommonEra("dd/MM/yyyy");
        return dateSettings;
    }
    
    private static TimePickerSettings customTimeFormat () {
        TimePickerSettings timeSettings = new TimePickerSettings(Locale.ENGLISH);
        timeSettings.setFormatForDisplayTime("HH:mm");
        timeSettings.setFormatForMenuTimes("HH:mm");
        timeSettings.generatePotentialMenuTimes(TimeIncrement.OneHour, null, null);
        return timeSettings;
    }
    
    @Override
    public void showAllShifts(List<Shift> shifts) {
        SwingUtilities.invokeLater(() -> {
            shiftListModel.clear();
            shifts.forEach(this::addToList);
            enableUI();
        });
    }
    
    @Override
    public void shiftAdded(Shift shift) {
        SwingUtilities.invokeLater(() -> {
            addToList(shift);
            enableUI();
            showInfoMessage("Shift added!");
        });
    }
    
    @Override
    public void shiftRemoved(Shift shift) {
        SwingUtilities.invokeLater(() -> {
            removeFromList(shift);
            enableUI();
            showInfoMessage("Shift removed!");
        });
    }
    
    @Override
    public void shiftUpdated(Shift oldShift, Shift newShift) {
        SwingUtilities.invokeLater(() -> {
            int oldShiftIndex = shiftListModel.indexOf(oldShift);
            shiftListModel.setElementAt(newShift, oldShiftIndex);
            restoreUpdateUI();
            showInfoMessage("Shift updated!");
        });
    }
    
    @Override
    public void showErrorOverlappedShift(Shift original, Shift overlapped) {
        String message = String.format("Shift %s-%s overlaps with %s-%s on %s (%s-%s/%s-%s)",
            original.getDoctorId(), original.getDepartmentId(),
            overlapped.getDoctorId(), overlapped.getDepartmentId(),
            original.getDate(),
            original.getStartTime(), original.getEndTime(),
            overlapped.getStartTime(), overlapped.getEndTime());
        
        SwingUtilities.invokeLater(() -> {
            if (shiftListModel.indexOf(original) == -1) addToList(original);
            enableUI();
            showErrorMessage(message);
        });
    }
    
    @Override
    public void showErrorShiftNotFound(Shift shift) {
        String message = String.format("No Shift matching %s was found", shift);
        
        SwingUtilities.invokeLater(() -> {
            removeFromList(shift);
            enableUI();
            showErrorMessage(message);
        });
    }
    
    @Override
    public void showErrorDoctorNotFound(Id doctorId) {
        String message = String.format("No Doctor with id %s was found", doctorId);
        
        SwingUtilities.invokeLater(() -> {
            enableUI();
            showErrorMessage(message);
        });
    }
    
    @Override
    public void showErrorDepartmentNotFound(Id departmentId) {
        String message = String.format("No Department with id %s was found", departmentId);
        
        SwingUtilities.invokeLater(() -> {
            enableUI();
            showErrorMessage(message);
        });
    }
}
