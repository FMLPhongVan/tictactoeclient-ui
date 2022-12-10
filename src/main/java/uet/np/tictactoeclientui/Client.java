package uet.np.tictactoeclientui;

import javafx.application.Platform;
import uet.np.tictactoeclientui.bot.AI;
import uet.np.tictactoeclientui.packet.Packet;
import uet.np.tictactoeclientui.packet.PacketService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client {
    public boolean isUsingGui = false;
    public static String address = "127.0.0.1";
    //public static int port = 16433;
    //public static String address = "s.vominhduc.me";
    public int port = 9000;
    public String KEY_MATCH = "123";
    public int UID = 44403;

    public void handle() {
        if (isUsingGui) Platform.runLater(() -> Controller.getInstance().setMatchInfo(port, UID));
        System.out.println("Port: " + port);
        System.out.println("UID: " + UID);
        System.out.println("KEY_MATCH: " + KEY_MATCH);
        try (Socket socket = new Socket(address, port)) {
            System.out.println("Connected to server");
            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("Connected to server"));
            DataInputStream din = new DataInputStream(socket.getInputStream());
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());

            // Send packet PKT_HI
            Packet hiPacket = PacketService.initHiPacket(KEY_MATCH, UID);
            dout.write(PacketService.turnPacketToBytes(hiPacket), 0, hiPacket.getSize());
            System.out.println("Send packet Hi");
            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("Send packet Hi"));
            int id = 0, l;
            boolean goFirst = true;
            boolean readyToPlay = false;
            AI ai = new AI();
            byte[] buffer = new byte[1000];

            boolean running = true;
            while (running) {
                int recvBytes = din.read(buffer);
                System.out.println("Received " + recvBytes + " bytes");
                if (recvBytes <= 0) {
                    break;
                }

                int currentPos = 0;
                while (true) {
                    currentPos += 4;
                    int len = Utils.convertByteArrayToInt(buffer, currentPos, true);

                    Packet recvPacket = PacketService.turnBytesToPacket(buffer, currentPos - 4);
                    switch (recvPacket.type) {
                        case PKT_HI:
                            System.out.println("Received PKT_HI");
                            break;
                        case PKT_ID:
                            id = Utils.convertByteArrayToInt(recvPacket.data, 0);
                            goFirst = Utils.convertByteArrayToInt(recvPacket.data, 4) == 1;
                            System.out.println("Received PKT_ID");
                            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("Received PKT_ID"));
                            System.out.println("ID: " + id);
                            System.out.println("You go first ?: " + goFirst);
                            if (isUsingGui) {
                                boolean finalGoFirst = goFirst;
                                Platform.runLater(() -> Controller.getInstance().setIsX(finalGoFirst));
                            }
                            break;
                        case PKT_BOARD:
                            ai.n = Utils.convertByteArrayToInt(recvPacket.data, 0);
                            ai.m = Utils.convertByteArrayToInt(recvPacket.data, 4);
                            ai.initBoard();
                            l = Utils.convertByteArrayToInt(recvPacket.data, 8);
                            ai.lengthToWin = Utils.convertByteArrayToInt(recvPacket.data, 12);
                            System.out.println("Received PKT_BOARD");
                            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("Received PKT_BOARD"));
                            System.out.println("n: " + ai.n);
                            System.out.println("m: " + ai.m);
                            System.out.println("lengthToWin: " + ai.lengthToWin);
                            System.out.println("Blocked: " + l);
                            for (int i = 0; i < l; i++) {
                                int blocked = Utils.convertByteArrayToInt(recvPacket.data, 16 + i * 4);
                                ai.board[blocked / ai.n][blocked % ai.n] = -1;
                                System.out.format("Blocked %d: %d %d\n", blocked, blocked / ai.n, blocked % ai.n);
                            }
                            if (isUsingGui) {
                                int finalL = l;
                                Platform.runLater(() -> Controller.getInstance().setBoard(ai.m, ai.n, ai.lengthToWin, finalL));
                            }

                            readyToPlay = true;

                            if (goFirst) {
                                int move = (int) (Math.random() * (ai.n * ai.m));
                                while (ai.board[move / ai.n][move % ai.n] != 0) {
                                    move = (int) (Math.random() * (ai.n * ai.m));
                                }

                                ai.board[move / ai.n][move % ai.n] = 1;
                                System.out.format("My move: %d %d\n", move / ai.n, move % ai.n);

                                Packet sendPacket = PacketService.initSendPacket(id, move);
                                dout.write(PacketService.turnPacketToBytes(sendPacket), 0, sendPacket.getSize());
                                goFirst = false;
                            }

                            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().updateBoard(ai.board));
                            ai.printBoard();

                            break;
                        case PKT_RECEIVE:
                            System.out.println("Received PKT_RECEIVE");
                            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("Received PKT_RECEIVE"));
                            int move = Utils.convertByteArrayToInt(recvPacket.data, 0);
                            ai.board[move / ai.n][move % ai.n] = 2;

                            System.out.format("Opponent move: %d %d\n", move / ai.n, move % ai.n);
                            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().updateBoard(ai.board));
                            if (isUsingGui) {
                                int finalMove = move;
                                Platform.runLater(() -> Controller.getInstance().addLog("Opponent move: " + finalMove / ai.n + " " + finalMove % ai.n));
                            }
                            move = ai.nextMove();
                            System.out.format("My move: %d %d\n", move / ai.n, move % ai.n);
                            if (isUsingGui) {
                                int finalMove = move;
                                Platform.runLater(() -> Controller.getInstance().addLog("My move: " + finalMove / ai.n + " " + finalMove % ai.n));
                                Platform.runLater(() -> Controller.getInstance().updateBoard(ai.board));
                            }
                            ai.board[move / ai.n][move % ai.n] = 1;

                            if (readyToPlay) {
                                ai.printBoard();
                            }

                            Packet sendPacket = PacketService.initSendPacket(id, move);
                            dout.write(PacketService.turnPacketToBytes(sendPacket), 0, sendPacket.getSize());
                            break;
                        case PKT_ERROR:
                            System.out.println("Received PKT_ERROR");
                            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("Received PKT_ERROR"));
                            do {
                                move = (int) (Math.random() * (ai.n * ai.m));
                            } while (ai.board[move / ai.n][move % ai.n] != 0);
                            if (isUsingGui) {
                                int finalMove = move;
                                Platform.runLater(() -> Controller.getInstance().addLog("My move: " + finalMove / ai.n + " " + finalMove % ai.n));
                                Platform.runLater(() -> Controller.getInstance().updateBoard(ai.board));
                            }

                            sendPacket = PacketService.initSendPacket(id, move);
                            dout.write(PacketService.turnPacketToBytes(sendPacket), 0, sendPacket.getSize());
                            break;
                        case PKT_END:
                            System.out.println("Received PKT_END");
                            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("Received PKT_END"));
                            running = false;
                            int winnerId = Utils.convertByteArrayToInt(recvPacket.data, 0);
                            if (winnerId == id) {
                                System.out.println("You win");
                                if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("You win"));
                            } else if (winnerId == 0) {
                                System.out.println("Draw");
                                if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("Draw"));
                            } else {
                                System.out.println("You lose");
                                if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("You lose"));
                            }
                            break;
                        default:
                            System.out.println("Unknown packet type");
                            if (isUsingGui) Platform.runLater(() -> Controller.getInstance().addLog("Unknown packet type"));
                            running = false;
                            break;
                    }

                    if (recvBytes <= currentPos + len + 4) {
                        break;
                    }
                }
            }

            din.close();
            dout.close();

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> Controller.getInstance().addLog(e.getMessage()));
        }
    }
}
