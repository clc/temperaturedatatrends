// Copyright 2014 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * @fileoverview Scrapes the data from the Temperature Data Trends spreadsheet
 * and sends it to a local server. Also scrapes the fields in a form to generate
 * the TDT link.
 *
 * @author clchen@google.com (Charles L Chen)
 */




//-----------<START OF SCRAPER FOR RESULTS>------------------------------------
var summaryData = new Object();

function addToSummary(cells){
  if (cells.length == 4) {
    var timeStamp = cells[0].textContent;
    var patientId = cells[1].textContent;
    var temperature = cells[2].textContent;
    var status = cells[3].textContent;
    if (timeStamp == "Timestamp") {
      return;
    }
    if ((timeStamp.length > 0) && 
        (patientId.length > 0) && 
        (temperature.length > 0) && 
        (status.length > 0)) {
      if (summaryData[patientId]) {
        if (parseFloat(temperature) > parseFloat(summaryData[patientId]["max_temp"])) {
          summaryData[patientId]["max_temp"] = temperature;
        }
      } else {
        summaryData[patientId] = new Object();
        summaryData[patientId]["max_temp"] = temperature;
      }
      summaryData[patientId]["last_entry"] = timeStamp;
      summaryData[patientId]["last_temp"] = temperature;
      summaryData[patientId]["last_status"] = status;
    }
  }
}

function summaryToString() {
  var output = "<summary>"
  output = output + "patientId, lastEntry, lastTemp, maxTemp, lastStatus\n";
  var keys = Object.keys(summaryData);
  for (var i=0,key; key = keys[i]; i++) {
    output = output + key + ", " + summaryData[key]["last_entry"] + ", " +
        summaryData[key]["last_temp"]  + ", " + summaryData[key]["max_temp"] +
        ", " + summaryData[key]["last_status"] + "\n";
  }
  output = output + "</summary>"
  return output;
}

function scrapeContent(){
  if (document.location.toString().indexOf("pubhtml") == -1) {
    return;
  }
  var output = "<raw>";
  summaryData = new Object();
  var rows = document.getElementsByTagName("tr");
  for (var i=0, row; row = rows[i]; i++) {
    var cells = row.getElementsByTagName("td");
    addToSummary(cells);
    for (var j=0, cell; cell = cells[j]; j++) {
      output = output + cell.textContent + ", ";
    }
    output = output + "\n";
  }
  // Convert "Well"/"Sick" status into 0/1 for data analysis tools.
  output = output.replace(/Well/g, '0');
  output = output.replace(/Sick/g, '1');
  output = output + "</raw>";

  // Put in the summary data for easier data analysis.
  output = summaryToString() + output;

  chrome.runtime.sendMessage({data: output});
  window.setTimeout(function(){window.location.reload();}, 60000);
}

window.setTimeout(scrapeContent, 1000);

//-----------<END OF SCRAPER FOR RESULTS>------------------------------------

//-----------<START OF SCRAPER FOR FORM GENERATOR>---------------------------
var idField = "";
var tempField = "";
var feelingField = "";
var targetUrl = "";
var destValue = "public%20health%20agency";

function outputText() {
  var idValue = document.getElementById("tdtInput").value;
  var linkText = "https://tdt/?";
  linkText = linkText + "idField=" + idField + "&";
  linkText = linkText + "tempField=" + tempField + "&";
  linkText = linkText + "feelingField=" + feelingField + "&";
  linkText = linkText + "idValue=" + idValue + "&";
  linkText = linkText + "destValue=" + destValue + "&";
  linkText = linkText + "targetUrl=" + targetUrl;
  document.getElementById("tdtOutput").value = linkText;
}

function makeTdtLinkGenerator() {
  var idEntry = document.createElement("input");
  idEntry.type = "text";
  idEntry.id = "tdtInput";
  document.body.appendChild(idEntry);
  document.body.appendChild(document.createElement("br"));
  document.body.appendChild(document.createElement("br"));
  var getTdtButton = document.createElement("button");
  getTdtButton.textContent = "Generate TDT link";
  getTdtButton.addEventListener("click", outputText);
  document.body.appendChild(getTdtButton);
  document.body.appendChild(document.createElement("br"));
  document.body.appendChild(document.createElement("br"));
  var output = document.createElement("input");
  output.type = "text";
  output.id = "tdtOutput";
  output.size = 300;
  document.body.appendChild(output);
}

function initFormGenerator() {
  var inputs = document.getElementsByTagName("input");
  if (inputs.length > 3) {
    if ((inputs[0].type == "text") &&
        (inputs[1].type == "text") &&
        (inputs[2].type == "radio")) {
      idField = inputs[0].name;
      tempField = inputs[1].name;
      feelingField = inputs[2].name;
      makeTdtLinkGenerator();
    }
  }
}

if (document.location.toString().indexOf("/viewform") != -1) {
  targetUrl = document.location.toString().replace("viewform", "formResponse");
  window.setTimeout(initFormGenerator, 1000);
}

//-----------<END OF SCRAPER FOR FORM GENERATOR>---------------------------


