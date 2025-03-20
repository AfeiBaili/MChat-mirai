package online.afeibaili;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class SocketHandle {
    private static final Server server = new Server(33393);

    public static void load() {
        server.connection();
    }

    public static void send(String message) {
        server.sockets.forEach(socket -> {
            try {
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter writer = new BufferedWriter(osw);

                writer.write(message + "\n");
                writer.flush();
            } catch (IOException e) {
                server.sockets.remove(socket);
            }
        });
    }


    private static class Server implements AutoCloseable {
        ServerSocket server;
        Socket socket;
        Set<Socket> sockets = new HashSet<>();

        public Server(int port) {
            try {
                this.server = new ServerSocket(port);
                Message.sendToGroup("服务器开启成功！");
            } catch (IOException e) {
                Message.sendToGroup("服务器开启错误：" + e.getMessage());
            }
        }

        private void initReceiveMessage() {
            new Thread(() -> {
                try (
                        InputStream is = socket.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader reader = new BufferedReader(isr)
                ) {
                    String message;
                    if (!reader.readLine().equals("校验050516")) close();
                    while ((message = reader.readLine()) != null) {
                        Message.sendToGroup(message);
                    }
                    Message.sendToGroup("对方断开连接！");
                } catch (IOException e) {
                    Message.sendToGroup("对方异常断开连接！" + e.getMessage());
                }
            }, "reader").start();
        }


        public void connection() {
            new Thread(() -> {
                try {
                    while (true) {
                        socket = server.accept();
                        Message.sendToGroup("连接成功：" + socket.getRemoteSocketAddress());

                        sockets.add(socket);
                        initReceiveMessage();
                    }
                } catch (IOException e) {
                    Message.sendToGroup("建立连接失败：" + e.getMessage());
                    close();
                }
            }, "connection").start();
        }

        public ServerSocket getServer() {
            return server;
        }

        public void setServer(ServerSocket server) {
            this.server = server;
        }

        public Socket getSocket() {
            return socket;
        }

        public void setSocket(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void close() {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                Message.sendToGroup("服务器关闭时错误：" + e.getMessage());
            }
        }
    }
}
