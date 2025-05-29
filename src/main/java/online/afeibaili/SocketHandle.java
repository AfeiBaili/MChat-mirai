package online.afeibaili;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import static online.afeibaili.MChat.logger;

public class SocketHandle {
    private static final Server server = new Server(33393);

    public static void load() {
        //进行连接
        server.connection();
    }

    /**
     * 将消息发送至MC
     *
     * @param message 消息
     */
    public static void send(String message) {
        server.sockets.forEach(socket -> {
            try {
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                PrintWriter writer = new PrintWriter(osw);
                writer.println(message);
                writer.flush();
            } catch (IOException e) {
                server.sockets.remove(socket);
                logger.info("消息发送失败！" + e.getMessage());
            }
        });
    }


    private static class Server implements AutoCloseable {
        //服务器
        ServerSocket server;
        Socket socket;
        Set<Socket> sockets = new HashSet<>();

        public Server(int port) {
            try {
                this.server = new ServerSocket(port);
                logger.info("服务器开启成功！");
            } catch (IOException e) {
                logger.info("服务器开启错误：" + e.getMessage());
            }
        }


        public void connection() {
            //始终开启连接线程
            new Thread(() -> {
                try {
                    while (true) {
                        socket = server.accept();

                        //进行连接校验
                        if (!checkConnection()) {
                            close();
                            continue;
                        }

                        //存入连接列表
                        sockets.add(socket);
                        //创建只读线程
                        initReceiveMessage();
                        logger.info("连接成功：" + socket.getRemoteSocketAddress().toString().split(":")[1]);
                    }
                } catch (IOException e) {
                    logger.info("建立连接失败：" + e.getMessage());
                    close();
                }
            }, "connection").start();
        }

        private void initReceiveMessage() {
            new Thread(() -> {
                try {
                    socket.setSoTimeout(0);
                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr);

                    String message;
                    while ((message = reader.readLine()) != null) {
                        Message.sendToGroup(message);
                    }
                    logger.info("对方断开连接！");
                } catch (IOException e) {
                    logger.info("对方异常断开连接！" + e.getMessage());
                }
            }, "reader").start();
        }

        public boolean checkConnection() {
            try {
                socket.setSoTimeout(5000);
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(isr);

                if (reader.readLine().equals("校验050516")) return true;
            } catch (IOException ignored) {
                //校验失败处理
                logger.info("连接校验失败");
            }
            return false;
        }

        @Override
        public void close() {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                Message.sendToGroup("服务器关闭时错误：" + e.getMessage());
            }
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
    }
}
