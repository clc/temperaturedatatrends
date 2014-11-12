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
var maxHoursBetweenReadings = 12; // Default is 12 hours to enforce twice a day measurement.
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
    var maxTimeBetweenReadings = maxHoursBetweenReadings * 60 * 60 * 1000; // x hours * 60 min/hr * 60s/min * 1000 ms/s
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
  // TODO: Uncomment this once this is better tested.
  // updateAggregateChart(timestamp, userId, temperature);
  sendEmail(timestamp, userId, temperature, feeling, symptoms);
}

// 5. DONE!

//-------------------------------------------------------

var nameOfMainResponsesSheetTab = "Form Responses 1";
var nameOfSummaryTab = "Summary";
var nameOfAggregateChartTab = "Aggregate Chart";
var keyForUserToIdMap = "USER_TO_INDEX_MAP";
var keyForUsersArray = "USERS_ARRAY";

function setupSummarySheet(){
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  try {
    var summarySheet = targetSpreadSheet.insertSheet(nameOfSummaryTab);
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
  summarySheet = targetSpreadSheet.getSheetByName(nameOfSummaryTab);
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

function updateAggregateChart(timestamp, userId, temperature){
  try {
    var userToIdMap = JSON.parse(PropertiesService.getScriptProperties().getProperty(keyForUserToIdMap));
    var users = JSON.parse(PropertiesService.getScriptProperties().getProperty(keyForUsersArray));
    if (userToIdMap[userId] == null) {
      forceUpdateAggregateChart();
      return;
    }
    var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
    var aggregateSheet = targetSpreadSheet.getSheetByName(nameOfAggregateChartTab);
    var rowData = new Array();
    for (var j = 0; j<users.length; j++) {
      rowData.push("");
    }
    rowData[0] = timestamp;
    rowData[userToIdMap[userId] + 1] = temperature;
    aggregateSheet.appendRow(rowData);
  } catch (e) {
    forceUpdateAggregateChart();
  }
}

function forceUpdateAggregateChart(){
  // TODO: Be smarter here - only do a destructive full redraw of the aggregate chart if it's a new user.
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  var rawResponsesSheet = targetSpreadSheet.getSheetByName(nameOfMainResponsesSheetTab);
  var aggregateSheet;
  try {
    aggregateSheet = targetSpreadSheet.insertSheet(nameOfAggregateChartTab);
    individualSheet.appendRow(["Timestamp", "Temperature (F)"]);
  } catch (e) {
  }
  aggregateSheet = targetSpreadSheet.getSheetByName(nameOfAggregateChartTab);
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
  // Clean up old data
  var oldCharts = aggregateSheet.getCharts();
  for (var i = 0; i < oldCharts.length; i++) {
    aggregateSheet.removeChart(oldCharts[i]);
  }  
  aggregateSheet.clear();
  // Put in current data
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
  // Graph everything.
  drawAggregateChart();  
  // Save state
  PropertiesService.getScriptProperties().setProperty(keyForUserToIdMap, JSON.stringify(userToIdMap));
  PropertiesService.getScriptProperties().setProperty(keyForUsersArray, JSON.stringify(users));
}

function drawAggregateChart(){  
  var targetSpreadSheet = SpreadsheetApp.openByUrl(spreadsheetUrl);
  var aggregateSheet = targetSpreadSheet.getSheetByName(nameOfAggregateChartTab);
  var chartBuilder = aggregateSheet.newChart();
  chartBuilder.setChartType(Charts.ChartType.LINE)
          .setPosition(2, 2, 0, 0)
          .setOption('title', aggregateSheet.getName())
          .setOption('interpolateNulls', 'true')
          .setOption('pointSize', '7');
  var lastColumn = aggregateSheet.getLastColumn();
  if (lastColumn > 26) {
    lastColumn = 26;
  }
  for (var i = 0; i < lastColumn; i++) {
    var colLetter = String.fromCharCode("A".charCodeAt(0) + i);
    var range = aggregateSheet.getRange(colLetter + "1:" + colLetter + aggregateSheet.getLastRow());
    chartBuilder.addRange(range);
  }
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
    if (sheet.getName() == nameOfAggregateChartTab) {
      forceUpdateAggregateChart();
    } else if ((sheet.getName() != nameOfMainResponsesSheetTab) && (sheet.getName() != nameOfSummaryTab)) {
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
  
  ss.setActiveSheet(ss.getSheetByName(nameOfAggregateChartTab));
  ss.moveActiveSheet(0);
  ss.setActiveSheet(ss.getSheetByName(nameOfSummaryTab));
  ss.moveActiveSheet(0);
  ss.setActiveSheet(ss.getSheetByName(nameOfMainResponsesSheetTab));
  ss.moveActiveSheet(0);
}

