(ns recipe.systems
  (:require [recipe.handler :refer [ring-handler site sente-handler]]
            [recipe.github]
            [com.stuartsierra.component :as component]
            [ring.middleware.defaults :refer [wrap-defaults]]
            [taoensso.sente.server-adapters.http-kit :refer [sente-web-server-adapter]]
            [taoensso.timbre :as log]
            [system.components
             [http-kit :refer [new-web-server]]
             [datomic :refer [new-datomic-db]]
             [sente :refer [new-channel-sockets sente-routes]]
             [endpoint :refer [new-endpoint]]
             [handler :refer [new-handler]]
             [middleware :refer [new-middleware]]]
            [environ.core :refer [env]]))

(defn integer
  [v]
  (when v (Integer. v)))

(def dev-defaults
  {:db-host "127.0.0.47"
   :db-port "4334"
   :db-database "recipe"
   :db-storage "dev"
   :http-port "8080"
   :http-ip "127.0.0.1"
   :github-client-id nil
   :github-client-secret nil
   :github-redirect-uri "http://localhost:8080/callback"})

(defn config [defaults k & {:keys [as]
                            :or {as identity}}]
  (cond
    (env k) (as (env k))
    (defaults k) (as (defaults k))))

(def dev-config
  (let [c (partial config dev-defaults)]
    {:db-host (c :db-host)
     :db-port (c :db-port :as integer)
     :db-database (c :db-database)
     :db-storage (c :db-storage)
     :http-port (c :http-port :as integer)
     :http-ip (c :http-ip)
     :github-client-id (c :github-client-id)
     :github-client-secret (c :github-client-secret)
     :github-redirect-uri (c :github-redirect-uri)}))

(defn routes [_]
  (component/using (new-endpoint ring-handler) [:db :github]))

(defn db [{:keys [db-host db-port db-database db-storage]}]
  (new-datomic-db (format "datomic:%s://%s:%d/%s"
                          db-storage
                          db-host
                          db-port
                          db-database)))

(defn sente-endpoint [_]
  (component/using
   (new-endpoint sente-routes)
   [:sente]))

(defn sente [_]
  (component/using
   (new-channel-sockets sente-handler sente-web-server-adapter {:wrap-component? true})
   [:db :github]))

(defn middleware [_]
  (new-middleware {:middleware [[wrap-defaults :defaults]]
                   :defaults site}))

(defn handler [_]
  (component/using
   (new-handler)
   [:sente-endpoint :routes :middleware]))

(defn http [{:keys [http-port http-ip]}]
  (component/using
   (new-web-server nil nil {:port http-port
                            :ip http-ip})
   [:handler]))

(defn github [config]
  (recipe.github/new-api {:client-id (:github-client-id config)
                          :client-secret (:github-client-secret config)
                          :redirect-uri (:github-redirect-uri config)}))

(defn base-system
  [config]
  {:db (db config)
   :routes (routes config)
   :sente-endpoint (sente-endpoint config)
   :sente (sente config)
   :middleware (middleware config)
   :handler (handler config)
   :github (github config)
   :http (http config)})

(def dev-system (base-system dev-config))

(def prod-system (base-system dev-config))

