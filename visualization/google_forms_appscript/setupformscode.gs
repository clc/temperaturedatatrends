/**
 * Uses the Forms API to create the form for Temperature Data Trends.
 */
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

