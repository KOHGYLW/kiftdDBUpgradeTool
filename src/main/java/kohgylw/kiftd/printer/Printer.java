package kohgylw.kiftd.printer;

import kohgylw.kiftdDBUpgradeTool.module.ToolUIModule;
import kohgylw.kiftd.server.util.*;

public class Printer
{
    public static Printer instance;
    private static boolean isUIModel;
    private static ToolUIModule tum;
    
    public static void init(final boolean isUIModel) {
        Printer.instance = new Printer();
        if (isUIModel) {
            try {
				Printer.tum = ToolUIModule.getInsatnce();
				Printer.isUIModel = isUIModel;
			} catch (Exception e) {
				System.out.println("错误：无法以UI模式输出信息，自动切换至命令模式输出。详细信息："+e);
			}
        }
    }
    
    public void print(final String context) {
        if (Printer.instance != null) {
            if (Printer.isUIModel) {
                Printer.tum.printMessage(context);
            }
            else {
                System.out.println("[" + new String(ServerTimeUtil.accurateToSecond().getBytes()) + "]" + new String(context.getBytes()) + "\r\n");
            }
        }
    }
}
