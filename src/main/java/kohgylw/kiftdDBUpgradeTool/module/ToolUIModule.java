package kohgylw.kiftdDBUpgradeTool.module;

import java.awt.event.*;
import javax.swing.border.*;
import java.awt.*;
import javax.swing.event.*;

import kohgylw.kiftd.ui.module.KiftdDynamicWindow;
import kohgylw.kiftdDBUpgradeTool.tool.H2DBUpgradeTool;

import javax.swing.*;
import java.text.*;
import java.util.*;

public class ToolUIModule extends KiftdDynamicWindow {

	protected static JFrame window;
	private static JTextArea output;
	private static ToolUIModule instance;
	private static JButton upgrade;
	private static JButton exit;
	private SimpleDateFormat sdf;
	/**
	 * 窗口原始宽度
	 */
	private final int OriginSize_Width = 300;
	/**
	 * 窗口原始高度
	 */
	private final int OriginSize_Height = 460;

	private ToolUIModule() throws Exception {
		setUIFont();
		(ToolUIModule.window = new JFrame("kiftd数据库升级工具")).setSize(OriginSize_Width, OriginSize_Height);
		ToolUIModule.window.setLocation(100, 100);
		ToolUIModule.window.setResizable(false);
		ToolUIModule.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ToolUIModule.window.setLayout(new BoxLayout(ToolUIModule.window.getContentPane(), 3));
		final JPanel titlebox = new JPanel(new FlowLayout(1));
		titlebox.setBorder(new EmptyBorder(0, 0, (int) (-25 * proportion), 0));
		final JLabel title = new JLabel("KDUT");
		title.setFont(new Font("宋体", 1, (int) (30 * proportion)));
		titlebox.add(title);
		ToolUIModule.window.add(titlebox);
		final JPanel subtitlebox = new JPanel(new FlowLayout(1));
		subtitlebox.setBorder(new EmptyBorder(0, 0, (int) (-20 * proportion), 0));
		final JLabel subtitle = new JLabel("-- kiftd数据库升级工具 --");
		subtitle.setFont(new Font("宋体", 0, (int) (13 * proportion)));
		subtitlebox.add(subtitle);
		ToolUIModule.window.add(subtitlebox);
		final JPanel buttonBox = new JPanel(new GridLayout(2, 1));
		buttonBox.add(ToolUIModule.upgrade = new JButton("升级(Upgrade)[^]"));
		buttonBox.add(ToolUIModule.exit = new JButton("退出(Exit)[X]"));
		ToolUIModule.window.add(buttonBox);
		final JPanel outputBox = new JPanel(new FlowLayout(1));
		outputBox.add(new JLabel("[输出信息(Server Message)]："));
		(ToolUIModule.output = new JTextArea()).setLineWrap(true);
		output.setRows(9 + (int) (proportion));
		output.setSize((int) (292 * proportion), 100);
		ToolUIModule.output.setEditable(false);
		ToolUIModule.output.setForeground(Color.RED);
		ToolUIModule.output.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				Thread t = new Thread(() -> {
					if (output.getLineCount() >= 1000) {
						int end = 0;
						try {
							end = output.getLineEndOffset(100);
						} catch (Exception exc) {
						}
						output.replaceRange("", 0, end);
					}
					output.setCaretPosition(output.getText().length());
				});
				t.start();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				output.selectAll();
				output.setCaretPosition(output.getSelectedText().length());
				output.requestFocus();
			}
		});
		outputBox.add(new JScrollPane(ToolUIModule.output));
		ToolUIModule.window.add(outputBox);
		final JPanel bottombox = new JPanel(new FlowLayout(1));
		bottombox.setBorder(new EmptyBorder(0, 0, (int) (-30 * proportion), 0));
		bottombox.add(new JLabel("--青阳龙野@kohgylw--"));
		ToolUIModule.window.add(bottombox);
		ToolUIModule.upgrade.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				upgrade.setEnabled(false);
				exit.setEnabled(false);
				Thread t = new Thread(() -> {
					H2DBUpgradeTool tool = new H2DBUpgradeTool();
					boolean result = tool.upgrade();
					// 升级完毕，无论结果如何都要启用“退出”按钮
					exit.setEnabled(true);
					if (!result) {
						// 如果升级失败，额外启用“升级”按钮，以便用户重试
						upgrade.setEnabled(true);
					}
				});
				t.start();
			}
		});
		ToolUIModule.exit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		modifyComponentSize(ToolUIModule.window);
	}

	public void show() {
		ToolUIModule.window.setVisible(true);
		printMessage("欢迎使用！");
		printMessage("升级方法：1，退出旧版本的kiftd，并将新版本的jar主程序、libs文件夹、"
				+ "mybatisResource文件夹、fonts文件夹和webContext文件夹共5项计拷贝至旧版本内替换对应的内容；2，将该工具的jar程序也拷贝至旧版本的主目录内；"
				+ "3，启动该工具并点击“升级”按钮；4，升级完成后退出该工具（之后您可以选择删除该工具）；5，开始体验新版本。");
		printMessage("准备就绪。");
	}

	public static ToolUIModule getInsatnce() throws Exception {
		if (ToolUIModule.instance == null) {
			ToolUIModule.instance = new ToolUIModule();
		}
		return ToolUIModule.instance;
	}

	private void exit() {
		ToolUIModule.upgrade.setEnabled(false);
		this.printMessage("退出程序...");
		System.exit(0);
	}

	public void printMessage(final String context) {
		ToolUIModule.output.append("[" + this.getFormateDate() + "]" + context + "\n");
	}

	private String getFormateDate() {
		if (null == sdf) {
			sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		}
		return sdf.format(new Date());
	}
}
