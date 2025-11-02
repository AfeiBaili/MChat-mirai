package online.afeibaili;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.List;

public final class MChat extends JavaPlugin {
    public static final MChat INSTANCE = new MChat();
    public static final List<Long> GROUPS = new ArrayList<>();
    public static final List<Long> MASTERS = new ArrayList<>();
    public static MiraiLogger logger;
    static Bot bot;

    static {
        GROUPS.add(975709430L);
//        GROUPS.add(962295696L);
        MASTERS.add(2411718391L);
    }

    private MChat() {
        super(new JvmPluginDescriptionBuilder("onine.afeibaili.mchat", "2.1.0").name("MChat").author("AfeiBaili").build());
    }


    @Override
    public void onEnable() {
        logger = getLogger();
        //监听器加载
        Listener.load();
        //网络连接加载
        SocketHandle.load();
        logger.info("MChat加载成功");
    }
}