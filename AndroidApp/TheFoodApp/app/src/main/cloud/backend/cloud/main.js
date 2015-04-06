
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

function calculateAverageRating(ratingsArray) {
    var total = 0;
    var numRatings = ratingsArray.length;

    for (var index = 0; index < numRatings; ++index) {
        var star = ratingsArray[index].get("stars")
        total += star;
        console.log("Star: " + star + " Index: " + index + " id: " + ratingsArray[index].id);
    }

    console.log("Average Rating: " + (total / numRatings) + " Total: " + total + " Number of Ratings: " + numRatings);

    return total / numRatings;
}

function updateAverageRating(rating, dish, response) {
    if (response == undefined) { response = {}; }

    var relation = dish.relation("DishToPosts");
    var query = relation.query();
    // TODO: add 7 days constraint !!!

    query.find({
        success: function(result) {
            if (result != undefined) { result.push(rating); }
            var averageRating = calculateAverageRating(result);
            dish.set("averageRating", averageRating);
            dish.save();

            console.log("[updateAverageRating] Updated average rating ... " + averageRating);

            response.success = true;
            response.message = "";
        },
        error: function(error) {
            console.error("[updateAverageRating] Error updating average rating [" + error + "]");
            response.success = false;
            response.message = error;
        }
    });
}

Parse.Cloud.afterSave("Ratings", function(request, response) {

    if (response == undefined) { response = {}; }

  var rating = request.object;
  var dish = request.object.get("dish");
  var dishId = dish.id;

  var output = {
    success: true,
    message: "",
    dishId: dishId,
    ratingId: rating.id,
    stars: rating.get("stars"),
    averageRating: 0
  };

  console.log("Begin update average rating ... " + rating.id + ", " + dishId);
  updateAverageRating(rating, dish, output);

  /*var query = new Parse.Query("Dish");

  query.get(dishId, {
    success: function(dish) {
        console.log("Begin update average rating ... " + rating.id + ", " + dishId);
        updateAverageRating(dish, output);
    },
    error: function(error) {
        console.error("[afterSave] Error updating average rating ..." + error);
        output.success = false;
        output.message = error;
    }
  });*/
});

Parse.Cloud.define("TryAverage", function(request, response) {
  var query = new Parse.Query("Ratings");
  query.get("d7mPc8kU4J", {
      success: function(result) {
        response.success(JSON.stringify(result) + "\n\n" + result.get("dish").id + "\n\n" + result.get("stars"));

      },
      error: function(error) {
          response.error("ERROR " + error.code + " : " + error.message);
      }
  });
});