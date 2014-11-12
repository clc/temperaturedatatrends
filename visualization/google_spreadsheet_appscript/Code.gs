/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// Google AppScript for generating alerts, summaries, and charts for TDT.
// author:  Rossum Robot Studio (rossumrobotstudio@gmail.com)
//          Charles L. Chen (clchen@google.com)

// Usage:
// 1. CONFIGURE THESE TO MATCH WHAT YOU HAVE!!!
var recipients = "MY_ACCOUNT@gmail.com";
var spreadsheetUrl = "https://docs.google.com/spreadsheets/d/SOME_REALLY_RANDOM_LOOKING_STRING/edit";
var maxTimeBetweenReadings = 12 * 60 * 60 * 1000; // 12 hours * 60 min/hr * 60s/min * 1000 ms/s
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
      var target = "B" + i;
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
  var symptoms = e.namedValues['Symptoms'][0];
  updateSummarySheet(timestamp, userId, temperature, feeling, symptoms);
  updateIndividualSheet(timestamp, userId, temperature, feeling, symptoms);
  // TODO: Uncomment this once issues are fixed.
  // updateAggregateChart();
  sendEmail(timestamp, userId, temperature, feeling, symptoms);
}

// 5. DONE!
var nameOfMainResponsesSheetTab = "Form Responses 1";

function setupSummarySheet(){
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  try {
    var summarySheet = targetSpreadSheet.insertSheet("Summary");
    summarySheet.appendRow(["UserID", "Last entry", "Last temperature (F)", "Max temperature (F)", "Feeling", "Symptoms"]);
  } catch (e) {
  }
}

function sendEmail(timestamp, userId, temperature, feeling, symptoms) {
  var message = timestamp + " " + userId + " " + temperature + " " + feeling;
  if (symptoms.length > 0) {
    message = message + "  Symptoms:" + symptoms;
  }
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

function updateSummarySheet(timestamp, userId, temperature, feeling, symptoms) {
  var summarySheet;
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  setupSummarySheet();
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
  summarySheet.appendRow([userId, timestamp, temperature, maxTemp, feeling, symptoms]);
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

function updateIndividualSheet(timestamp, userId, temperature, feeling, symptoms) {
  var individualSheet;
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  try {
    individualSheet = targetSpreadSheet.insertSheet(userId);
    individualSheet.appendRow(["Timestamp", "Temperature (F)", "Feeling", "Symptoms", "Threshold (F)"]);
    sortSheets();
  } catch (e) {
  }
  individualSheet = targetSpreadSheet.getSheetByName(userId);
  individualSheet.appendRow([timestamp, temperature, feeling, symptoms, temperatureThreshold]);
}

function updateAggregateChart(){
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  var rawResponsesSheet = targetSpreadSheet.getSheetByName(nameOfMainResponsesSheetTab);
  var aggregateSheet;
  try {
    aggregateSheet = targetSpreadSheet.insertSheet("Aggregate Chart");
    individualSheet.appendRow(["Timestamp", "Temperature (F)"]);
  } catch (e) {
  }
  aggregateSheet = targetSpreadSheet.getSheetByName("Aggregate Chart");
  aggregateSheet.clear();
  var userToIdMap = new Object();
  var users = new Array();
  for (var i=2; i <= rawResponsesSheet.getLastRow(); i++) {
    var userId = rawResponsesSheet.getRange("B" + i + ":" + "B" + i).getValues()[0][0];
    if (userToIdMap[userId] == null) {
      userToIdMap[userId] = users.length;
      users.push(userId);
    }
  }
  var headerData = new Array();
  headerData.push("Timestamp");
  for (var i = 0; i < users.length; i++) {
    headerData.push(users[i] + " (F)");
  }  
  aggregateSheet.appendRow(headerData);
  for (var i=2; i <= rawResponsesSheet.getLastRow(); i++) {
    var userId = rawResponsesSheet.getRange("B" + i + ":" + "B" + i).getValues()[0][0];
    var rowData = new Array();
    for (var j = 0; j<users.length; j++) {
      rowData.push("");
    }
    rowData[0] = rawResponsesSheet.getRange("A" + i + ":" + "A" + i).getValues()[0][0];
    rowData[userToIdMap[userId] + 1] = rawResponsesSheet.getRange("C" + i + ":" + "C" + i).getValues()[0][0];
    aggregateSheet.appendRow(rowData);
  } 
  showAggregateChart();
}

function showAggregateChart(){
  // TODO: Fix this function so it graphs all the entries rather than only hard coding 5.
  // Also need to delete existing charts.
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  var aggregateSheet = targetSpreadSheet.getSheetByName("Aggregate Chart");
  
      var range1 = aggregateSheet.getRange("A1:A" + aggregateSheet.getLastRow());
      var range2 = aggregateSheet.getRange("B1:B" + aggregateSheet.getLastRow());
      var range3 = aggregateSheet.getRange("C1:C" + aggregateSheet.getLastRow());
      var range4 = aggregateSheet.getRange("D1:D" + aggregateSheet.getLastRow());
      var range5 = aggregateSheet.getRange("E1:E" + aggregateSheet.getLastRow());
      var chartBuilder = aggregateSheet.newChart();
      chartBuilder.addRange(range1).addRange(range2).addRange(range3).addRange(range4).addRange(range5)
          .setChartType(Charts.ChartType.LINE)
          .setPosition(2, 2, 0, 0)
          .setOption('title', aggregateSheet.getName())
          .setOption('interpolateNulls', 'true')
          .setOption('pointSize', '7');
      aggregateSheet.insertChart(chartBuilder.build());

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
      var range3 = sheet.getRange("E1:E" + sheet.getLastRow());
      var chartBuilder = sheet.newChart();
      chartBuilder.addRange(range1).addRange(range2).addRange(range3)
          .setChartType(Charts.ChartType.LINE)
          .setPosition(2, 2, 0, 0)
          .setOption('title', sheet.getName())
          .setOption('pointSize', '7');
      sheet.insertChart(chartBuilder.build());
    } else {
      SpreadsheetApp.getUi().alert("Please go a sheet for a specific User ID first.");
    }
}

function sortSheets () {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var sheetNameArray = [];
  var sheets = ss.getSheets();
 
  for (var i = 0; i < sheets.length; i++) {
    sheetNameArray.push(sheets[i].getName());
  }
 
  sheetNameArray.sort();
 
  for( var j = 0; j < sheets.length; j++ ) {
    ss.setActiveSheet(ss.getSheetByName(sheetNameArray[j]));
    ss.moveActiveSheet(j + 1);
  }
  
  ss.setActiveSheet(ss.getSheetByName("Summary"));
  ss.moveActiveSheet(0);
  ss.setActiveSheet(ss.getSheetByName(nameOfMainResponsesSheetTab));
  ss.moveActiveSheet(0);
}
