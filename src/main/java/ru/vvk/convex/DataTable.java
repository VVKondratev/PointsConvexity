package ru.vvk.convex;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.jogamp.nativewindow.util.Dimension;

public class DataTable {
	private JFrame frame;
	JTable results;
	DefaultTableModel model;
	public DataTable(String[] titles) {
		frame = new JFrame();
		frame.setBounds(100, 100, 812, 671);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		this.frame.setLocationRelativeTo(null); 
		this.frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		model = new DefaultTableModel();
		for(int i =0;i < titles.length;i++) {
		model.addColumn(titles[i]);}
		results = new JTable(model);
        JScrollPane jscrlp = new JScrollPane(results);
        frame.getContentPane().add(jscrlp);
        frame.setVisible(true);
	}
	public void addRaw(String[] data) {
		model.addRow(data);
	}
	
}
