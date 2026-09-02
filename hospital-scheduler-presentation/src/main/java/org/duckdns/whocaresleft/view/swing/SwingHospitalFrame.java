package org.duckdns.whocaresleft.view.swing;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class SwingHospitalFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    SwingHospitalFrame frame = new SwingHospitalFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public SwingHospitalFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 600);
        setMinimumSize(new Dimension(450, 600));
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{0, 0};
        gbl_contentPane.rowHeights = new int[]{0, 0};
        gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{1.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
        gbc_tabbedPane.fill = GridBagConstraints.BOTH;
        gbc_tabbedPane.gridx = 0;
        gbc_tabbedPane.gridy = 0;
        contentPane.add(tabbedPane, gbc_tabbedPane);
        
        SwingDoctorView swingDoctorView = new SwingDoctorView();
        swingDoctorView.setName("doctorView");
        tabbedPane.addTab("Doctors", null, swingDoctorView, null);
        
        SwingDepartmentView swingDepartmentView = new SwingDepartmentView();
        swingDepartmentView.setName("departmentView");
        tabbedPane.addTab("Departments", null, swingDepartmentView, null);
        
        SwingShiftView swingShiftView = new SwingShiftView();
        swingShiftView.setName("shiftView");
        tabbedPane.addTab("Shifts", null, swingShiftView, null);

    }

}
