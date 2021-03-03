const moment = require("moment");

const clearSession = () => {
  // Clear the session
  pm.environment.unset("currentAccessToken");
  pm.environment.unset("startDate");
  pm.environment.unset("startDateTime ");
  pm.environment.unset("endDate");
  pm.environment.unset("endDateTime");
};

const obtainToken = () => {
  console.log("Obtaining new access token");
  pm.sendRequest(tokenPostRequest, function (err, res) {
    console.log(err ? err : res.json());
    if (err === null) {
      console.log("Saving the token");
      var responseJson = res.json();
      pm.environment.set("currentAccessToken", responseJson.access_token);
    }
  });
};

const addDateTimesToEnvironment = () => {
  // Add formatted date and datetime strings into the environment
  const dateFormat = "YYYY-MM-DD";
  const dateTimeFormat = "YYYY-MM-DDTss:mm:hh";
  const start = moment();
  const end = moment().add(7, "days");
  const startDate = start.format(dateFormat);
  const startDateTime = start.startOf("day").format(dateTimeFormat);
  const endDate = end.format(dateFormat);
  const endDateTime = end.startOf("day").format(dateTimeFormat);

  pm.environment.set("startDate", startDate);
  pm.environment.set("endDate", endDate);
  pm.environment.set("startDateTime", startDateTime);
  pm.environment.set("endDateTime", endDateTime);
};

const authString = `${pm.environment.get("oauth2Client")}:${pm.environment.get(
  "oauth2Secret"
)}`;

const tokenPostRequest = {
  url:
    pm.collectionVariables.get("oauth2Url") + "?grant_type=client_credentials",
  method: "POST",
  header: [
    "Content-Type:application/json",
    "Authorization: Basic " +
      CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(authString)),
  ],
};

// run
clearSession();
obtainToken();
addDateTimesToEnvironment();
