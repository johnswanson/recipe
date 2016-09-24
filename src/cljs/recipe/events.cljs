(ns recipe.events
  (:require [recipe.db :refer [default-value]]
            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path trim-v]]))

(reg-event-fx
 :initialize-db
 (fn [{:keys [db]}]
   {:db default-value}))

(reg-event-db
 :inc
 (fn [db _]
   (update-in db [:count] inc)))
