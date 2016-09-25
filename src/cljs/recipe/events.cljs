(ns recipe.events
  (:require [recipe.db :refer [default-value]]
            [recipe.ws :as ws]
            [day8.re-frame.async-flow-fx]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path trim-v]]
            ))

(defn boot-flow
  []
  {:first-dispatch [:ws-connect]
   :rules [{:when :seen? :events :success-ws-connect :dispatch [:do-initial-query]}
           {:when :seen? :events :success-initial-query :dispatch [:success-boot] :halt? true}
           {:when :seen-any-of?
            :events [:fail-ws-connect :fail-initial-query]
            :dispatch [:fail-boot] :halt? true}]})

(reg-event-fx
 :do-initial-query
 (fn [_ _]
   {:ws-query [{:query [:github/auth-url]
                :on-success [:success-initial-query]
                :on-failure [:fail-initial-query]
                :timeout 2000}]}))

(reg-event-db
 :success-initial-query
 (fn [db [_ reply]]
   (assoc db :github/auth-url reply)))

(reg-event-fx
 :fail-initial-query
 (fn [fx [_ reply]]
   fx))

(reg-event-fx
 :ws-connect
 (fn [_ _]
   {:ws-connect nil}))

(reg-event-fx
 :boot
 (fn [_ _]
   {:db (-> default-value)
    :async-flow (boot-flow)}))

