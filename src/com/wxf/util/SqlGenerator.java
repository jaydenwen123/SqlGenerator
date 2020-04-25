package com.wxf.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * 
 * Ϊ�˹�����Ҫ������һЩ�����õ�sql����
 * 
 * @author wenxiaofei
 *
 * @date 2018��6��13��
 *
 */
public class SqlGenerator {

	private JTextComponent textComponent = null;

	private static SqlGenerator sqlGenerator = null;
	
	private static int index=1;

	/**
	 * ˫����
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��22��
	 *
	 * @param textComponent
	 * @return
	 */
	public static SqlGenerator createSqlGeneator(JTextComponent textComponent) {
		if (sqlGenerator == null)
			synchronized (SqlGenerator.class) {
				if (sqlGenerator == null)
					sqlGenerator = new SqlGenerator(textComponent);
			}
		return sqlGenerator;
	}

	private SqlGenerator(JTextComponent textComponent) {
		super();
		this.textComponent = textComponent;
	}
	
	

	// private final java.util.List<StringBuilder> sqlList=new
	// ArrayList<>();

	private final String INSERT_SQL_COLUMNS = "insert into ord.`sequence_collect_data_21` "
			+ " (`TAG`, `TIME_STAMP`, `FIELD`, `CREATE_DATE`,"
			+ " `COLLECT_ID`, `STATE`, `SCAN_TITLE`, `RECORD_INDEX`) values";

	private SqlGenerator() {

	}
	
	public void clearIndex(){
		index=1;
	}

	/**
	 * 
	 * ����һ�ܵĲ�������
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��13��
	 *
	 * @param filePath
	 * @param lineTags
	 * @param date
	 * @param hourOffset
	 * @param interval
	 */
	public void generateSqlOneWeek(String filePath, String[] lineTags, Calendar date, int interval) {
		generateSqlOneDay(filePath, lineTags, date, 0, interval);
		for (int i = -6; i < 0; i++) {
			clearIndex();
			generateSqlOneDay(filePath, lineTags, date, -1, interval);
		}
	}

	/**
	 * 
	 * ����һ��Ĳ�������
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��13��
	 *
	 * @param filePath
	 * @param lineTags
	 * @param date
	 * @param hourOffset
	 * @param interval
	 */
	public void generateSqlOneDay(String filePath, String[] lineTags, Calendar date, int dayOffset, int interval) {
		date.add(Calendar.DAY_OF_MONTH, dayOffset);
		clearIndex();
		for (int i = 0; i < 24; i++) {
			generateSql(filePath, lineTags, date, i, interval, true);
		}
	}

	/**
	 * 
	 * ����һ��Ĳ�������
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��13��
	 *
	 * @param filePath
	 * @param lineTags
	 * @param date
	 * @param hourOffset
	 * @param interval
	 */
	public  void generateSqlOneDayOfCurrentBefore(String filePath, String[] lineTags, int current, Calendar date,
			int interval) {
		clearIndex();
		generateSqlWithDuringTime(filePath, lineTags, 0, current, date, interval);
	}

	/**
	 * 
	 * ����һ��Ĳ�������
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��13��
	 *
	 * @param filePath
	 * @param lineTags
	 * @param date
	 * @param hourOffset
	 * @param interval
	 */
	public void generateSqlWithDuringTime(String filePath, String[] lineTags,
			int from, int to, Calendar date,
			int interval) {
		//���¼�������
		index=((from-0)*60)/interval+1;
		for (int i = from; i < to; i++) {
			generateSql(filePath, lineTags, date, i, interval, true);
		}
	}

	/**
	 * ���ɵ�ǰʱ��ǰ��ĳ��Сʱ�ڵ�����
	 * 
	 * �˴�Ĭ�ϣ�lineTags��0��Ԫ����������lineTags���һ��Ԫ��Ϊ�쳣����������֯����
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��13��
	 *
	 * @param filePath
	 * @param lineTags
	 * @param date
	 * @param hourOffset
	 * @param interval
	 */
	public void generateSql(String filePath, String[] lineTags, Calendar date, int hour, int interval, boolean append) {
		// insert into `sequence_collect_data_21` (`TAG`, `TIME_STAMP`, `FIELD`,
		// `CREATE_DATE`, `COLLECT_ID`, `STATE`, `SCAN_TITLE`, `RECORD_INDEX`)
		// values
		// ('HandleExp','20180613100000','20','2018-06-13
		// 10:00:00',NULL,'1','60����ͳ��һ��','10');
		if (lineTags == null || lineTags.length == 0)
			return;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		Random random = new Random();
		// System.out.println(dateFormat.format(new Date()));
		// 1.���浽filePath�ļ���
		FileWriter fw = null;
		try {
			fw = new FileWriter(filePath, append);
			int i = 0;
			// ���ѭ������ʱ��
			StringBuilder sb = null;
			int start = 0;
			int num;
			Timestamp ts;
			// date.add(Calendar.HOUR_OF_DAY, hourOffset);
			date.set(Calendar.HOUR_OF_DAY, hour);
			while (start < 60) {

				date.set(Calendar.MINUTE, start);
				date.set(Calendar.SECOND, 0);
				// �ڲ�ѭ����������
				for (String line : lineTags) {
					if (line.equals(lineTags[lineTags.length - 1])) {
						num = random.nextInt(20) + 10;
					} else if (line.equals(lineTags[0])) {
						num = random.nextInt(70) + 20;
					} else {
						num = random.nextInt(50) + 15;
					}
					ts = new java.sql.Timestamp(date.getTime().getTime());
					sb = new StringBuilder(INSERT_SQL_COLUMNS);
					// ƴ��tag
					sb.append(" (\'").append(line).append("\'").append(",").append("\'")
							.append(dateFormat.format(date.getTime())).append("\'").append(",").append(num)
							.append(",\'").append(ts.toString().substring(0, ts.toString().lastIndexOf(".")))
							.append("\',").append("NULL").append(",1").append(",\'").append(interval + "����ͳ��һ��\'")
							.append(",").append(index + ");");
					// System.setOut(out);
					// �ض��������
					if (textComponent instanceof JTextPane)
						redirectSystemStreamsWithTextPane();
					else if (textComponent instanceof JTextArea)
						redirectSystemStreamsWithTextArea();

					System.out.println(sb.toString());
					fw.write(sb.toString());
					// sqlList.add(sb);
					fw.write("\r\n");
				}
				start += interval;
				index++;
			}
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * ���ı�text��ֵ��ʾ���ؼ�text��
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��22��
	 *
	 * @param text
	 */
	private void updateTextPane(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JTextPane textPane = (JTextPane) textComponent;
				Document doc = textPane.getDocument();
				try {
					doc.insertString(doc.getLength(), text, null);
				} catch (BadLocationException e) {
					throw new RuntimeException(e);
				}
				textPane.setCaretPosition(doc.getLength() - 1);
			}
		});
	}

	/**
	 * �ض����׼�������TextPane�ؼ���
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��22��
	 *
	 */
	private void redirectSystemStreamsWithTextPane() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(final int b) throws IOException {
				updateTextPane(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextPane(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}

	/**
	 * ���ı�text��ʾ��TextArea�ؼ���
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��22��
	 *
	 * @param text
	 */
	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JTextArea textArea = (JTextArea) textComponent;
				textArea.append(text);
			}
		});
	}

	/**
	 * �����������TextArea��
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��22��
	 *
	 */
	private void redirectSystemStreamsWithTextArea() {
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}

			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}

	/**
	 * ����ord.`sequence_collect_data_21`�õĲ�������
	 * 
	 * Ŀ�ģ����ɵ�ǰ��֮ǰһ������ݣ�ͬʱ���ɵ�ǰʱ����ǰ������
	 * 
	 * @author wenxiaofei
	 *
	 * @date 2018��6��13��
	 *
	 * @param filePathJ
	 */
	public void generateSql(String filePath, String[] lineTags, Calendar date, int interval) {
		generateSql(filePath, lineTags, date, 0, interval, false);
	}

	public static void main(String[] args) {
		Calendar c = Calendar.getInstance();
		String[] lineTags = new String[] { "HandleTotal", "HandleFin", "HandleExp" };
		// generateSqlWithDuringTime("E:\\testSql.sql", lineTags, 9,14,c, 5 );
		// generateSqlOneDayOfCurrentBefore("E:\\testSql.sql", lineTags,23, c,
//		 generateSqlOneDay("E:\\testSql.sql", lineTags,c, 0, 3 );
		// generateSqlOneWeek("E:\\weekSql.sql", lineTags,c, 5 );
		// generateSql(filePath, lineTags, date, hour, interval, append);
		// generateSql("E:\\test.sql", lineTags, c, 5);
		new SqlGenerator(null).generateSqlOneDayOfCurrentBefore("E:\\testSql.sql",lineTags, 19, c,
			5) ;
	}

	private void testDate() {
		Calendar c = Calendar.getInstance();
		System.out.println(c.get(Calendar.YEAR));
		System.out.println(c.get(Calendar.DAY_OF_MONTH));
		// ǰһ��
		c.add(Calendar.DAY_OF_MONTH, -5);
		System.out.println(c.get(Calendar.DAY_OF_MONTH));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		System.out.println(dateFormat.format(new Date()));

		Date d = c.getTime();
		System.out.println(dateFormat.format(d));
		// System.out.println(c.);
		Instant instant = Instant.now();
		Instant instant2 = Instant.now();
		System.out.println(instant.getNano());
	}
}
