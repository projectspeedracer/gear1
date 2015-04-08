
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
       // console.log("Star: " + star + " Index: " + index + " id: " + ratingsArray[index].id);
    }

    console.log("Average Rating: " + (total / numRatings) + " Total: " + total + " Number of Ratings: " + numRatings);

    return total / numRatings;
}

function updateAverageRating(rating, dish, response) {
    if (response == undefined) { response = {}; }

    console.log("[updateAverageRating] entering..");
    var relation = dish.relation("DishToPosts");
    var query = relation.query();
    // TODO: add 7 days constraint !!!

    query.find({
        success: function(result) {
            // TODO: not required to fake if we add relation first..
            //if (result != undefined) { result.push(rating); }
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

function sendPush(rating, dish, restaurant, user) {
    var channel = restaurant.get("places_id");
    console.log("Sending Push over channel: " + channel);

    var userName = user.get("appUserName");
    var dishName = dish.get("name");
    if (rating.get("stars") == 1) {
        var message = (userName + " did not like " + dishName);
    } else if (rating.get("stars") == 2) {
        var message = (userName + " says " + dishName + " was not bad!");
    }
    else {
        var message = (userName + " loved " + dishName);
    }

    Parse.Push.send({
        channels: [ channel ],
        data: {
          alert: message,
          userId: user.id,
          ratingId: rating.id,
          dishId: dish.id
        }
      }, {
        success: function() {
          // Push was successful
          console.log("Push successful rating ... " + rating.id + ", " + dish.id);
        },
        error: function(error) {
          // Handle error
          console.log("Push error rating ... " + rating.id + ", " + dish.id);
        }
      });
}

function addRelations(rating, dish, restaurant, user) {
    console.log("Added relations ...");

    addUserRelation(rating, user);

    addRestaurantRelation(rating, restaurant);

    // update Dish after saving..
    addDishRelation(rating, dish);

    console.log("Added relations, done...");
}

function addDishRelation(rating, dish) {
    var relationDish = dish.relation("DishToPosts");
    relationDish.add(rating);
    dish.save({
                      success: function() {
                        // Push was successful
                        updateDish(rating, dish);
                        console.log("Dish Relation added successfully ... " + rating.id + ", " + dish.id);
                      },
                      error: function(error) {
                        // Handle error
                        console.log("ERROR: Dish Relation ... " + rating.id + ", " + dish.id);
                      }
                    });
}

function addRestaurantRelation(rating, restaurant) {
    var relationRestaurant = restaurant.relation("RestaurantToPosts");
    relationRestaurant.add(rating);
    restaurant.save();
}

function addUserRelation(rating, user) {
    var relationUser = user.relation("UserToPosts");
    relationUser.add(rating);
    user.save();
}

function updateDish(rating, dish) {
    var output = {
        success: true,
        message: "",
        dishId: dish.id,
        ratingId: rating.id,
        stars: rating.get("stars"),
        averageRating: 0
    };

    console.log("Begin update average rating ... " + rating.id + ", " + dish.id);
    updateAverageRating(rating, dish, output);
}

Parse.Cloud.afterSave("Ratings", function(request, response) {

    if (response == undefined) { response = {}; }

  var rating = request.object;
  var dish = request.object.get("dish");
  var restaurant = request.object.get("restaurant");
  var user = request.object.get("user");


  console.log("fetching restaurant now");
  restaurant.fetch({
    success: function(restaurantFetched) {
        console.log("fetching dish now");
        dish.fetch({
            success: function(dishFetched) {
                console.log("fetching user now");
                user.fetch({
                    success: function(userFetched) {
                        // Send push notification
                        sendPush(rating, dishFetched, restaurantFetched, userFetched);

                        // Add relations and save dish
                        addRelations(rating, dishFetched, restaurantFetched, userFetched);
                    }
                });
            }
          });
    }
  });



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