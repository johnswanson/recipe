(ns recipe.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn login-button
  []
  (let [auth-url (subscribe [:github/auth-url])]
    [:a {:href @auth-url} "login"]))

(defn logout-button
  [] [:a {:href "/logout"} "logout"])

(defn logged-in-app-view
  [user recipes]
  [:div
   [:div "Hello, " (:user/username user)]
   [:div
    (for [recipe recipes]
      ^:key [:span recipe])]
   [logout-button]])

(defn logged-in-app
  [user]
  (let [recipes (subscribe [:app/recipes])]
    (logged-in-app-view user @recipes)))

(defn app-view [user]
  (if user
    [logged-in-app user]
    [login-button]))

(defn app
  []
  (let [user (subscribe [:app/logged-in-user])]
    (fn [] [app-view @user])))

