
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.beforeSave('Location', function(request, response) {
  // Check if the user added a pin in the last minute
  var LocationObject = Parse.Object.extend('Location');
  var query = new Parse.Query(LocationObject);

  var oneMinuteAgo = new Date();
  oneMinuteAgo.setMinutes(oneMinuteAgo.getMinutes() - 1);
  query.greaterThan('createdAt', oneMinuteAgo);

  query.equalTo('user', Parse.User.current());

  // Count the number of pins
  query.count({
    success: function(count) {
      if (count > 0) {
        response.error('Sorry, too soon to post again!');
      } else {
        response.success();
      }
    },
    error: function(error) {
      response.error('Oups something went wrong.');
    }
  });
});