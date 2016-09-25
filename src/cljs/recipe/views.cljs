(ns recipe.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn login-button
  []
  (let [auth-url (subscribe [:github/auth-url])]
    [:a {:href @auth-url} "login"]))

(defn app
  []
  (let [username (subscribe [:user/username])]
    (fn []
      (if @username
        [:div "Hello, " @username]
        [login-button]))))

