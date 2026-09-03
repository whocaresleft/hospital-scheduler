package org.duckdns.whocaresleft.view.swing;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.duckdns.whocaresleft.presenter.DepartmentPresenter;
import org.duckdns.whocaresleft.presenter.DoctorPresenter;
import org.duckdns.whocaresleft.presenter.ShiftPresenter;
import org.duckdns.whocaresleft.view.DepartmentView;
import org.duckdns.whocaresleft.view.DoctorView;
import org.duckdns.whocaresleft.view.ShiftView;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SwingHospitalFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    
    private transient DoctorPresenter doctorPresenter;
    private transient DepartmentPresenter departmentPresenter;
    private transient ShiftPresenter shiftPresenter;
    
    private SwingDoctorView doctorView;
    private SwingDepartmentView departmentView;
    private SwingShiftView shiftView;
    private JLabel hospitalLabel;
    
    public SwingHospitalFrame() {
        setTitle("Hospital Scheduler X");
        
        doctorView = new SwingDoctorView();
        departmentView = new SwingDepartmentView();
        shiftView = new SwingShiftView();
        
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 600);
        setMinimumSize(new Dimension(450, 600));
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{0, 0};
        gbl_contentPane.rowHeights = new int[]{0, 0, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);
        
        hospitalLabel = new JLabel("Hospital Scheduler");
        hospitalLabel.setName("hospitalLabel");
        GridBagConstraints gbc_hospitalLabel = new GridBagConstraints();
        gbc_hospitalLabel.insets = new Insets(0, 0, 5, 0);
        gbc_hospitalLabel.gridx = 0;
        gbc_hospitalLabel.gridy = 0;
        contentPane.add(hospitalLabel, gbc_hospitalLabel);
        
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
        gbc_tabbedPane.fill = GridBagConstraints.BOTH;
        gbc_tabbedPane.gridx = 0;
        gbc_tabbedPane.gridy = 1;
        contentPane.add(tabbedPane, gbc_tabbedPane);
        
        doctorView.setName("doctorView");
        tabbedPane.addTab("Doctors", null, doctorView, null);
        
        departmentView.setName("departmentView");
        tabbedPane.addTab("Departments", null, departmentView, null);
        
        shiftView.setName("shiftView");
        tabbedPane.addTab("Shifts", null, shiftView, null);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) { new Thread(() -> doctorPresenter.allDoctors()).start(); }
        });
        
        tabbedPane.addChangeListener(e -> {
            int index = tabbedPane.getSelectedIndex();
            
            if (index == 0)
                new Thread(() -> doctorPresenter.allDoctors()).start();
            else if (index == 1)
                new Thread(() -> departmentPresenter.allDepartments()).start();
            else if (index == 2)
                new Thread(() -> shiftPresenter.allShifts()).start();
        });
    }
    
    public void setDoctorPresenter(DoctorPresenter doctorPresenter) {
        this.doctorPresenter = doctorPresenter;
        doctorView.setPresenter(doctorPresenter);
    }
    
    public void setDepartmentPresenter(DepartmentPresenter departmentPresenter) {
        this.departmentPresenter = departmentPresenter;
        departmentView.setPresenter(departmentPresenter);
    }
    
    public void setShiftPresenter(ShiftPresenter shiftPresenter) {
        this.shiftPresenter = shiftPresenter;
        shiftView.setPresenter(shiftPresenter);
    }
    
    public DoctorView getDoctorView() { return doctorView; }
    public DepartmentView getDepartmentView() { return departmentView; }
    public ShiftView getShiftView() { return shiftView; }
}
