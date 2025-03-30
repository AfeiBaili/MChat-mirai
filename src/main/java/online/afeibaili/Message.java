package online.afeibaili;

import net.mamoe.mirai.contact.Group;

import java.util.Optional;


public class Message {
    public static void sendToGroup(String message) {
        if (message.trim().isEmpty()) return;
        if (MChat.bot != null) {
            MChat.GROUPS.forEach(gl -> {
                Optional<Group> group = Optional.ofNullable(MChat.bot.getGroup(gl));
                group.ifPresent(gr -> gr.sendMessage(message));
            });
        }
    }

    public static void sendToMC(String message) {
        SocketHandle.send(message);
    }
}
