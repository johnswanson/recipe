(ns recipe.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :github/auth-url
 (fn [db _]
   (:github/auth-url db)))
