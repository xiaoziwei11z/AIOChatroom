package main;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import controller.DownloadController;
import controller.SelectController;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListModel;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JOptionPane;

public class DownloadDialog extends JDialog {
	public static DefaultListModel<String> model;
	public static JList<String> list;
	private final JPanel contentPanel = new JPanel();
	private String path = null;

	public DownloadDialog() {
		setTitle("请选择要下载的文件");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		WindowAdapter windowAdapter = new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Client.downButton.setEnabled(true);
				Client.upButton.setEnabled(true);
			}
		};
		addWindowListener(windowAdapter);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 434, 228);
		contentPanel.add(scrollPane);
		
		model = new DefaultListModel<>();
		list = new JList<>(model);
		SelectController.send();
		scrollPane.setViewportView(list);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("确定");
				DownloadDialog downloadDialog = this;
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(path == null){
							JOptionPane.showMessageDialog(downloadDialog, "请先选择下载路径");
						}else if(list.getSelectedValue() == null){
							JOptionPane.showMessageDialog(downloadDialog, "请选择文件");
						}else if(list.getSelectedValues().length > 1){
							JOptionPane.showMessageDialog(downloadDialog, "只能选择一个文件");
						}else{
							DownloadController.download(list.getSelectedValue());
							DownloadController.readFile(list.getSelectedValue(),path);
							removeWindowListener(windowAdapter);
							dispose();
						}
					}
				});
				{
					JButton button = new JButton("选择路径");
					button.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JFileChooser fileChooser = new JFileChooser();
							fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							fileChooser.showDialog(downloadDialog,"请选择保存的路径");
							if(fileChooser.getSelectedFile()!=null){
								path = fileChooser.getSelectedFile().getAbsolutePath();
							}
						}
					});
					buttonPane.add(button);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("取消");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setLocation(750, 300);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
}
