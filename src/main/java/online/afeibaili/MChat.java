package online.afeibaili;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;

import java.util.ArrayList;
import java.util.List;

public final class MChat extends JavaPlugin {
    public static final MChat INSTANCE = new MChat();
    public static final List<Long> GROUPS = new ArrayList<>();
    public static final List<Long> MASTERS = new ArrayList<>();
    private static final StringBuffer STRING_BUFFER = new StringBuffer();
    public static MChatSocket server = new MChatSocket();
    public static MiraiLogger logger;
    private static Bot bot;
    private static Boolean isClose = false;

    static {
        GROUPS.add(975709430L);
        MASTERS.add(2411718391L);
    }

    private MChat() {
        super(new JvmPluginDescriptionBuilder("onine.afeibaili.mchat", "1.2.0").name("MChat").author("AfeiBaili").build());
    }

    /**
     * 将消息发送到QQ群聊
     *
     * @param message 要发送的消息
     */
    public static void send(String message) {
        if (!isClose) {
            GROUPS.forEach(group -> {
                Group g = bot.getGroup(group);
                if (g != null) {
                    g.sendMessage(message);
                }
            });
        }
    }

    @Override
    public void onEnable() {
        logger = getLogger();
        listener();
    }

    public void listener() {
        GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, e -> {
            bot = e.getBot();
        });
        GlobalEventChannel.INSTANCE.filter(event -> {
            if (event instanceof GroupEvent) {
                GroupMessageEvent groupEvent = (GroupMessageEvent) event;
                return GROUPS.contains(groupEvent.getGroup().getId()) && MASTERS.contains(groupEvent.getSender().getId());
            }
            return false;
        }).subscribeAlways(GroupMessageEvent.class, e -> {
            String message = e.getMessage().contentToString();
            Group send = e.getSubject();
            if (message.charAt(0) == '/') {
                String[] strings = message.split(" ");
                switch (strings[0]) {
                    case "/菜单":
                        send.sendMessage("""
                                /m-查看主人
                                /m-添加主人
                                /m-删除主人
                                /m-查看群
                                /m-添加群
                                /m-删除群
                                /m-重新加载
                                /m-开启聊天
                                /m-关闭聊天"""
                        );
                        break;
                    case "/m-查看主人":
                        MASTERS.forEach(m -> STRING_BUFFER.append(m).append("\n"));
                        STRING_BUFFER.delete(STRING_BUFFER.length() - 1, STRING_BUFFER.length());
                        send.sendMessage(STRING_BUFFER.toString());
                        STRING_BUFFER.delete(0, STRING_BUFFER.length());
                        break;
                    case "/m-查看群":
                        GROUPS.forEach(g -> STRING_BUFFER.append(g).append("\n"));
                        STRING_BUFFER.delete(STRING_BUFFER.length() - 1, STRING_BUFFER.length());
                        send.sendMessage(STRING_BUFFER.toString());
                        STRING_BUFFER.delete(0, STRING_BUFFER.length());
                        break;
                    case "/m-添加主人":
                        try {
                            MASTERS.add(Long.parseLong(strings[1]));
                            send.sendMessage("添加成功");
                        } catch (NumberFormatException ex) {
                            send.sendMessage("QQ格式不对, " + ex.getMessage());
                        }
                        break;
                    case "/m-删除主人":
                        try {
                            if (Long.parseLong(strings[1]) == 2411718391L) {
                                send.sendMessage("无法删除阿飞");
                                break;
                            }
                            MASTERS.remove(Long.parseLong(strings[1]));
                            send.sendMessage("删除成功");
                        } catch (NumberFormatException ex) {
                            send.sendMessage("QQ格式不对, " + ex.getMessage());
                        }
                        break;
                    case "/m-添加群":
                        try {
                            GROUPS.add(Long.parseLong(strings[1]));
                            send.sendMessage("添加成功");
                        } catch (NumberFormatException ex) {
                            send.sendMessage("QQ群格式不对, " + ex.getMessage());
                        }
                        break;
                    case "/m-删除群":
                        try {
                            if (Long.parseLong(strings[1]) == 975709430L) {
                                send.sendMessage("无法删除主群");
                                break;
                            }
                            GROUPS.remove(Long.parseLong(strings[1]));
                            send.sendMessage("删除成功");
                        } catch (NumberFormatException ex) {
                            send.sendMessage("QQ群格式不对, " + ex.getMessage());
                        }
                        break;
                    case "/m-关闭聊天":
                        isClose = true;
                        send.sendMessage("已关闭MChat");
                        break;
                    case "/m-开启聊天":
                        send.sendMessage("已开启MChat");
                        isClose = false;
                        break;
                    case "/m-重新加载":
                        server.unload();
                        server = new MChatSocket();
                        send.sendMessage("已重新加载");
                        break;
                }
            }
        });

        //要发送到MC的监听器
        GlobalEventChannel.INSTANCE.filter(event -> {
            if (event instanceof GroupEvent) {
                GroupMessageEvent groupEvent = (GroupMessageEvent) event;
                return GROUPS.contains(groupEvent.getGroup().getId());
            }
            return false;
        }).subscribeAlways(GroupMessageEvent.class, e -> {
            server.send(e.getGroup().getName() + " " + e.getSender().getNick() + ": " + e.getMessage().contentToString());
        });
    }
}