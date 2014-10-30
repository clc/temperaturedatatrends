// Usage:
// 1. CONFIGURE THESE TO MATCH WHAT YOU HAVE!!!
var recipients = "MY_ACCOUNT@gmail.com";
var spreadsheetUrl = "https://docs.google.com/spreadsheets/d/SOME_REALLY_RANDOM_LOOKING_STRING/edit";
var maxTimeBetweenReadings = 12 * 60 * 60 * 1000; // 12 hours * 60 min/hr * 60s/min * 1000 ms/s
var nameOfMainResponsesSheetTab = "Responses";
var temperatureThreshold = 100.4; // Any value greater or equal to this will result in an alert email.

// 2. Go to your Spreadsheet and click Tools > Script editor. Copy and paste the entire contents of this file into the script editor.

// 3. Set up a time driven trigger for this. Hourly is probably good.
function periodicCheck(){
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  setupSummarySheet();
  var summarySheet = targetSpreadSheet.getSheetByName("Summary");
  for (var i=1; i <= summarySheet.getLastRow(); i++) {
    var rowContents = summarySheet.getSheetValues(i, 1, 1, 5)[0];
    var msTimestamp = Date.parse(rowContents[1]);
    var currentTime = new Date().getTime();
    if (msTimestamp + maxTimeBetweenReadings < currentTime) {
      MailApp.sendEmail(recipients, "[TDT ALERT]: Missed latest temperature check in from " + rowContents[0], rowContents[0] + " last checked in at " + rowContents[1]);
      var target = "B" + summarySheet.getLastRow();
      var range = summarySheet.getRange(target + ":" + target);
      range.setBackground("red");
    }
  }
}

// 4. Set up a spreadsheet driven trigger for this. Use onFormSubmit.
function onFormSubmit(e) {
  var timestamp = e.namedValues['Timestamp'][0];
  var userId = e.namedValues['UserID'][0];
  var temperature = parseFloat(e.namedValues['Temperature (F)'][0]);
  var feeling = e.namedValues['Feeling'][0];
  sendEmail(timestamp, userId, temperature, feeling);
  updateSummarySheet(timestamp, userId, temperature, feeling);
  updateIndividualSheet(timestamp, userId, temperature, feeling);
}

// 5. DONE!

function setupSummarySheet(){
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  try {
    var summarySheet = targetSpreadSheet.insertSheet("Summary");
    summarySheet.appendRow(["UserID", "Last entry", "Last temperature (F)", "Max temperature (F)", "Feeling"]);
  } catch (e) {
  }
}

function sendEmail(timestamp, userId, temperature, feeling) {
  var message = timestamp + " " + userId + " " + temperature + " " + feeling;
  var subject = ""
  if (temperature >= temperatureThreshold) {
    subject = "[TDT ALERT]: High temperature (" + temperature + " F) reported by " + userId + ".";
  } else if (feeling == "Sick") {
    subject = "[TDT ALERT]: " + userId + " is feeling sick.";
  } 
  if (subject.length > 0) {
    MailApp.sendEmail(recipients, subject, message);
  }
}

function updateSummarySheet(timestamp, userId, temperature, feeling) {
  var summarySheet;
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  try {
    summarySheet = targetSpreadSheet.insertSheet("Summary");
    summarySheet.appendRow(["UserID", "Last entry", "Last temperature (F)", "Max temperature (F)", "Feeling"]);
  } catch (e) {
  }
  summarySheet = targetSpreadSheet.getSheetByName("Summary");
  var maxTemp = temperature;
  for (var i=1; i <= summarySheet.getLastRow(); i++) {
    var rowContents = summarySheet.getSheetValues(i, 1, 1, 5)[0];
    if (rowContents[0] == userId) {
      var oldTemp = parseFloat(rowContents[3]);
      var currentTemp = parseFloat(temperature);
      if (oldTemp > currentTemp) {
        maxTemp = oldTemp;
      }
      summarySheet.deleteRow(i);
      break;
    }
  }
  summarySheet.appendRow([userId, timestamp, temperature, maxTemp, feeling]);
  if (temperature >= temperatureThreshold) {
     var target = "C" + summarySheet.getLastRow();
     var range = summarySheet.getRange(target + ":" + target);
     range.setBackground("red");
  }
  if (maxTemp >= temperatureThreshold) {
     var target = "D" + summarySheet.getLastRow();
     var range = summarySheet.getRange(target + ":" + target);
     range.setBackground("red");
  }
  if (feeling == "Sick") {
     var target = "E" + summarySheet.getLastRow();
     var range = summarySheet.getRange(target + ":" + target);
     range.setBackground("red");
  }
}

function updateIndividualSheet(timestamp, userId, temperature, feeling) {
  var individualSheet;
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  try {
    individualSheet = targetSpreadSheet.insertSheet(userId);
    individualSheet.appendRow(["Timestamp", "Temperature (F)", "Feeling"]);
  } catch (e) {
  }
  individualSheet = targetSpreadSheet.getSheetByName(userId);
  individualSheet.appendRow([timestamp, temperature, feeling]);
}

function onOpen() {
  var ui = SpreadsheetApp.getUi();
  // Or DocumentApp or FormApp.
  ui.createMenu('Temperature Data Trends')
      .addItem('View chart', 'showChart')
      .addToUi();
}

function showChart() {
 var sheet = SpreadsheetApp.getActiveSheet();
    if ((sheet.getName() != nameOfMainResponsesSheetTab) && (sheet.getName() != "Summary")) {
      var range1 = sheet.getRange("A1:A" + sheet.getLastRow());
      var range2 = sheet.getRange("B1:B" + sheet.getLastRow());
      var chartBuilder = sheet.newChart();
      chartBuilder.addRange(range1).addRange(range2)
          .setChartType(Charts.ChartType.LINE)
          .setPosition(2, 2, 0, 0)
          .setOption('title', sheet.getName());
      sheet.insertChart(chartBuilder.build());
    } else {
      SpreadsheetApp.getUi().alert("Please go a sheet for a specific User ID first.");
    }
}


