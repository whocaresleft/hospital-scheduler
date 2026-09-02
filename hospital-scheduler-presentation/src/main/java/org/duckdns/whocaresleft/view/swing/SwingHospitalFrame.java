package org.duckdns.whocaresleft.view.swing;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;

public class SwingHospitalFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    
    private SwingDoctorView doctorView;
    private SwingDepartmentView departmentView;
    private SwingShiftView shiftView;
    private JLabel hospitalLabel;
    
    public SwingHospitalFrame() {
        doctorView = new SwingDoctorView();
        departmentView = new SwingDepartmentView();
        shiftView = new SwingShiftView();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
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
    }
}
