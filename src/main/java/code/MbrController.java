package code;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class MbrController {
    @FXML
    private TextField txt_mbrDetEmail;
    @FXML
    private Text txt_mbrDetLbl, txt_mbrDetName, txt_mbrDetUcid, txt_mbrDetSince;
    @FXML
    private DatePicker pick_mbrDetSince, pick_mbrDetTill;
}
