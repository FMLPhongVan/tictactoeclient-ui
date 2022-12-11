package uet.np.tictactoeclientui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public Label lPort;
    public Label lUID;
    public Label lState;
    public Label lIsX;
    public Label lSize;
    public Label lLengthTioWin;
    public Label lNumsOfBlocked;
    public Label lLogs;
    public GridPane gpBoard;
    public AnchorPane apBoardContainer;

    private int nrow;
    private int ncol;
    private int cellSize;
    private boolean isX = true;

    public void setMatchInfo(int port, int uid) {
        lPort.setText(String.format("Port: %d", port));
        lUID.setText(String.format("UID: %d", uid));
        lState.setText("State: Waiting board from server");
        /*//setBoard(5, 10, 3, 4);
        updateBoard(new int[][] {
                {0, 1, 2, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 2, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 2, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 2, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 2, 1, 1, 1, 1, 1, 1, 1},
        });*/
    }

    public void addLog(String log) {
        List<String> lines =  new ArrayList<>(List.of(lLogs.getText().split("\n")));
        // add date to log and add to lines
        LocalDateTime now = LocalDateTime.now();
        String datetime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        lines.add(datetime + ": " + log);
        lLogs.setText(String.join("\n", lines));
        // get current date time
    }

    public void setBoard(int m, int n, int l, int k) {
        lSize.setText(String.format("Size: %d x %d", m, n));
        lLengthTioWin.setText(String.format("Length to win: %d", l));
        lNumsOfBlocked.setText(String.format("Number of blocked: %d", k));
        lState.setText("State: Playing");

        nrow = m;
        ncol = n;

        gpBoard = new GridPane();
        gpBoard.setStyle("-fx-background-color: #000000");
        apBoardContainer.getChildren().add(gpBoard);
        gpBoard.setMaxWidth(apBoardContainer.getWidth());
        gpBoard.setMaxHeight(apBoardContainer.getHeight());
        cellSize = Math.min((int) apBoardContainer.getWidth() / ncol, (int) apBoardContainer.getHeight() / nrow);
        gpBoard.setPrefSize(n * cellSize, m * cellSize);
        gpBoard.setLayoutX((apBoardContainer.getWidth() - gpBoard.getPrefWidth()) / 2);
        gpBoard.setLayoutY((apBoardContainer.getHeight() - gpBoard.getPrefHeight()) / 2);
        gpBoard.setGridLinesVisible(false);
        gpBoard.setHgap(5);
        gpBoard.setVgap(5);
        // init size of gridpane
        //gpBoard.setPrefSize(30 * m, 30 * n);
        gpBoard.setGridLinesVisible(true);
        // add m * n cells to gridpane
        for (int i = 0; i < m; i++) {
            Node[] row = new AnchorPane[n];
            for (int j = 0; j < n; j++) {
                row[j] = getEmptyCell();
            }
            gpBoard.addRow(i, row);
        }
    }

    public void updateBoard(int x, int y, int val) {
        if (val == 1) {
            if (isX) gpBoard.add(getXCell(),y, x);
            else gpBoard.add(getOCell(), y, x);
        } else if (val == 2) {
            if (isX) gpBoard.add(getOCell(),y, x);
            else gpBoard.add(getXCell(), y, x);
        } else if(val == -1) {
            gpBoard.add(getEmptyCell(), y, x);
        }
    }

    public void setIsX(boolean isX) {
        this.isX = isX;
        lIsX.setText("You are: " + (isX ? "X" : "O"));
    }

    private AnchorPane getEmptyCell() {
        AnchorPane cell = new AnchorPane();
        cell.setPrefWidth(cellSize);
        cell.setPrefHeight(cellSize);
        // add stylesheet to cell
        cell.getStylesheets().add(Objects.requireNonNull(getClass().getResource("gridpane.css")).toExternalForm());
        cell.getStyleClass().add("box");
        cell.getStyleClass().add("empty");
        return cell;
    }

    private AnchorPane getBlockedCell() {
        AnchorPane cell = getEmptyCell();
        cell.getStyleClass().remove("empty");
        cell.getStyleClass().add("blocked");
        return cell;
    }

    private FontIcon getIcon(String iconCode) {
        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize((int) (0.6 * cellSize));
        icon.getStyleClass().add("box");
        icon.setWrappingWidth(cellSize - 5);
        icon.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        return icon;
    }

    private AnchorPane getXCell() {
        AnchorPane cell = getEmptyCell();
        cell.getStyleClass().remove("empty");
        cell.getStyleClass().add("player-1");

        FontIcon icon = getIcon("fa-times");
        //icon.setX((cellSize - icon.getIconSize()) / 2.0);
        icon.setY(cellSize - icon.getIconSize() / 2.0);
        cell.getChildren().add(icon);
        return cell;
    }

    private AnchorPane getOCell() {
        AnchorPane cell = getEmptyCell();
        cell.getStyleClass().remove("empty");
        cell.getStyleClass().add("player-2");

        FontIcon icon = getIcon("fa-circle-o");
        cell.getChildren().add(icon);
        //icon.setX(icon.getIconSize() / 2.0);
        icon.setY(cellSize - icon.getIconSize() / 2.0);
        return cell;
    }

    private static Controller instance = null;

    public static Controller getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
    }
}