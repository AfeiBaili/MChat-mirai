package online.afeibaili;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class MChatSocket {
    private Selector selector;

    public MChatSocket() {
        load();
    }

    public void unload() {
        try {
            selector.keys().forEach(selectionKey -> {
                try {
                    selectionKey.channel().close();
                } catch (IOException e) {
                    MChat.logger.info("管道关闭时异常！");
                }
            });
            selector.close();
        } catch (IOException e) {
            MChat.logger.info("选择器关闭时异常！");
        }
    }

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
                        try {
                            if (key.isAcceptable()) {
                                SocketChannel socket = serverSocket.accept();
                                socket.configureBlocking(false);
                                socket.register(selector, SelectionKey.OP_READ);
                                MChat.send("MChat成功连接至服务器" + socket.getRemoteAddress());
                            }
                            if (key.isReadable()) {
                                SocketChannel channel = (SocketChannel) key.channel();
                                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                StringBuilder stringBuffer = new StringBuilder();
                                try {
                                    int len;
                                    while ((len = channel.read(byteBuffer)) > 0) {
                                        byteBuffer.flip();
                                        stringBuffer.append(StandardCharsets.UTF_8.decode(byteBuffer));
                                        byteBuffer.clear();
                                    }
                                    if (len == -1) {
                                        key.cancel();
                                        channel.close();
                                        MChat.send("远程断开连接，正在重启服务端");
                                        unload();
                                        load();
                                    }
                                    String message = stringBuffer.toString();
                                    if (!message.equals("💓")) {
                                        MChat.send(message);
                                    }
                                } catch (IOException e) {
                                    key.cancel();
                                    MChat.send("远程已断开连接！");
                                }
                            }
                        } catch (IOException e) {
                            MChat.send("服务器开启异常，请重试");
                        }
                    });
                    selector.selectedKeys().clear();

                }
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException("线程内错误！" + e);
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
                        MChat.send("MChat客户端已断开连接");
                        socketChannel.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        MChat.logger.info("管道关闭时异常！");
                    }
                    key.cancel();
                }
            }
        });
    }
}