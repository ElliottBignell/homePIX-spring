$(document).ready(function() {

$('#leftSelect').change(function() {
    var selectedValue = $(this).val();
    var otherValue = $('#rightSelect').val();
    // Navigate to a new URL using the selected value
    window.location.href = '/organise/' + encodeURIComponent(selectedValue) + '/' + encodeURIComponent(otherValue);
});

$('#rightSelect').change(function() {
    var selectedValue = $(this).val();
    var otherValue = $('#leftSelect').val();
    // Navigate to a new URL using the selected value
    window.location.href = '/organise/' + encodeURIComponent(otherValue) + '/' + encodeURIComponent(selectedValue);
});

$('figure').click(function() {

    // Toggle 'selected' class on the clicked figure
    $(this).toggleClass('selected');

    // Remove 'active' class from all figures and add 'inactive'
    $('figure').removeClass('active').addClass('inactive');

    // Add 'active' class to the last clicked figure and remove 'inactive'
    $(this).addClass('active').removeClass('inactive');
});

$('#moveBtn').click(function() {

  // Initialize an array to hold the numerical IDs
  var numericalIds = [];

  // Get all figures with the 'selected' class
  var selectedFigures = $('figure.selected');

  // Process each selected figure to extract the numerical ID
  selectedFigures.each(function() {
      // Split the ID by hyphen and get the second part (the numerical ID)
      var figureIdParts = $(this).attr('id').split('-');
      var numericalId = figureIdParts[1]; // Assuming the ID format is always 'figure-12345'

      // Add the numerical ID to the array
      numericalIds.push(numericalId);
  });

  // Join the array of numerical IDs into a comma-delimited string
  var idsString = numericalIds.join(',');

  // Now you have a comma-delimited string of all selected numerical IDs
  console.log(idsString);

  // Model attributes injected directly into JavaScript
  var leftFolder = /*[[${leftFolder}]]*/ 'defaultLeftFolder';
  var rightFolder = /*[[${rightFolder}]]*/ 'defaultRightFolder';

  movePicture(idsString, leftFolder.name, rightFolder.name);
});
});
