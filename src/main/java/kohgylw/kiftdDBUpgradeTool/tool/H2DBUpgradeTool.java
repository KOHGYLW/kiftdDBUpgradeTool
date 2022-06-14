package kohgylw.kiftdDBUpgradeTool.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.jdbc.JdbcSQLException;

import kohgylw.kiftd.printer.Printer;
import kohgylw.kiftd.server.util.KiftdProperties;

public class H2DBUpgradeTool {

	private static final String H2DB_DRIVER_NAME = "org.h2.Driver";
	private static final String H2DB_USER = "root";
	private static final String H2DB_PWD = "301537gY";
	private static final String SERVER_PROPERTIES_FILE = "server.properties";

	private String path;
	private String DEFAULT_FILE_SYSTEM_PATH;
	private String confdir;
	private KiftdProperties serverp;

	/**
	 * 
	 * <h2>执行升级</h2>
	 * <p>
	 * 该方法会尝试将旧版H2数据库的内容归档在文件节点文件夹内，并存储为“upgarde.sql”的格式。
	 * </p>
	 * 
	 * @author 青阳龙野(kohgylw)
	 * @return 升级结果，如成功则返回true，否则返回false
	 */
	public boolean upgrade() {
		// 初始化程序主目录下各个文件夹的逻辑路径
		Printer.instance.print("正在初始化设置……");
		this.path = System.getProperty("user.dir");// 开发环境下使用项目工程路径
		String classPath = System.getProperty("java.class.path");
		if (classPath.indexOf(File.pathSeparator) < 0) {
			File f = new File(classPath);
			classPath = f.getAbsolutePath();
			if (classPath.endsWith(".jar")) {
				this.path = classPath.substring(0, classPath.lastIndexOf(File.separator));// 使用环境下使用程序主目录
			}
		}
		this.DEFAULT_FILE_SYSTEM_PATH = this.path + File.separator + "filesystem" + File.separator;
		this.confdir = this.path + File.separator + "conf" + File.separator;
		this.serverp = new KiftdProperties();
		final File serverProp = new File(this.confdir + SERVER_PROPERTIES_FILE);
		if (!serverProp.isFile()) {
			Printer.instance.print("错误：请先将本程序放置在kiftd的主程序目录内（应与kiftd主程序、conf文件夹、libs文件夹等内容同级）。");
			return false;
		}
		Printer.instance.print("正在载入配置文件...");
		FileInputStream serverPropIn;
		try {
			serverPropIn = new FileInputStream(serverProp);
			this.serverp.load(serverPropIn);
		} catch (IOException e) {
			Printer.instance
					.print("错误：配置文件载入失败。如果您尚未使用过旧版本的kiftd，则可以在替换新版本内容之后直接开始使用；否则，请检查您是否在kiftd的程序主目录中具备读写权限，然后再次尝试升级。");
			return false;
		}
		String fileSystemPath;
		// 加载主文件系统路径配置
		String FSPath = this.serverp.getProperty("FS.path");
		if (FSPath == null) {
			Printer.instance.print("警告：未找到主文件系统路径配置，将采用默认值。");
			fileSystemPath = this.DEFAULT_FILE_SYSTEM_PATH;
		} else if (FSPath.equals("DEFAULT")) {
			fileSystemPath = this.DEFAULT_FILE_SYSTEM_PATH;
		} else {
			fileSystemPath = FSPath.replaceAll("\\\\:", ":").replaceAll("\\\\\\\\", "\\\\");// 后面的替换是为了兼容以前版本的设置
		}
		if (!fileSystemPath.endsWith(File.separator)) {
			fileSystemPath = fileSystemPath + File.separator;
		}
		// 检查是否开启了MySQL
		if ("true".equals(serverp.getProperty("mysql.enable"))) {
			Printer.instance.print("提示：您已将文件系统数据库替换为MySQL，因此无需进行此升级，请在替换新版本内容之后直接开始使用。");
			return true;
		}
		// 定位数据库URL
		String fileNodePath = fileSystemPath + "filenodes" + File.separator;
		File nodePath = new File(fileNodePath);
		if (!nodePath.isDirectory() || !nodePath.canRead() || !nodePath.canWrite()) {
			Printer.instance.print(
					"错误：文件节点存储路径查找失败。如果您尚未使用过旧版本的kiftd，则可以在替换新版本内容之后直接开始使用；否则请检查您是否在kiftd的程序主目录中具备读写权限，然后再次尝试升级。");
			return false;
		}
		Printer.instance.print("正在进行升级，请勿关闭程序……");
		String upgradeFilePath = fileNodePath + "upgrade.sql";
		File oldDBMvFile = new File(fileNodePath + "kift.mv.db");
		File oldDBTraceFile = new File(fileNodePath + "kift.trace.db");
		// 如果旧数据库文件仍存在
		if (oldDBMvFile.exists() || oldDBTraceFile.exists()) {
			Printer.instance.print("正在导出旧数据，请勿关闭程序……");
			// 先清除旧归档文件（若有）
			File oldUpgradeFile = new File(upgradeFilePath);
			if (oldUpgradeFile.exists()) {
				if (!oldUpgradeFile.delete()) {
					Printer.instance.print("错误：无法清理旧文件。请手动删除[" + oldUpgradeFile.getAbsolutePath() + "]文件，然后再次尝试升级。");
					return false;
				}
			}
			// 尝试从旧数据库中导出数据
			String dbURL = "jdbc:h2:file:" + fileNodePath + "kift";
			// 连接数据库
			try {
				Class.forName(H2DB_DRIVER_NAME).newInstance();
				Connection conn = DriverManager.getConnection(dbURL, H2DB_USER, H2DB_PWD);
				// 将数据库的内容保存为SQL归档文件
				Statement stateScript = conn.createStatement();
				stateScript.execute("SCRIPT TO '" + upgradeFilePath + "'");
				stateScript.close();
				conn.close();
				// 检查是否导出成功
				File newUpgradeFile = new File(upgradeFilePath);
				if (!newUpgradeFile.exists()) {
					Printer.instance.print("错误：旧数据库归档失败。请确保您具备读写权限，然后再次尝试升级。");
					return false;
				}
				Printer.instance.print("导出成功。");
			} catch (JdbcSQLException e) {
				Printer.instance.print("错误：数据库版本格式异常。如果您之前已经进行了升级操作并成功，则无需重复执行本操作。");
				return false;
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
				Printer.instance.print("错误：" + e.getClass().getName() + ": " + e.getMessage());
				return false;
			}
		}
		// 如果新归档文件生成成功，或已有旧的归档文件且数据库已被删除，则尝试清理旧数据库的文件
		Printer.instance.print("正在检查数据……");
		File newUpgradeFile = new File(upgradeFilePath);
		if (!newUpgradeFile.exists()) {
			Printer.instance
					.print("错误：旧数据库归档失败。如果您尚未使用过旧版本的kiftd，则可以在替换新版本内容之后直接开始使用；否则请检查您是否在kiftd的程序主目录中具备读写权限，然后再次尝试升级。");
			return false;
		}
		Printer.instance.print("正在清理旧文件……");
		if ((oldDBMvFile.exists() && !oldDBMvFile.delete()) || (oldDBTraceFile.exists() && !oldDBTraceFile.delete())) {
			Printer.instance.print("错误：无法清理旧文件。请手动删除[" + oldDBMvFile.getAbsolutePath() + "]文件和["
					+ oldDBTraceFile.getAbsolutePath() + "]文件（共计2项），然后再次尝试升级。");
			return false;
		}
		// 至此，升级成功
		Printer.instance.print("升级完成！您可以开始体验新版本了。祝您使用愉快~");
		return true;
	}

}
