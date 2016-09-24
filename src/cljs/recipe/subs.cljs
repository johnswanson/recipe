(ns recipe.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :count
 (fn [db _]
   (:count db)))
