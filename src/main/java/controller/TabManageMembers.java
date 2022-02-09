package controller;

import com.google.cloud.Timestamp;
import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Duration;
import model.MemberRecord;
import util.MemberRecordCell;
import util.StateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabManageMembers extends VBox {

    @FXML
    TextField addMemberName;
    @FXML
    TextField addMemberIdentifier;
    @FXML
    TextField addMemberTelegramId;
    @FXML
    Button addMemberButton;
    @FXML
    BorderPane manageMemberListContainer;
    static final ListView<MemberRecord> memberListView = new ListView<>();

    private final PseudoClass errorClass = PseudoClass.getPseudoClass("error");
    IFirebaseDB firebaseDB = new FirebaseDB();

    public TabManageMembers() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/tab_manage_members.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Set Telegram ID field to only accept numbers
        addMemberTelegramId.setTextFormatter(new TextFormatter<Object>(c -> {
            if (!c.getControlNewText().matches("\\d*"))
                return null;
            else
                return c;
        }));
        addMemberButton.setOnAction(event -> onAddMember());
        populateMemberListView();
    }

    public void onAddMember() {
        if (validateInputs()) {
            String name = addMemberName.getText().trim();
            String telegramId = addMemberTelegramId.getText().trim();
            String identifier = addMemberIdentifier.getText().trim();
            if (identifier.isEmpty() || identifier == null) {
                identifier = "";
            }
            // Now add member to firebase as well as to list view
            try {
                MemberRecord memberRecord = new MemberRecord(name, identifier, telegramId);
                Timestamp timestamp = firebaseDB.addMember(StateManager.getCurrentOrg(), memberRecord);
                if (timestamp != null) {
                    System.out.println("Created new member for organisation: " + StateManager.getCurrentOrg() + " at " + timestamp);
                    updateMemberListView(memberRecord, true);
                    createSuccessfulMemberAdditionDialog().show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void populateMemberListView() {
        // Get records from firebase
        List<MemberRecord> records = new ArrayList<>();
        try {
            records = firebaseDB.getMemberRecords(StateManager.getCurrentOrg());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now display
        ObservableList<MemberRecord> list = FXCollections.observableArrayList(records);
        memberListView.setItems(list);
        memberListView.setCellFactory(new Callback<ListView<MemberRecord>, ListCell<MemberRecord>>() {
            @Override
            public ListCell<MemberRecord> call(ListView<MemberRecord> param) {
                return new MemberRecordCell();
            }
        });
        manageMemberListContainer.setCenter(memberListView);
    }

    public static void updateMemberListView(MemberRecord newMember, Boolean add) {
        ObservableList<MemberRecord> list = memberListView.getItems();
        if (add) {
            list.add(newMember);
        } else {
            list.remove(newMember);
        }
        memberListView.setItems(list);
    }

    private Boolean validateInputs() { // Identifier is optional
        String name = addMemberName.getText().trim();
        String telegramId = addMemberTelegramId.getText().trim();
        Tooltip nameTooltip = new Tooltip(), telegramIdTooltip = new Tooltip();
        nameTooltip.setShowDelay(Duration.millis(100));
        telegramIdTooltip.setShowDelay(Duration.millis(100));

        Boolean nameValid = false, telegramIdValid = false;
        if (name.trim().isEmpty()) {
            addMemberName.pseudoClassStateChanged(errorClass, true);
            nameTooltip.setText("Name must not be empty");
            addMemberName.setTooltip(nameTooltip);
        } else {
            addMemberName.pseudoClassStateChanged(errorClass, false);
            addMemberName.setTooltip(null);
            nameValid = true;
        }
        if (telegramId.trim().isEmpty()) {
            addMemberTelegramId.pseudoClassStateChanged(errorClass, true);
            telegramIdTooltip.setText("Telegram ID must not be empty");
            addMemberTelegramId.setTooltip(telegramIdTooltip);
        } else {
            addMemberTelegramId.pseudoClassStateChanged(errorClass, false);
            addMemberTelegramId.setTooltip(null);
            telegramIdValid = true;
        }
        return (nameValid && telegramIdValid);
    }

    private Dialog createSuccessfulMemberAdditionDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderText("Success");
        dialog.setContentText("Member successfully added!");
        ButtonType btn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(btn);
        return dialog;
    }
}
