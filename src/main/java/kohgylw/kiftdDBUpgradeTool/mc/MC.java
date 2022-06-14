package kohgylw.kiftdDBUpgradeTool.mc;

import kohgylw.kiftd.printer.Printer;
import kohgylw.kiftdDBUpgradeTool.module.ToolUIModule;
import kohgylw.kiftdDBUpgradeTool.tool.H2DBUpgradeTool;

/**
 * 
 * <h2>kiftd数据库升级工具</h2>
 * <p>
 * 该工具用于将kiftd 1.0.35或更早版本的内置H2数据库导出为SQL脚本，以供1.1.0或更新版本的kiftd自动识别并导入。
 * </p>
 * <h4>许可声明：</h4>
 * <p>
 * 使用该源代码（包括但不限于：分发、修改、编译）代表您接受以下条款：
 * </p>
 * <ul>
 * <li>任何人均可以免费获取本程序的源代码的原版拷贝，并进行分发或修改，并可用于任何用途。</li>
 * <li>经由该源代码或其修改版本编译而成的程序也可以运用于任何用途，无论是商业的还是非商业的。</li>
 * <li>作者青阳龙野（kohgylw@163.com）无需为使用该源代码或其编译生成的程序所导致的任何后果承担责任。</li>
 * <li>作者青阳龙野（kohgylw@163.com）保留kiftd原版源代码及其编译生成的程序的版权。</li>
 * </ul>
 * 
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public class MC {

	private static final String COMMAND_UPGARDE = "-upgarde";// 以命令模式启动的参数

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			// 无参数情况下以图形模式启动
			Printer.init(true);
			try {
				ToolUIModule ui = ToolUIModule.getInsatnce();
				ui.show();
			} catch (Exception e) {
				System.out.println("错误：无法以图形模式启动。错误信息如下：");
				e.printStackTrace();
				System.out.println("（提示：如果您不具备图形界面，也可以使用“"+COMMAND_UPGARDE+"”参数启动程序并执行升级操作。）");
			}
		} else if (args.length == 1 && COMMAND_UPGARDE.equals(args[0])) {
			// 当输入“-upgrade”参数时，以命令模式启动
			H2DBUpgradeTool tool =new H2DBUpgradeTool();
			tool.upgrade();
		} else {
			// 当输入其他参数时，认为输入错误
			System.out.println("错误：无法识别的参数。如果您希望使用图形模式启动程序，请勿输入任何参数。如果您希望使用命令模式启动程序，请输入参数“"+COMMAND_UPGARDE+"”。");
		}
	}

}
