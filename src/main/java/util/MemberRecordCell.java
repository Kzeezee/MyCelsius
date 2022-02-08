package util;

import com.google.cloud.Timestamp;
import controller.TabAddMember;
import firebase.Firebase;
import firebase.FirebaseDB;
import firebase.IFirebaseDB;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import model.MemberRecord;

import java.io.IOException;

import static util.Constants.SPACING_STYLE;

// Credits: https://stackoverflow.com/questions/15661500/javafx-listview-item-with-an-image-button
public class MemberRecordCell extends ListCell<MemberRecord> {
    HBox hbox = new HBox();
    Label name = new Label("John Doe");
    Label identifier = new Label("johntheman@gmail.com");
    Label telegramId = new Label("SAMPLEID");
    Pane pane = new Pane();
    Button button = new Button("Delete");

    public MemberRecordCell() {
        super();
        padContent();
        hbox.getChildren().addAll(name, identifier, telegramId, pane, button);
        hbox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(pane, Priority.ALWAYS);
    }

    @Override
    protected void updateItem(MemberRecord item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) { // <== test for null item and empty parameter
            name.setText(item.getName());
            identifier.setText(item.getIdentifier());
            telegramId.setText(item.getTelegramId());
            button.setOnAction(event -> createDeleteMemberWarningDialog(item));
            setGraphic(hbox);
        } else {
            setGraphic(null);
        }
    }

    private void createDeleteMemberWarningDialog(MemberRecord memberRecord) {
        Dialog dialog = new Dialog();
        dialog.setHeaderText("Warning");
        dialog.setContentText("Are you sure you want to delete " + name.getText().trim() + "?");
        ButtonType btn = new ButtonType("Confirm", ButtonBar.ButtonData.YES);
        dialog.getDialogPane().getButtonTypes().add(btn);
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btn);
        okBtn.setOnAction(event -> {
            try {
                // Delete member from firestore
                IFirebaseDB firebaseDB = new FirebaseDB();
                System.out.println("Executing deletion on member: " + memberRecord.getName());
                Timestamp timestamp = firebaseDB.deleteMember(StateManager.getCurrentOrg(), memberRecord);
                if (timestamp != null) {
                    System.out.println("Successfully deleted member: " + memberRecord.getName() + " at " + timestamp);
                    TabAddMember.updateMemberListView(memberRecord, false);
                } else {
                    throw new RuntimeException("Something went wrong when deleting member: " + memberRecord.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        dialog.show();
    }

    // Hardcoded styling for now
    private void padContent() {
        name.setStyle(SPACING_STYLE + "-fx-pref-width: 300; -fx-min-width: 300; -fx-max-width: 300;");
        identifier.setStyle(SPACING_STYLE + "-fx-pref-width: 250; -fx-min-width: 250; -fx-max-width: 250;");
    }
}
