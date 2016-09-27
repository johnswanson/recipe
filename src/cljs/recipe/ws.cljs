(ns recipe.ws
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [taoensso.sente :as sente :refer [cb-success?]]
            [taoensso.timbre :as log]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [re-frame.core :refer [dispatch reg-fx reg-event-fx]]))

(def ws (atom nil))

(defmulti handle-event first)
(defmethod handle-event :chsk/handshake [event]
  (log/infof "Event %s" event))
(defmethod handle-event :chsk/state [[_ [old new]]]
  (cond
    (and (not (:open? old)) (:open? new)) (dispatch [:success-ws-connect new])
    (and (not (:open? new)) (:open? old)) (dispatch [:fail-ws-connect new])))

(reg-event-fx
 ::ws-received
 (fn ws-received [_ [_ {:keys [event]}]]
   (handle-event event)))

(reg-fx
 :ws-connect
 (fn ws-connect [_]
   (if-let [{:keys [chsk ch-recv send-fn state]}
            (sente/make-channel-socket! "/chsk" {:type :auto})]
     ;; if we get a successful event on the channel, then dispatch successful
     ;; if we get a failure event on the channel, then dispatch
     ;; if the make-channel-socket! call just failed, then dispatch failure
     (do
       (reset! ws {:chsk chsk
                   :ch-chsk ch-recv
                   :chsk-send! send-fn
                   :chsk-state state})
       (go-loop []
         (when-let [v (<! ch-recv)]
           (dispatch [::ws-received v])
           (recur))))
     (dispatch [:failure-ws-connect]))))

(defn run-query! [{:keys [query on-success on-failure timeout]}]
  (if-let [f (:chsk-send! @ws)]
    (f [:app/query query] timeout (fn [reply]
                                    (if (sente/cb-success? reply)
                                      (dispatch (conj on-success reply))
                                      (dispatch (conj on-failure reply)))))
    (dispatch on-failure)))

(reg-fx
 :ws-query
 (fn ws-query [vs]
   (doseq [v (if (coll? vs) vs [vs])]
     (run-query! v))))

