module uet.np.tictactoeclientui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;

    opens uet.np.tictactoeclientui to javafx.fxml;
    exports uet.np.tictactoeclientui;
}