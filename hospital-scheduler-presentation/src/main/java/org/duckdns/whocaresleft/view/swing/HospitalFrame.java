package org.duckdns.whocaresleft.view.swing;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class HospitalFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private final JTabbedPane tabbedPane;
    
    public HospitalFrame(SwingDoctorView doctorView) {
        
        super("Hospital Scheduler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setName("mainTabbedPain");
        
        tabbedPane.addTab("Doctor View", doctorView);
        
        add(tabbedPane);
    }
}
