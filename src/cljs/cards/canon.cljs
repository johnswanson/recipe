(ns cards.canon)

(def -recipes
  [{:db/id                1
    :recipe/thumbnail-url
    "https://jds.objects-us-west-1.dream.io/screenshots/2016-08-10_14.59.52.png"
    :recipe/title         "Ms. Vo Thi Huong's Garlic Shrimp"
    :recipe/description
    "Writer Laurie Woolever brought this recipe back to us from the white-sand
    beaches of Hue, Vietnam, where a lady named Ms. Vo Thi Huong stir-fries
    fresh shrimp like these."
    :recipe/ingredients   ["3T neutral oil"
                           "6 garlic cloves, roughly chopped"
                           "1 large shallot, roughly chopped"
                           "2 bunches (12-14) scallions, cut into 2\" lengths"
                           "1lb large shrimp, shelled and deveined"
                           "1T sriracha sauce (or more to taste)"
                           "2T kewpie mayonnaise"
                           "2T soy sauce"
                           "+ cooked jasmine rice, for serving"]}
   {:db/id                2
    :recipe/thumbnail-url
    "https://jds.objects-us-west-1.dream.io/screenshots/2016-08-10_14.59.33.png"
    :recipe/title         "Tod Mun Fish Cakes"
    :recipe/description
    "This is one of those kick-off-the-meal dishes I always order at Thai
    places. It's *really* good."
    :recipe/ingredients   ["12oz fresh cod or other mild white fish, cut
                           into 1\" pieces"
                           "1/4C red curry paste"
                           "1 large egg"
                           "1T fish sauce"
                           "1T sugar"
                           "1/2t kosher salt"
                           "1/2C thai sweet chili sauce"
                           "1C chopped seeded cucumber"
                           "2T chopped roasted unsalted peanuts"
                           "2T minced cilantro"
                           "1t sambal oelek (optional)"
                           "+ lime juice or salt"
                           "+ neutral oil"
                           "+ lime wedges, for serving"]}])

(defn recipes []
  (shuffle -recipes))
