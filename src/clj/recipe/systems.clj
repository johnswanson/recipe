(ns recipe.systems
  (:require [recipe.handler :refer [ring-handler site sente-handler]]
            [recipe.github]
            [com.stuartsierra.component :as component]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
            [taoensso.timbre :as log]
            [system.components
             [http-kit :refer [new-web-server]]
             [postgres :refer [new-postgres-database]]
             [sente :refer [new-channel-sockets sente-routes]]
             [endpoint :refer [new-endpoint]]
             [handler :refer [new-handler]]
             [middleware :refer [new-middleware]]]))

(defn routes []
  (component/using (new-endpoint ring-handler) [:db :github]))

(defn db [config]
  (new-postgres-database config))

(defn sente-endpoint []
  (component/using
   (new-endpoint sente-routes)
   [:sente]))

(defn sente []
  (component/using
   (new-channel-sockets sente-handler sente-web-server-adapter {:wrap-component? true})
   [:db :github]))

(defn middleware []
  (new-middleware {:middleware [[wrap-defaults :defaults]]
                   :defaults site}))

(defn handler []
  (component/using
   (new-handler)
   [:sente-endpoint :routes :middleware]))

(defn http [config]
  (component/using
   (new-web-server nil nil config)
   [:handler]))

(defn github [config]
  (recipe.github/new-api config))

(defn base-system
  [config]
  {:db (db (:db config))
   :routes (routes)
   :sente-endpoint (sente-endpoint)
   :sente (sente)
   :middleware (middleware)
   :handler (handler)
   :github (github (:github config))
   :http (http (:http config))})


