package online.afeibaili;

import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class Listener {
    public static void load() {
        groupListener();
        onlineListener();
    }

    public static void groupListener() {
        GlobalEventChannel.INSTANCE.filter(event -> {
            if (event instanceof GroupEvent) {
                GroupEvent groupEvent = (GroupEvent) event;
                return MChat.GROUPS.contains(groupEvent.getGroup().getId());
            }
            return false;
        }).subscribeAlways(GroupMessageEvent.class, event -> {
            String message = event.getMessage().contentToString();
            String name = event.getSender().getNick();
            if (message.charAt(0) == '!') {
                //todo 命令部分
            }
            Message.sendToMC(event.getGroup().getName() + " " + name + "：" + message);
        });
    }

    public static void onlineListener() {
        GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, botOnlineEvent -> {
            MChat.bot = botOnlineEvent.getBot();
        });
    }
}
