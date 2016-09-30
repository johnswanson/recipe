(ns recipe.events
  (:require [recipe.db :refer [default-value valid-db-schema? valid-schema?]]
            [recipe.ws :as ws]
            [day8.re-frame.async-flow-fx]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [inject-cofx path trim-v after debug]]
            [re-frame.db :as db]))

(defn reg-event-db
  ([id handler-fn]
   (reg-event-db id nil handler-fn))
  ([id interceptors handler-fn]
   (re-frame.core/reg-event-db
    id
    [(when ^boolean goog.DEBUG debug)
     (when ^boolean goog.DEBUG (after valid-schema?))]
    handler-fn)))

(defn reg-event-fx
  ([id handler-fn]
   (reg-event-fx id nil handler-fn))
  ([id interceptors handler-fn]
   (re-frame.core/reg-event-fx
    id
    [(when ^boolean goog.DEBUG debug)
     (when ^boolean goog.DEBUG (after valid-db-schema?))]
    handler-fn)))

(defn boot-flow
  []
  {:first-dispatch [:ws-connect]
   :rules [{:when :seen? :events :success-ws-connect :dispatch [:do-initial-query]}
           {:when :seen-any-of? :events [:fail-get-auth-url :fail-get-user] :dispatch [:fail-initial-query]}
           {:when :seen-all-of? :events [:success-get-auth-url :success-get-user] :dispatch [:success-boot] :halt? true}
           {:when :seen-any-of?
            :events [:fail-ws-connect :fail-initial-query]
            :dispatch [:fail-boot] :halt? true}]})

(reg-event-db
 :success-ws-connect
 (fn [db _]
   db))

(reg-event-db
 :success-boot
 (fn [db _]
   db))

(reg-event-db
 :success-get-user
 (fn [db [_ user]]
   (if user
     (-> db
         (assoc-in [:recipe.db/by-id (:user/id user)] user)
         (assoc :recipe.db/logged-in-user (:user/id user)))
     db)))

(reg-event-fx
 :do-initial-query
 (fn [_ _]
   {:ws-query [{:query [:github/auth-url]
                :on-success [:success-get-auth-url]
                :on-failure [:fail-get-auth-url]
                :timeout 2000}
               {:query [:app/logged-in-user]
                :on-success [:success-get-user]
                :on-failure [:fail-get-user]
                :timeout 2000}]}))

(reg-event-db
 :success-get-auth-url
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
   {:db default-value
    :async-flow (boot-flow)}))

