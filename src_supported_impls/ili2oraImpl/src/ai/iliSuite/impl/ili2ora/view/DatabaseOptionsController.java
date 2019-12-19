package ai.iliSuite.impl.ili2ora.view;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import ai.iliSuite.impl.controller.IController;
import ai.iliSuite.impl.dbconn.AbstractConnection;
import ai.iliSuite.impl.ili2ora.EnumIli2ora;
import ai.iliSuite.impl.ili2ora.EnumOraConnType;
import ai.iliSuite.util.KeyValuePair;
import ai.iliSuite.view.util.navigation.ResourceUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;

public class DatabaseOptionsController implements IController, Initializable {
	@FXML
	private ResourceBundle applicationBundle;

	@FXML
	private TextField txt_host;
	@FXML
	private TextField txt_port;
	@FXML
	private ComboBox<KeyValuePair<EnumOraConnType, String>> cbxConnectionType;
	@FXML
	private TextField txt_databaseName;
	@FXML
	private TextField txt_databaseSchema;
	@FXML
	private TextField txt_user;
	@FXML
	private PasswordField txt_password;
	@FXML
	private Text lbl_connectionResult;

	private AbstractConnection connection;

	private List<Node> listOfRequired;

	private boolean createSchema;
	private Parent viewRootNode;

	public DatabaseOptionsController(AbstractConnection connection, boolean createSchema) throws IOException {
		this.connection = connection;
		this.createSchema = createSchema;
		
		String strBundlePath = "ai.iliSuite.impl.ili2ora.resources.application";
		String strResourcePath = "/ai/iliSuite/impl/ili2ora/view/DatabaseOptions.fxml";
		// TODO Posible carga de componentes antes de ser necesario
		viewRootNode = ResourceUtil.loadResource(strResourcePath, strBundlePath, this);
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		applicationBundle = arg1;

		listOfRequired = new ArrayList<>();

		listOfRequired.add(txt_databaseName);
		
		txt_host.setPromptText(applicationBundle.getString("default.database.host"));
		txt_port.setPromptText(applicationBundle.getString("default.database.port"));
		@SuppressWarnings("unchecked")
		ObservableList<KeyValuePair<EnumOraConnType, String>> items = FXCollections.observableArrayList(
				new KeyValuePair<EnumOraConnType, String>(EnumOraConnType.SID, "SID"),
				new KeyValuePair<EnumOraConnType, String>(EnumOraConnType.SERVICE, "Service Name"));
		cbxConnectionType.getItems().addAll(items);
		cbxConnectionType.getSelectionModel().selectFirst();
	}

	@Override
	public Map<String, String> getParams() {

		Map<String, String> result = null;

		if (validateRequiredFields()) {
			String host = txt_host.getText() != null && !txt_host.getText().isEmpty() ? txt_host.getText() : null;
			String port = txt_port.getText() != null && !txt_port.getText().isEmpty() ? txt_port.getText() : null;
			String databaseName = txt_databaseName.getText() != null && !txt_databaseName.getText().isEmpty()
					? txt_databaseName.getText() : null;
			String databaseSchema = txt_databaseSchema.getText() != null && !txt_databaseSchema.getText().isEmpty() ? txt_databaseSchema.getText() : null;
			String user = txt_user.getText() != null && !txt_user.getText().isEmpty() ? txt_user.getText() : null;
			String pass = txt_password.getText() != null && !txt_password.getText().isEmpty() ? txt_password.getText()
					: null;
			boolean isService = cbxConnectionType.getValue().getKey() == EnumOraConnType.SERVICE;

			Map<String, String> params = new HashMap<String, String>();

			// TODO parametros en enumeracion
			params.put("host", host);
			params.put("port", port);
			if(!isService)
				params.put("databaseName", databaseName);
			else
				params.put("dbservice", databaseName);
			params.put("databaseSchema", databaseSchema);
			params.put("user", user);
			params.put("password", pass);

			connection.setConnectionParams(params);

			boolean validConnection = false;
			try {
				validConnection = connection.isValid();
				if(databaseSchema!=null && !createSchema)
					validConnection = validConnection && connection.checkSchema(databaseSchema);
			} catch (ClassNotFoundException | SQLException e) {

				lbl_connectionResult.setText(e.getLocalizedMessage());
				validConnection = false;
			} finally {
				if (validConnection) {
					result = new HashMap<String, String>();

					if (host != null)
						result.put(EnumIli2ora.DB_HOST.getName(), host);

					if (port != null)
						result.put(EnumIli2ora.DB_PORT.getName(), port);

					if (databaseName != null) {
						if(!isService)
							result.put(EnumIli2ora.DB_DATABASE.getName(), databaseName);
						else
							result.put(EnumIli2ora.DB_SERVICE.getName(), databaseName);
					}
					if(databaseSchema!=null)
						result.put(EnumIli2ora.DB_SCHEMA.getName(), databaseSchema);
					if (user != null)
						result.put(EnumIli2ora.DB_USER.getName(), user);

					if (pass != null)
						result.put(EnumIli2ora.DB_PWD.getName(), pass);
				}
			}
		}
		return result;
	}

	protected boolean validateRequiredFields() {
		boolean toValid = true;
		for (Node n : listOfRequired) {
			if (n instanceof TextField) {
				if (((TextField) n).getText().isEmpty()) {
					((TextField) n).setStyle("-fx-text-box-border: red ; -fx-focus-color: red ;");
					((TextField) n).setTooltip(new Tooltip(applicationBundle.getString("general.required")));
					((TextField) n).setOnKeyReleased(event -> {
						((TextField) n).setStyle(null);
						((TextField) n).setTooltip(null);
					});
					toValid = false;
				}
			}
		}
		return toValid;
	}
	
	@Override
	public Parent getGraphicComponent() {
		return viewRootNode;
	}
}
