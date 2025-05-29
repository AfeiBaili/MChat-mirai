package online.afeibaili;

import java.util.Optional;


public class Message {
    public static void sendToGroup(String message) {
        if (message.trim().isEmpty()) return;
        if (MChat.bot != null) {
            MChat.GROUPS.forEach(gl -> {
                Optional.ofNullable(MChat.bot.getGroup(gl)).ifPresent(group ->
                        group.sendMessage(message));
            });
        }
    }

    public static void sendToMC(String message) {
        SocketHandle.send(message);
    }
}
