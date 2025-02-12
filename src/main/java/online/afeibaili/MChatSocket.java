package online.afeibaili;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class MChatSocket {
    private Selector selector;

    public void load() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException("选择器IO异常！" + e);
        }
        Thread server = new Thread(() -> {
            ServerSocketChannel serverSocket;
            try {
                serverSocket = ServerSocketChannel.open();
                serverSocket.bind(new InetSocketAddress(33393));
                serverSocket.configureBlocking(false);
                serverSocket.register(selector, SelectionKey.OP_ACCEPT);

                while (selector.select() != 0) {
                    selector.selectedKeys().forEach(key -> {
                        if (key.isAcceptable()) {
                            try {
                                SocketChannel socket = serverSocket.accept();
                                socket.configureBlocking(false);
                                socket.register(selector, SelectionKey.OP_READ);
                                MChat.send("MChat连接至服务器：" + socket.getRemoteAddress());
                            } catch (IOException e) {
                                MChat.logger.info("管道注册时异常！");
                            }
                        }
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            StringBuilder stringBuffer = new StringBuilder();
                            try {
                                while (channel.read(byteBuffer) > 0) {
                                    byteBuffer.flip();
                                    stringBuffer.append(StandardCharsets.UTF_8.decode(byteBuffer));
                                    byteBuffer.clear();
                                }
                                String message = stringBuffer.toString();
                                if (!message.equals("💓")) {
                                    MChat.send(message);
                                }
                            } catch (IOException e) {
                                key.cancel();
                                try {
                                    channel.close();
                                } catch (IOException ex) {
                                    MChat.logger.info("管道关闭时异常！");
                                }
                                MChat.send("管道断开连接！");
                            }
                        }
                    });
                    selector.selectedKeys().clear();
                }
                selector.close();
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException("线程内错误！" + e);
            } finally {
                try {
                    selector.close();
                } catch (IOException e) {
                    MChat.logger.info("选择器关闭时异常！");
                }
            }
        }, "MChatSocketServer");
        server.start();
    }

    public void send(String message) {
        Set<SelectionKey> keys = selector.keys();
        keys.forEach(key -> {
            SelectableChannel channel = key.channel();
            if (channel instanceof SocketChannel) {
                SocketChannel socketChannel = (SocketChannel) channel;
                try {
                    socketChannel.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    try {
                        MChat.send("远程已断开连接：" + socketChannel.getRemoteAddress());
                        socketChannel.close();
                    } catch (IOException ex) {
                        MChat.logger.info("管道关闭时异常！");
                    }
                    key.cancel();
                }
            }
        });
    }
}