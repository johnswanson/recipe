(ns recipe.events
  (:require [recipe.db :refer [default-value valid-db-schema? valid-schema?]]
            [recipe.ws :as ws]
            [day8.re-frame.async-flow-fx]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [inject-cofx path trim-v after debug console]]
            [re-frame.db :as db]
            [clojure.spec :as s]))

(defn reg-event-db
  ([id handler-fn]
   (reg-event-db id nil handler-fn))
  ([id interceptors handler-fn]
   (re-frame.core/reg-event-db
    id
    [(when ^boolean goog.DEBUG debug)
     (when ^boolean goog.DEBUG (after valid-schema?))
     trim-v
     interceptors]
    handler-fn)))

(defn reg-event-fx
  ([id handler-fn]
   (reg-event-fx id nil handler-fn))
  ([id interceptors handler-fn]
   (re-frame.core/reg-event-fx
    id
    [(when ^boolean goog.DEBUG debug)
     (when ^boolean goog.DEBUG (after valid-db-schema?))
     trim-v
     interceptors]
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

(defn success-get-user [db [user]]
  (if user
    (assoc db :recipe.db/logged-in-user user)
    db))

(reg-event-db
 :success-get-user
 success-get-user)

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
 (fn [db [reply]]
   (assoc db :github/auth-url reply)))

(reg-event-fx
 :fail-initial-query
 (fn [fx [reply]]
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

(defn update-editing-recipe
  [db [k value]]
  (let [recipe (if-let [existing (get-in db [:recipe.db/editing-recipe])]
                 (assoc existing k value)
                 {:recipe/id (recipe.db/tempid)
                  k value})]
    (assoc db :recipe.db/editing-recipe recipe)))

(reg-event-db
 :update-editing-recipe
 update-editing-recipe)

(defn save-recipe
  [{:keys [db]} _]
  (let [id (recipe.db/tempid)
        recipe (get db :recipe.db/editing-recipe)]
    {:db (-> db
             (dissoc :recipe.db/editing-recipe)
             (assoc-in [:recipes/by-id id] recipe)
             (update-in [:recipe.db/recipes] #(into [] (conj % id))))
     :ws-query [{:query [:save-recipe recipe]
                 :on-success [:success-save-recipe]
                 :on-failure [:fail-save-recipe]
                 :timeout 2000}]}))

(defn success-save-recipe
  [db {:keys [db/id]}]
  db)

(defn fail-save-recipe
  [db {:keys [db/id]}]
  db)

(defn start-import-recipe
  [{:keys [db]} [url]]
  {:db (assoc-in db [:imports url :status] :pending)
   :ws-query [{:query [:import/start url]
               :on-success [:success-import-url url]
               :on-failure [:fail-import-url url]
               :timeout 8000}]})

(reg-event-fx :start-import-recipe start-import-recipe)

(defn fail-import-url
  [db [url error]]
  (assoc-in db [:recipe.db/imports url :import/error] error))

(reg-event-db :fail-import-url fail-import-url)

(defn success-import-url
  [db [url response]]
  (assoc-in db [:recipe.db/imports url :import/data] response))

(reg-event-db :success-import-url success-import-url)

