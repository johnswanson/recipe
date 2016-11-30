(ns recipe.ws
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [taoensso.sente :as sente :refer [cb-success?]]
            [taoensso.timbre :as log]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [re-frame.core :refer [dispatch reg-fx console]]))

(def ws (atom nil))

(defmulti -event-msg-handler :id)

(defmethod -event-msg-handler :default
  [{:as ev-msg}]
  (console :warning "Unhandled event" ev-msg))

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] ?data]
    (if (:first-open? new-state-map)
      (dispatch [:success-ws-connect])
      (console :log "Channel socket state change: %s" new-state-map))))

(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (console :log "Push event from server: " ?data))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (console :log "Handshake: " ?data))

(defn event-msg-handler [{:as ev-msg :keys [id ?data event]}]
  (console :log ev-msg)
  (-event-msg-handler ev-msg))

(defonce router_ (atom nil))
(defn stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! [ch-chsk]
  (stop-router!)
  (reset! router_ (sente/start-client-chsk-router! ch-chsk event-msg-handler)))

(defn ws-connect [_]
  (if-let [{:keys [chsk ch-recv send-fn state]}
           (sente/make-channel-socket! "/chsk" {:type :auto})]
    (do
      (start-router! ch-recv)
      (reset! ws {:chsk-send! send-fn}))
    (dispatch [:fail-ws-connect])))

(defn run-query! [{:keys [query on-success on-failure timeout]}]
  (if-let [f (:chsk-send! @ws)]
    (f query timeout (fn [reply]
                       (if (and (sente/cb-success? reply)
                                (nil? (:error reply)))
                         (dispatch (conj on-success (:result reply)))
                         (dispatch (conj on-failure reply)))))
    (dispatch on-failure)))

(reg-fx
 :ws
 (fn ws [vs]
   (doseq [v (if (coll? vs) vs [vs])]
     (run-query! v))))

(reg-fx
 :ws-connect
 ws-connect)

