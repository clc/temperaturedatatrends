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

// Uses the Forms API to create the form for Temperature Data Trends.
//
// Usage:
// 1. Create a new Form.
// 2. Go to Tools > Script editor and dump the contents of this file into it.
// 3. Run "createForm" and grant authorizations.
// 4. Go back to Google Drive. You will now see that you have a new Form named
// "Temperature Data Trends" that has everything set up for it.
//
// Note: After the Form is created by the script, you can optionally go back and
// add data validation for the Temperature values to make sure they are within
// a reasonable range.
//
// author:  Charles L. Chen (clchen@google.com)

function createForm() {
  var form = FormApp.create('Temperature Data Trends');
  form.addTextItem().setTitle('UserID').setRequired(true);
  // Will need to manually add validation after this is created!
  form.addTextItem().setTitle('Temperature (F)').setRequired(true);
  
  var feelingItem = form.addMultipleChoiceItem().setTitle('Feeling').setRequired(true);  
  var symptomsPage = form.addPageBreakItem().setTitle('Do you have any of these symptoms?');
  var wellChoice = feelingItem.createChoice('Well', FormApp.PageNavigationType.SUBMIT);
  var sickChoice = feelingItem.createChoice('Sick', symptomsPage);
  feelingItem.setChoices([wellChoice, sickChoice]);
  
  var symptomsCheckbox = form.addCheckboxItem().setTitle('Symptoms');
  symptomsCheckbox.setChoices([symptomsCheckbox.createChoice('Extreme tiredness'),
                              symptomsCheckbox.createChoice('Muscle pain'),
                              symptomsCheckbox.createChoice('Headache'), 
                              symptomsCheckbox.createChoice('Sore throat'), 
                              symptomsCheckbox.createChoice('Vomiting'), 
                              symptomsCheckbox.createChoice('Diarrhea'), 
                              symptomsCheckbox.createChoice('Rash'), 
                              symptomsCheckbox.createChoice('Unexplained bleeding'), 
                              symptomsCheckbox.createChoice('Taking pain relievers')]);
  
  
Logger.log('Published URL: ' + form.getPublishedUrl());
Logger.log('Editor URL: ' + form.getEditUrl());
}

