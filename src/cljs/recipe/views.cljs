(ns recipe.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn app
  []
  (let [v (subscribe [:count])]
    (fn []
      [:div "howdy hey, " @v
       [:button {:on-click #(dispatch [:inc])} "clickme"]])))

