package com.wxf.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

import com.wxf.util.SqlGenerator;

import javax.swing.JTextArea;

/**
 * 
 * SqlGeneratorUI sql生成器界面
 * 
 * @author wenxiaofei
 *
 * @date 2018年6月22日
 *
 */
public class SqlGeneratorUI extends JFrame {

	private JPanel contentPane;
	private JTabbedPane tabbedPane;
	private JPanel otherTab;
	private JPanel netToolTab;
	private JPanel sqlGeneratorTab;
	private JSplitPane splitPane;
	private JPanel operationPanel;
	private JSplitPane operationSplitPane;
	private JScrollPane loggerScrollPanel;
	private JButton selectHour;
	private JButton selectDay;
	private JButton selectWeek;
	private JPanel panel;
	private JButton btnSelectPath;
	private JButton btnOpenfile;
	private JButton btnExecute;

	private String file = null;
	private JScrollPane configScrollPane;
	private JTextArea configSqlArea;

	private int interval = 5;
	private int dayOffset = 0;
	private int from, to;
	
	public static enum SqlGenerateType {
		WEEK, CURRENTDAY, DURINGHOUR, FROMZEROTOCURRENT, LASTDAY
	}

	// 选择生成sql的类型
	private SqlGenerateType selectType;
	private JButton btnSelectbeforecurrent;
	private JButton btnExit;
	private JTextArea loggerTextArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SqlGeneratorUI frame = new SqlGeneratorUI();
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
	public SqlGeneratorUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 608, 398);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);

		sqlGeneratorTab = new JPanel();
		sqlGeneratorTab.setName("SqlGenerator");
		sqlGeneratorTab.setToolTipText("");
		tabbedPane.addTab(" SqlGenerator ", null, sqlGeneratorTab, null);
		sqlGeneratorTab.setLayout(new BorderLayout(0, 0));

		splitPane = new JSplitPane();
		splitPane.setDividerSize(3);
		sqlGeneratorTab.add(splitPane, BorderLayout.CENTER);

		operationPanel = new JPanel();
		splitPane.setLeftComponent(operationPanel);
		operationPanel.setLayout(new BorderLayout(0, 0));

		operationSplitPane = new JSplitPane();
		operationSplitPane.setDividerSize(3);
		operationSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		operationPanel.add(operationSplitPane);

		panel = new JPanel();
		operationSplitPane.setLeftComponent(panel);

		selectHour = new JButton("selectHour");
		selectHour.setActionCommand("selectHour");
		selectHour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// JFrame f = new JFrame();
				// f.setContentPane(new JCalendar());
				// f.pack();
				// // f.setResizable(false);
				// f.show();
				acceptInterval();
				acceptDuringHours();
			}
		});
		panel.setLayout(new GridLayout(0, 4, 0, 0));
		panel.add(selectHour);

		selectDay = new JButton("selectDay");
		selectDay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				acceptInterval();
				acceptDayOffSet();
				selectType = SqlGenerateType.CURRENTDAY;
			}

		});
		panel.add(selectDay);

		selectWeek = new JButton("selectWeek");
		selectWeek.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 生成一周的数据
				acceptInterval();
				selectType = SqlGenerateType.WEEK;

			}

		});
		
		btnSelectbeforecurrent = new JButton("selectBeforeCurrent");
		btnSelectbeforecurrent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				acceptInterval();
				selectType=SqlGenerateType.FROMZEROTOCURRENT;
			}
		});
		panel.add(btnSelectbeforecurrent);
		panel.add(selectWeek);

		btnSelectPath = new JButton("selectSaveFile");
		btnSelectPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser("C://Users//Administer/Desktop", new FileSystemView() {
					@Override
					public File createNewFolder(File containingDir) throws IOException {
						// TODO Auto-generated method stub
						File folder = new File(containingDir + "\\新建文件夹");
						if (!folder.exists())
							folder.mkdir();
						return folder;
					}
				});
				int ret = fileChooser.showSaveDialog(getContentPane());
				if (ret == JFileChooser.APPROVE_OPTION) {
					System.out.println("保存文件");
					saveFileToLocal(fileChooser);
					btnExecute.setEnabled(true);
				} else if (ret == JFileChooser.CANCEL_OPTION) {
					System.out.println("取消");
				}
			}
		});
		panel.add(btnSelectPath);

		btnOpenfile = new JButton("openFile");
		btnOpenfile.setEnabled(false);
		btnOpenfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Runtime.getRuntime().exec("explorer "+file);
					Desktop.getDesktop().open(new File(file));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		panel.add(btnOpenfile);

		btnExecute = new JButton("execute");
		btnExecute.setEnabled(false);
		btnExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						execute(file);
						JOptionPane.showMessageDialog(getContentPane(), "sql生成完毕!");
					}
				}).start();

			}
		});
		panel.add(btnExecute);
		
		btnExit = new JButton("exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				dispose();
				System.exit(0);
			}
		});
		panel.add(btnExit);

		configScrollPane = new JScrollPane();
		operationSplitPane.setRightComponent(configScrollPane);

		configSqlArea = new JTextArea();
		configSqlArea.setEditable(false);
		configScrollPane.setViewportView(configSqlArea);
		operationSplitPane.setDividerLocation(0.5);

		loggerScrollPanel = new JScrollPane();
		splitPane.setRightComponent(loggerScrollPanel);

		loggerTextArea=new JTextArea();
		loggerScrollPanel.setViewportView(loggerTextArea);
		splitPane.setDividerLocation(0.5);

		netToolTab = new JPanel();
		tabbedPane.addTab("  NetTools  ", null, netToolTab, null);

		otherTab = new JPanel();
		tabbedPane.addTab("  Other  ", null, otherTab, null);
	}

	/**
	 * 接收时间间隔参数
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018年6月23日
	 *
	 */
	private void acceptInterval() {
		String strInterval = JOptionPane.showInputDialog(getContentPane(), "请输入时间间隔，单位是秒", "5");
		try {
			this.interval = Integer.parseInt(strInterval);
		} catch (Exception e2) {
			JOptionPane.showMessageDialog(getContentPane(), "输入的时间间隔不对，请重新输入", "错误提示", JOptionPane.ERROR_MESSAGE);
//			acceptInterval();
		}
	}

	private void acceptDayOffSet() {
		String strDayOffset = JOptionPane.showInputDialog(getContentPane(), "请输入天数的偏移量，当天怎为0，过去的天的偏移量则为负数；如-1表示过去1天的数",
				"0");
		try {
			this.dayOffset = Integer.parseInt(strDayOffset);
		} catch (Exception e2) {
			JOptionPane.showMessageDialog(getContentPane(), "输入的天数偏移量不对，请重新输入", "错误提示", JOptionPane.ERROR_MESSAGE);
//			acceptDayOffSet();
		}
	}

	private void acceptDuringHours() {
		String strDuring = JOptionPane.showInputDialog(getContentPane(),
				"请输入要生成数据的起止时间，以小时输入，格式为：from,to;同时from<to;比如5，9", "0,0");
		try {
			String[] during = strDuring.split(",");
			if (during.length == 2) {
				this.from = Integer.parseInt(during[0]);
				this.to = Integer.parseInt(during[1]);
				this.selectType = SqlGenerateType.DURINGHOUR;
			} else {
				throw new RuntimeException("输入的时间段不对");
			}
		} catch (Exception e2) {
			JOptionPane.showMessageDialog(getContentPane(), "输入的时间段不对，请重新输入", "错误提示", JOptionPane.ERROR_MESSAGE);
//			acceptDuringHours();
		}
	}

	/**
	 * 执行创建sql的程序
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018年6月22日
	 *
	 */
	private void execute(String file) {
		// TODO Auto-generated method stub
		SqlGenerator sqlGeneator = SqlGenerator.createSqlGeneator(loggerTextArea);
		String[] lineTags = new String[] { "HandleTotal", "HandleFin", "HandleExp" };
		Calendar date;
		if (file != null) {
			switch (selectType) {
			case WEEK:
				date= Calendar.getInstance();
				sqlGeneator.generateSqlOneWeek(file, lineTags, date, interval);
				break;
			case CURRENTDAY:
				date= Calendar.getInstance();
				sqlGeneator.generateSqlOneDay(file, lineTags, date, dayOffset, interval);
				break;
			case DURINGHOUR:
				date= Calendar.getInstance();
				sqlGeneator.generateSqlWithDuringTime(file, lineTags, from, to, date, interval);
				break;
			case FROMZEROTOCURRENT:
				 date= Calendar.getInstance();
				sqlGeneator.generateSqlOneDayOfCurrentBefore(file, lineTags, date.get(Calendar.HOUR_OF_DAY), date,
						interval);
				break;
			default:
				break;
			}

			btnOpenfile.setEnabled(true);
			btnExecute.setEnabled(false);
		} else {
			JOptionPane.showMessageDialog(getContentPane(), "您还没有选择保sql语句的文件，请先选择保存文件的位置", "消息提示框",
					JOptionPane.OK_OPTION);

		}
	}

	/**
	 * 保存文件到本地
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018年6月22日
	 *
	 * @param fileChooser
	 */
	private void saveFileToLocal(JFileChooser fileChooser) {
		// TODO Auto-generated method stub
		File file = fileChooser.getSelectedFile();
		this.file = file.getAbsolutePath();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			int select = JOptionPane.showConfirmDialog(getContentPane(), "文件已存在是否覆盖", "消息提示框",
					JOptionPane.YES_NO_OPTION);
			if (select == JOptionPane.YES_OPTION) {
				System.out.println("yes");
				try {
					file.delete();
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (select == JOptionPane.NO_OPTION) {
				System.out.println("no");
			}
		}
	}

}
