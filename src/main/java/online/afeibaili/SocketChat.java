package online.afeibaili;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class SocketChat {
    public static Boolean IS_TRUE = true;
    public static Selector selector;

    static {
        Thread thread = new Thread("MChatSocketServer") {
            @Override
            public void run() {
                ServerSocketChannel serverSocketChannel = null;
                try {
                    StringBuilder message = new StringBuilder();
                    selector = Selector.open();
                    serverSocketChannel = ServerSocketChannel.open();
                    serverSocketChannel.bind(new InetSocketAddress(33393));
                    serverSocketChannel.configureBlocking(false);
                    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                    while (IS_TRUE) {
                        selector.select();
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();

                            if (key.isAcceptable()) {
                                SocketChannel socket = serverSocketChannel.accept();
                                socket.configureBlocking(false);
                                socket.register(selector, SelectionKey.OP_READ);
                            }
                            if (key.isReadable()) {
                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                ByteBuffer buffer = ByteBuffer.allocate(1024);
                                try {
                                    while (socketChannel.read(buffer) > 0) {
                                        buffer.flip();
                                        message.append(StandardCharsets.UTF_8.decode(buffer));
                                        buffer.clear();
                                    }
                                } catch (IOException e) {
                                    key.cancel();
                                    socketChannel.close();
                                    continue;
                                }
                                if (message.toString().equals("—-√💓---")) {
                                    message.delete(0, message.length());
                                    continue;
                                }
                                MChat.send(message.toString());
                                message.delete(0, message.length());
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        if (serverSocketChannel != null) serverSocketChannel.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        };
        thread.start();
    }

    public static void init() {
        MChat.logger.info("MChat加载成功!");
    }

    //群聊发送到MC
    public static void send(String message) {
        Set<SelectionKey> keys = selector.keys();
        keys.forEach(key -> {
            SelectableChannel channel = key.channel();
            if (channel instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) channel;
                try {
                    socketChannel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    try {
                        socketChannel.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    key.channel();
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
