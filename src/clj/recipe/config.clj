(ns recipe.config
  (:require [environ.core :refer [env]]))

(defn integer
  [v]
  (when v (Integer. v)))

(defn config [k & {:keys [as default]
                   :or {as identity
                        default nil}}]
  (if (get env k default)
    (as (get env k default))
    (throw (Exception.
            (format "Configuration value not provided: %s" k)))))

(def dev-config
  {:db     {:host     (config :db-host :default "127.0.0.47")
            :port     (config :db-port :default "4334" :as integer)
            :database (config :db-database :default "recipe")
            :storage  (config :db-storage :default "dev")}
   :http   {:port (config :http-port :default "8080" :as integer)
            :ip   (config :http-ip :default "127.0.0.1")}
   :github {:client-id     (config :github-client-id)
            :client-secret (config :github-client-secret)
            :redirect-uri  (config :github-redirect-uri :default "http://localhost:8080/callback")}

   :figwheel {:figwheel-options {:css-dirs  ["resources/public/css"]
                                 :server-ip (config :figwheel-server-ip :default "localhost")}
              :build-ids        ["dev" "cards"]
              :all-builds
              [{:id           "cards"
                :figwheel     {:devcards true}
                :source-paths ["src/cljs"]
                :compiler     {:main       'cards.core
                               :asset-path "/js/compiled_cards"
                               :output-to  "resources/public/js/compiled_cards/app.js"
                               :output-dir "resources/public/js/compiled_cards"
                               :source-map true}}
               {:id           "dev"
                :figwheel     {:websocket-host (config :figwheel-websocket-host :default "localhost")}
                :source-paths ["src/cljs"]
                :compiler     {:main          'recipe.dev
                               :asset-path    "/js/compiled"
                               :output-to     "resources/public/js/compiled/app.js"
                               :output-dir    "resources/public/js/compiled"
                               :optimizations :none
                               :source-map    true}}]}})
