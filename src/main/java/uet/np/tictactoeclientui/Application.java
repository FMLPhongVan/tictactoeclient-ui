package uet.np.tictactoeclientui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    private static final Client client = new Client();
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("main-ui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        stage.setTitle("Tic tac toe Client");
        stage.setScene(scene);
        stage.show();

        new Thread(client::handle).start();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            client.isUsingGui = true;
            client.port = Integer.parseInt(args[args.length - 3]);
            client.UID = Integer.parseInt(args[args.length - 2]);
            client.KEY_MATCH = args[args.length - 1];
            if (args[0].equals("--gui")) {
                launch(args);
            }
            else client.handle();
        }
    }
}