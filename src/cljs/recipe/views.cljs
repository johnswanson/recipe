(ns recipe.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn app
  []
  (let [auth-url (subscribe [:github/auth-url])]
    (fn []
      [:a {:href @auth-url} "login"])))

