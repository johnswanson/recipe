(ns recipe.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :github/auth-url
 (fn [db _]
   (:github/auth-url db)))

(reg-sub
 :app/logged-in-user
 (fn [db _]
   (:app/logged-in-user db)))

(reg-sub
 :user/username
 (fn [db _]
   (let [user (subscribe [:app/logged-in-user])]
     (:user/username @user))))

(reg-sub
 :user/logged-in?
 (fn [db _]
   @(subscribe [:user/username])))
